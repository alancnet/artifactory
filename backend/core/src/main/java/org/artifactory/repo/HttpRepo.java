/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.repo;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.plugin.PluginsAddon;
import org.artifactory.addon.plugin.RemoteRequestCtx;
import org.artifactory.addon.plugin.download.AfterRemoteDownloadAction;
import org.artifactory.addon.plugin.download.BeforeRemoteDownloadAction;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.NullResourceStreamHandle;
import org.artifactory.io.RemoteResourceStreamHandle;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.remote.browse.HtmlRepositoryBrowser;
import org.artifactory.repo.remote.browse.HttpExecutor;
import org.artifactory.repo.remote.browse.RemoteItem;
import org.artifactory.repo.remote.browse.RemoteRepositoryBrowser;
import org.artifactory.repo.remote.browse.S3RepositoryBrowser;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.request.NullRequestContext;
import org.artifactory.request.RemoteRequestException;
import org.artifactory.request.RepoRequests;
import org.artifactory.request.Request;
import org.artifactory.request.RequestContext;
import org.artifactory.resource.RemoteRepoResource;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.traffic.TrafficService;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.HttpClientConfigurator;
import org.artifactory.util.HttpClientUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.iostreams.streams.in.BandwidthMonitorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.http.HttpHeaders.ACCEPT_ENCODING;
import static org.artifactory.request.ArtifactoryRequest.ARTIFACTORY_ORIGINATED;
import static org.artifactory.request.ArtifactoryRequest.ORIGIN_ARTIFACTORY;

public class HttpRepo extends RemoteRepoBase<HttpRepoDescriptor> {
    private static final Logger log = LoggerFactory.getLogger(HttpRepo.class);
    protected RemoteRepositoryBrowser remoteBrowser;
    @Nullable
    private CloseableHttpClient client;
    private boolean handleGzipResponse;
    private LayoutsCoreAddon layoutsCoreAddon;

    @GuardedBy("offlineCheckerSync")
    private Thread onlineMonitorThread;
    private Object onlineMonitorSync = new Object();

    public HttpRepo(HttpRepoDescriptor descriptor, InternalRepositoryService repositoryService,
            boolean globalOfflineMode, RemoteRepo oldRemoteRepo) {
        super(descriptor, repositoryService, globalOfflineMode, oldRemoteRepo);
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        layoutsCoreAddon = addonsManager.addonByType(LayoutsCoreAddon.class);
    }

    private static long getLastModified(HttpResponse response) {
        Header lastModifiedHeader = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (lastModifiedHeader == null) {
            return -1;
        }
        String lastModifiedString = lastModifiedHeader.getValue();
        //try {
        Date lastModifiedDate = DateUtils.parseDate(lastModifiedString);
        if (lastModifiedDate != null) {
            return lastModifiedDate.getTime();
        } else {
            log.warn("Unable to parse Last-Modified header : " + lastModifiedString);
            return System.currentTimeMillis();
        }
    }

    private static Set<ChecksumInfo> getChecksums(HttpResponse response) {
        Set<ChecksumInfo> remoteChecksums = Sets.newHashSet();

        ChecksumInfo md5ChecksumInfo = getChecksumInfoObject(ChecksumType.md5,
                response.getFirstHeader(ArtifactoryRequest.CHECKSUM_MD5));
        if (md5ChecksumInfo != null) {
            remoteChecksums.add(md5ChecksumInfo);
        }

        ChecksumInfo sha1ChecksumInfo = getChecksumInfoObject(ChecksumType.sha1,
                response.getFirstHeader(ArtifactoryRequest.CHECKSUM_SHA1));
        if (sha1ChecksumInfo != null) {
            remoteChecksums.add(sha1ChecksumInfo);
        }

        return remoteChecksums;
    }

    private static ChecksumInfo getChecksumInfoObject(ChecksumType type, Header checksumHeader) {
        if (checksumHeader == null) {
            return null;
        }

        return new ChecksumInfo(type, checksumHeader.getValue(), null);
    }

    @Override
    public void init() {
        super.init();
        // TODO: This flag should be in the remote repo descriptor
        handleGzipResponse = ConstantValues.httpAcceptEncodingGzip.getBoolean();
        if (!isOffline()) {
            this.client = createHttpClient();
        }
    }

    private synchronized void initRemoteRepositoryBrowser() {
        if (remoteBrowser != null) {
            return; // already initialized
        }
        HttpExecutor clientExec = new HttpExecutor() {
            @Override
            public CloseableHttpResponse executeMethod(HttpRequestBase method) throws IOException {
                return HttpRepo.this.executeMethod(method);
            }
        };
        boolean s3Repository = S3RepositoryBrowser.isS3Repository(getUrl(), getHttpClient());
        if (s3Repository) {
            log.debug("Repository {} caches S3 repository", getKey());
            remoteBrowser = new S3RepositoryBrowser(clientExec, this);
        } else {
            remoteBrowser = new HtmlRepositoryBrowser(clientExec);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        cleanupResources();
        if (client != null) {
            IOUtils.closeQuietly(client);
        }
    }

    @Override
    public void cleanupResources() {
        stopOfflineCheckThread();
    }

    public String getUsername() {
        return getDescriptor().getUsername();
    }

    public String getPassword() {
        return getDescriptor().getPassword();
    }

    public boolean isAllowAnyHostAuth() {
        return getDescriptor().isAllowAnyHostAuth();
    }

    public boolean isEnableTokenAuthentication() {
        return getDescriptor().isEnableTokenAuthentication();
    }

    public boolean isEnableCookieManagement() {
        return getDescriptor().isEnableCookieManagement();
    }

    public int getSocketTimeoutMillis() {
        return getDescriptor().getSocketTimeoutMillis();
    }

    public String getLocalAddress() {
        return getDescriptor().getLocalAddress();
    }

    public ProxyDescriptor getProxy() {
        return getDescriptor().getProxy();
    }

    @Override
    public ResourceStreamHandle conditionalRetrieveResource(String relPath, boolean forceRemoteDownload)
            throws IOException {
        //repo1 does not respect conditional get so the following is irrelevant for now.
        /*
        Date modifiedSince;
        if (modifiedSince != null) {
            //Add the if modified since
            String formattedDate = DateUtil.formatDate(modifiedSince);
            method.setRequestHeader("If-Modified-Since", formattedDate);
        }
        if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
            return new NullResourceStreamHandle();
        }
        */
        //Need to do a conditional get by hand - testing a head result last modified date against the current file
        if (!forceRemoteDownload && isStoreArtifactsLocally()) {
            RepoResource cachedResource = getCachedResource(relPath);
            if (cachedResource.isFound()) {
                if (cachedResource.isExpired()) {
                    //Send HEAD
                    RepoResource resource = retrieveInfo(relPath, false/*The relPath refers to files (.gz)*/, null);
                    if (resource.isFound()) {
                        if (cachedResource.getLastModified() > resource.getLastModified()) {
                            return new NullResourceStreamHandle();
                        }
                    }
                } else {
                    return new NullResourceStreamHandle();
                }
            }
        }
        //Do GET
        return downloadResource(relPath);
    }

    @Override
    public ResourceStreamHandle downloadResource(String relPath) throws IOException {
        return downloadResource(relPath, new NullRequestContext(getRepoPath(relPath)));
    }

    @Override
    public ResourceStreamHandle downloadResource(final String relPath, final RequestContext requestContext)
            throws IOException {
        assert !isOffline() : "Should never be called in offline mode";
        String pathForUrl = convertRequestPathIfNeeded(relPath);
        if (!relPath.equals(pathForUrl)) {
            RepoRequests.logToContext("Remote resource path was translated (%s) due to repository " +
                    "layout differences", pathForUrl);
        }
        Request request = requestContext.getRequest();
        if (request != null) {
            String alternativeRemoteDownloadUrl =
                    request.getParameter(ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL);
            if (StringUtils.isNotBlank(alternativeRemoteDownloadUrl)) {
                RepoRequests.logToContext("Request contains alternative remote resource path ({}=%s)",
                        ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL, alternativeRemoteDownloadUrl);
                pathForUrl = alternativeRemoteDownloadUrl;
            }
        }

        RepoRequests.logToContext("Appending matrix params to remote request URL");
        pathForUrl += buildRequestMatrixParams(requestContext.getProperties());
        final String fullUrl = appendAndGetUrl(convertRequestPathIfNeeded(pathForUrl));
        RepoRequests.logToContext("Using remote request URL - %s", fullUrl);
        AddonsManager addonsManager = InternalContextHelper.get().beanForType(AddonsManager.class);
        final PluginsAddon pluginAddon = addonsManager.addonByType(PluginsAddon.class);

        final RepoPath repoPath = InternalRepoPathFactory.create(getKey(), pathForUrl);
        final Request requestForPlugins = requestContext.getRequest();
        RepoRequests.logToContext("Executing any BeforeRemoteDownload user plugins that may exist");
        RemoteRequestCtx remoteRequestCtx = new RemoteRequestCtx();
        pluginAddon.execPluginActions(BeforeRemoteDownloadAction.class, remoteRequestCtx, requestForPlugins, repoPath);
        HttpGet method = new HttpGet(HttpUtils.encodeQuery(fullUrl));
        Map<String, String> headers = Maps.newHashMap(whiteListHeaders(requestContext));
        headers.putAll(remoteRequestCtx.getHeaders());
        notifyInterceptorsOnBeforeRemoteHttpMethodExecution(method, headers);
        RepoRequests.logToContext("Executing GET request to %s", fullUrl);
        final CloseableHttpResponse response = executeMethod(method, headers);

        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            //Not found
            IOUtils.closeQuietly(response);
            RepoRequests.logToContext("Received response status %s - throwing exception", statusLine);
            throw new RemoteRequestException("Unable to find " + fullUrl, statusCode, statusLine.getReasonPhrase());
        }
        if (statusCode != HttpStatus.SC_OK) {
            IOUtils.closeQuietly(response);
            RepoRequests.logToContext("Received response status %s - throwing exception", statusLine);
            throw new RemoteRequestException("Error fetching " + fullUrl, statusCode, statusLine.getReasonPhrase());
        }
        //Found
        long contentLength = HttpUtils.getContentLength(response);
        logDownloading(fullUrl, contentLength);
        RepoRequests.logToContext("Downloading content");

        final InputStream is = response.getEntity().getContent();
        verifyContentEncoding(response);
        return new MyRemoteResourceStreamHandle(response, fullUrl, requestForPlugins, pluginAddon, repoPath, is);
    }

    /**
    * Collects InternalArtifactoryRequest headers (explicitly allowed for delegation)
    * to the remote server,
    *
    * @see {@link org.artifactory.api.request.InternalArtifactoryRequest#getDelegationAllowedHeaders()}
    *
    * @param requestContext
     *
    * @return Map<String, String>
    */
    private Map<String, String> whiteListHeaders(RequestContext requestContext) {
        Map<String, String> whiteListedHeaders = Maps.newHashMap();
        Request request = requestContext.getRequest();
        if (request instanceof InternalArtifactoryRequest) {
            Set<String> set = ((InternalArtifactoryRequest)request).getDelegationAllowedHeaders();
            for (Map.Entry<String, String> header :
                    requestContext.getRequest().getHeaders().entrySet()) {
                if (set.contains(header.getKey()))
                    whiteListedHeaders.put(header.getKey(), header.getValue());
            }
        }
        return whiteListedHeaders;
    }

    private void verifyContentEncoding(HttpResponse response) throws IOException {
        if (!ConstantValues.httpAcceptEncodingGzip.getBoolean() && response.getEntity() != null) {
            Header[] contentEncodings = response.getHeaders(HttpHeaders.CONTENT_ENCODING);
            for (Header contentEncoding : contentEncodings) {
                if ("gzip".equalsIgnoreCase(contentEncoding.getValue())) {
                    throw new IOException("Received gzip encoded stream while gzip compressions is disabled");
                }
            }
        }
    }

    private void logDownloading(String fullUrl, long contentLength) {
        if (NamingUtils.isChecksum(fullUrl)) {
            log.debug("{} downloading {} {} ", this, fullUrl,
                    contentLength >= 0 ? StorageUnit.toReadableString(contentLength) : "Unknown content length");
        } else {
            log.info("{} downloading {} {} ", this, fullUrl,
                    contentLength >= 0 ? StorageUnit.toReadableString(contentLength) : "Unknown content length");
        }
    }

    private void logDownloaded(String fullUrl, int status, BandwidthMonitorInputStream bmis) {
        String statusMsg = status == 200 ? "" : "status='" + (status > 0 ? status : "unknown") + "' ";
        String summary =
                statusMsg + StorageUnit.toReadableString(bmis.getTotalBytesRead()) + " at " +
                        StorageUnit.format(StorageUnit.KB.fromBytes(bmis.getBytesPerSec())) + " KB/sec";
        if (NamingUtils.isChecksum(fullUrl)) {
            log.debug("{} downloaded  {} {}", this, fullUrl, summary);
        } else {
            log.info("{} downloaded  {} {}", this, fullUrl, summary);
        }
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response.
     * This method allows to override some of the default headers, note that the ARTIFACTORY_ORIGINATED, the
     * ORIGIN_ARTIFACTORY and the ACCEPT_ENCODING can't be overridden
     * The caller to this class is responsible to close the response.
     *
     * @param method Method to execute
     * @param extraHeaders Extra headers to add to the remote server request
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public CloseableHttpResponse executeMethod(HttpRequestBase method, Map<String, String> extraHeaders)
            throws IOException {
        return this.executeMethod(method, null, extraHeaders);
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response.
     * The caller to this class is responsible to close the response.
     *
     * @param method Method to execute
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public CloseableHttpResponse executeMethod(HttpRequestBase method) throws IOException {
        return executeMethod(method, null, null);
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response.
     * The caller to this class is responsible to close the response.
     *
     * @param method  Method to execute
     * @param context The request context for execution state
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public CloseableHttpResponse executeMethod(HttpRequestBase method, @Nullable HttpContext context)
            throws IOException {
        return executeMethod(method, context, null);
    }

    /**
     * Executes an HTTP method using the repository client and returns the http response.
     * This method allows to override some of the default headers, note that the ARTIFACTORY_ORIGINATED, the
     * ORIGIN_ARTIFACTORY and the ACCEPT_ENCODING can't be overridden
     * The caller to this class is responsible to close the response.
     *
     * @param method  Method to execute
     * @param context The request context for execution state
     * @param extraHeaders Extra headers to add to the remote server request
     * @return The http response.
     * @throws IOException If the repository is offline or if any error occurs during the execution
     */
    public CloseableHttpResponse executeMethod(HttpRequestBase method, @Nullable HttpContext context,
            Map<String, String> extraHeaders)
            throws IOException {
        addDefaultHeadersAndQueryParams(method,extraHeaders);
        return getHttpClient().execute(method, context);
    }

    @Override
    protected RepoResource retrieveInfo(String path, boolean folder, @Nullable RequestContext context) {
        assert !isOffline() : "Should never be called in offline mode";
        RepoPath repoPath = InternalRepoPathFactory.create(this.getKey(), path, folder);

        String fullUrl = assembleRetrieveInfoUrl(path, context);
        Map<String, String> headers = Maps.newHashMap();
        HttpRequestBase method;
        boolean replaceHeadWithGet = false;
        String methodType = "HEAD";
        if (context != null && StringUtils.isNotBlank(context.getRequest().getParameter(
                ArtifactoryRequest.PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET))) {
            replaceHeadWithGet = Boolean.valueOf(context.getRequest().getParameter(
                    ArtifactoryRequest.PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET));
        }
        if (replaceHeadWithGet) {
            log.debug("Param " + ArtifactoryRequest.PARAM_REPLACE_HEAD_IN_RETRIEVE_INFO_WITH_GET + " found in request" +
                    " context, switching HEAD with GET request");
            methodType = "GET";
            method = new HttpGet(HttpUtils.encodeQuery(fullUrl));
        } else {
            method = new HttpHead(HttpUtils.encodeQuery(fullUrl));
        }
        RepoRequests.logToContext("Executing %s request to %s", methodType, fullUrl);
        CloseableHttpResponse response = null;
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            notifyInterceptorsOnBeforeRemoteHttpMethodExecution(method, headers);
            response = executeMethod(method, httpClientContext, headers);
            return handleGetInfoResponse(repoPath, method, response, httpClientContext, context);
        } catch (IOException e) {
            String exceptionMessage = HttpClientUtils.getErrorMessage(e);
            RepoRequests.logToContext("Failed to execute %s request: %s", methodType, exceptionMessage);
            throw new RuntimeException("Failed retrieving resource from " + fullUrl + ": " + exceptionMessage, e);
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    protected String assembleRetrieveInfoUrl(String path, RequestContext context) {
        String pathForUrl = convertRequestPathIfNeeded(path);
        if (!path.equals(pathForUrl)) {
            RepoRequests.logToContext("Remote resource path was translated (%s) due to repository " +
                    "layout differences", pathForUrl);
        }
        boolean validContext = context != null;
        if (validContext) {
            Request request = context.getRequest();
            if (request != null) {
                String alternativeRemoteDownloadUrl =
                        request.getParameter(ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL);
                if (StringUtils.isNotBlank(alternativeRemoteDownloadUrl)) {
                    RepoRequests.logToContext("Request contains alternative remote resource path ({}=%s)",
                            ArtifactoryRequest.PARAM_ALTERNATIVE_REMOTE_DOWNLOAD_URL, alternativeRemoteDownloadUrl);
                    pathForUrl = alternativeRemoteDownloadUrl;
                }
            }
        }

        String fullUrl = appendAndGetUrl(pathForUrl);
        if (validContext) {
            RepoRequests.logToContext("Appending matrix params to remote request URL");
            Properties properties = context.getProperties();
            fullUrl += buildRequestMatrixParams(properties);
        }
        RepoRequests.logToContext("Using remote request URL - %s", fullUrl);
        return fullUrl;
    }

    /**
     * Notice: for use with HEAD method, no content is expected in the response.
     * Process the remote repository's response and construct a repository resource.
     *
     * @param repoPath       of requested resource
     * @param method         executed {@link org.apache.http.client.methods.HttpHead} from which to process the response.
     * @param response       The response to the get info request
     * @param context
     * @param requestContext
     * @return
     */
    protected RepoResource handleGetInfoResponse(RepoPath repoPath, HttpRequestBase method,
            CloseableHttpResponse response, @Nullable HttpClientContext context, RequestContext requestContext) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            RepoRequests.logToContext("Received status 404 (message: %s) on remote info request - returning unfound " +
                    "resource", response.getStatusLine().getReasonPhrase());
            return new UnfoundRepoResource(repoPath, response.getStatusLine().getReasonPhrase());
        }

        if (!isDisableFolderRedirectAssertion(requestContext)) {
            assertNoRedirectToFolder(repoPath, context);
        }

        // Some servers may return 204 instead of 200
        if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NO_CONTENT) {
            RepoRequests.logToContext("Received status {} (message: %s) on remote info request - returning unfound " +
                    "resource", statusCode, response.getStatusLine().getReasonPhrase());
            // send back unfound resource with 404 status
            return new UnfoundRepoResource(repoPath, response.getStatusLine().getReasonPhrase());
        }

        long lastModified = getLastModified(response);
        RepoRequests.logToContext("Found remote resource with last modified time - %s",
                new Date(lastModified).toString());

        long contentLength = HttpUtils.getContentLength(response);
        if (contentLength != -1) {
            RepoRequests.logToContext("Found remote resource with content length - %s", contentLength);
        }

        // if status is 204 and length is not 0 then the remote server is doing something wrong
        if (statusCode == HttpStatus.SC_NO_CONTENT && contentLength > 0) {
            // send back unfound resource with 404 status
            RepoRequests.logToContext("Received status {} (message: %s) on remote info request - returning unfound " +
                    "resource", statusCode, response.getStatusLine().getReasonPhrase());
            return new UnfoundRepoResource(repoPath, response.getStatusLine().getReasonPhrase());
        }

        Set<ChecksumInfo> checksums = getChecksums(response);
        if (!checksums.isEmpty()) {
            RepoRequests.logToContext("Found remote resource with checksums - %s", checksums);
        }

        String originalPath = repoPath.getPath();
        String filename = getFilename(method, originalPath);
        if (StringUtils.isNotBlank(filename)) {
            RepoRequests.logToContext("Found remote resource with filename header - %s", filename);
            if (NamingUtils.isMetadata(originalPath)) {
                String originalPathStrippedOfMetadata = NamingUtils.getMetadataParentPath(originalPath);
                String originalPathWithMetadataNameFromHeader =
                        NamingUtils.getMetadataPath(originalPathStrippedOfMetadata, filename);
                repoPath = InternalRepoPathFactory.create(repoPath.getRepoKey(),
                        originalPathWithMetadataNameFromHeader);
            } else {
                repoPath = InternalRepoPathFactory.create(repoPath.getParent(), filename);
            }
        }

        RepoRequests.logToContext("Returning found remote resource info");
        RepoResource res = new RemoteRepoResource(repoPath, lastModified, contentLength, checksums, response.getAllHeaders());
        return res;
    }

    private boolean isDisableFolderRedirectAssertion(RequestContext context) {
        if (context != null) {
            String disableFolderRedirectAssertion = context.getRequest().getParameter(
                    ArtifactoryRequest.PARAM_FOLDER_REDIRECT_ASSERTION);
            if (StringUtils.isNotBlank(disableFolderRedirectAssertion) && Boolean.valueOf(
                    disableFolderRedirectAssertion)) {
                // Do not perform in case of parameter provided
                RepoRequests.logToContext("Folder redirect assertion is disabled for internal download request");
                return true;
            }
        }

        return false;
    }

    private void assertNoRedirectToFolder(RepoPath repoPath, HttpClientContext context) {
        if (context != null) {
            // if redirected, check that the last redirect URL doesn't end with '/' (we assume those are directory paths)
            List<URI> redirects = context.getRedirectLocations();
            if (CollectionUtils.notNullOrEmpty(redirects)) {
                URI lastDestination = redirects.get(redirects.size() - 1);
                if (lastDestination != null && lastDestination.getPath().endsWith("/")) {
                    RepoRequests.logToContext(
                            "Remote info request was redirected to a directory - returning unfound resource");
                    throw new FileExpectedException(new RepoPathImpl(repoPath.getRepoKey(), repoPath.getPath(), true));
                }
            }
        }
    }

    protected CloseableHttpClient createHttpClient() {
        String password = CryptoHelper.decryptIfNeeded(getPassword());
        return new HttpClientConfigurator()
                .hostFromUrl(getUrl())
                .defaultMaxConnectionsPerHost(50)
                .maxTotalConnections(50)
                .connectionTimeout(getSocketTimeoutMillis())
                .soTimeout(getSocketTimeoutMillis())
                .handleGzipResponse(handleGzipResponse)
                .staleCheckingEnabled(true)
                .retry(1, false)
                .localAddress(getLocalAddress())
                .proxy(getProxy())
                .authentication(getUsername(), password, isAllowAnyHostAuth())
                .enableCookieManagement(isEnableCookieManagement())
                .enableTokenAuthentication(isEnableTokenAuthentication(), getUsername(), password)
                .getClient();
    }

    private RepoResource getCachedResource(String relPath) {
        LocalCacheRepo cache = getLocalCacheRepo();
        final NullRequestContext context = new NullRequestContext(getRepoPath(relPath));
        RepoResource cachedResource = cache.getInfo(context);
        return cachedResource;
    }

    /**
     * Adds default headers and extra headers to the HttpRequest method
     * The extra headers are unique headers that should be added to the remote server request according to special
     * requirement example : user adds extra headers through the User Plugin (BeforeRemoteDownloadAction)
     * @param method Method to execute
     * @param extraHeaders Extra headers to add to the remote server request
     */
    @SuppressWarnings({"deprecation"})
    private void addDefaultHeadersAndQueryParams(HttpRequestBase method, Map<String, String> extraHeaders) {
        //Explicitly force keep alive
        method.setHeader(HttpHeaders.CONNECTION, "Keep-Alive");

        //Add the current requester host id
        Set<String> originatedHeaders = RepoRequests.getOriginatedHeaders();
        for (String originatedHeader : originatedHeaders) {
            method.addHeader(ARTIFACTORY_ORIGINATED, originatedHeader);
        }
        String hostId = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
                HaCommonAddon.class).getHostId();
        // Add the extra headers to the remote request
        if (extraHeaders != null) {
            for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
                boolean isReservedKey=ARTIFACTORY_ORIGINATED.equals(entry.getKey())||
                                      ORIGIN_ARTIFACTORY.equals(entry.getKey())||
                                      ACCEPT_ENCODING.equals(entry.getKey());
                if( ! isReservedKey ) {
                    method.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
        // Add the default artifactory headers, those headers will always override the existing headers if they already exist
        method.addHeader(ARTIFACTORY_ORIGINATED, hostId);

        //For backwards compatibility
        method.setHeader(ORIGIN_ARTIFACTORY, hostId);

        //Set gzip encoding
        if (handleGzipResponse) {
            method.addHeader(ACCEPT_ENCODING, "gzip");
        }

        // Set custom query params
        String queryParams = getDescriptor().getQueryParams();
        if (StringUtils.isNotBlank(queryParams)) {
            String url = method.getURI().toString();
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            url += HttpUtils.encodeQuery(queryParams);
            method.setURI(URI.create(url));
        }
    }

    private String getFilename(HttpRequestBase method, String originalPath) {
        // Skip filename parsing if we are not dealing with latest maven non-unique snapshot request
        if (!isRequestForLatestMavenSnapshot(originalPath)) {
            return null;
        }

        // Try our custom X-Artifactory-Filename header
        Header filenameHeader = method.getFirstHeader(ArtifactoryRequest.FILE_NAME);
        if (filenameHeader != null) {
            String filenameString = filenameHeader.getValue();
            try {
                return URLDecoder.decode(filenameString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn("Unable to decode '{}' header '{}', returning un-encoded value.",
                        ArtifactoryRequest.FILE_NAME, filenameString);
                return filenameString;
            }
        }

        // Didn't find any filename, return null
        return null;
    }

    private boolean isRequestForLatestMavenSnapshot(String originalPath) {
        if (ConstantValues.requestDisableVersionTokens.getBoolean()) {
            return false;
        }

        if (!getDescriptor().isMavenRepoLayout()) {
            return false;
        }

        if (!MavenNaming.isNonUniqueSnapshot(originalPath)) {
            return false;
        }

        return true;
    }

    @Override
    protected List<RemoteItem> getChildUrls(String dirUrl) throws IOException {
        if (remoteBrowser == null) {
            initRemoteRepositoryBrowser();
        }
        return remoteBrowser.listContent(dirUrl);
    }

    /**
     * Converts the given path to the remote repo's layout if defined
     *
     * @param path Path to convert
     * @return Converted path if required and conversion was successful, given path if not
     */
    public String convertRequestPathIfNeeded(String path) {
        HttpRepoDescriptor descriptor = getDescriptor();
        return layoutsCoreAddon.translateArtifactPath(descriptor.getRepoLayout(), descriptor.getRemoteRepoLayout(),
                path);
    }

    @Override
    protected void putOffline() {
        long assumedOfflinePeriodSecs = getDescriptor().getAssumedOfflinePeriodSecs();
        if (assumedOfflinePeriodSecs <= 0) {
            return;
        }
        // schedule the offline thread to run immediately
        //scheduler.schedule(new OfflineCheckCallable(), 0, TimeUnit.MILLISECONDS);
        synchronized (onlineMonitorSync) {
            if (onlineMonitorThread != null) {
                if (onlineMonitorThread.isAlive()) {
                    return;
                }
                log.debug("Online monitor thread exists but not alive");
            }
            onlineMonitorThread = new Thread(new OnlineMonitorRunnable(), "online-monitor-" + getKey());
            onlineMonitorThread.setDaemon(true);
            log.debug("Online monitor starting {}", onlineMonitorThread.getName());
            onlineMonitorThread.start();
        }
    }

    @Override
    public void resetAssumedOffline() {
        synchronized (onlineMonitorSync) {
            log.info("Resetting assumed offline status");
            stopOfflineCheckThread();
            assumedOffline = false;
        }
    }

    private void stopOfflineCheckThread() {
        synchronized (onlineMonitorSync) {
            if (onlineMonitorThread != null) {
                log.debug("Online monitor stopping {}", onlineMonitorThread.getName());
                onlineMonitorThread.interrupt();
                onlineMonitorThread = null;
            }
        }
    }

    protected CloseableHttpClient getHttpClient() {
        if (client == null) {
            throw new IllegalStateException("Repo is offline. Cannot use the HTTP client.");
        }
        return client;
    }

    private class OnlineMonitorRunnable implements Runnable {
        /**
         * max attempts until reaching the maximum wait time
         */
        private static final int MAX_FAILED_ATTEMPTS = 10;

        /**
         * Failed requests counter
         */
        private int failedAttempts = 0;

        @Override
        public void run() {
            log.debug("Online monitor started for {}", getKey());
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    if (checkOnline()) {
                        if (assumedOffline) {
                            log.info("{} is back online!", getKey());
                        }
                        assumedOffline = false;
                        onlineMonitorThread = null;
                        return;
                    }
                    if (!assumedOffline) {
                        log.info("{} is inaccessible. Setting as offline!", getKey());
                        assumedOffline = true;
                    }
                    long nextOnlineCheckDelay = calculateNextOnlineCheckDelay();
                    nextOnlineCheckMillis = System.currentTimeMillis() + nextOnlineCheckDelay;
                    log.debug("Online monitor sleeping for {} millis", nextOnlineCheckDelay);
                    Thread.sleep(nextOnlineCheckDelay);
                } catch (InterruptedException e) {
                    log.debug("Online monitor interrupted");
                    Thread.interrupted();
                    return;
                }
            }
        }

        private long calculateNextOnlineCheckDelay() {
            long maxFailureCacheSecs = getDescriptor().getAssumedOfflinePeriodSecs();
            long maxFailureCacheMillis = TimeUnit.SECONDS.toMillis(maxFailureCacheSecs);    // always >= 1000

            long nextOnlineCheckDelayMillis;
            failedAttempts++;
            if (failedAttempts < MAX_FAILED_ATTEMPTS) {
                if (maxFailureCacheSecs / MAX_FAILED_ATTEMPTS < 2) {
                    long failurePenaltyMillis = maxFailureCacheMillis / MAX_FAILED_ATTEMPTS;
                    nextOnlineCheckDelayMillis = failedAttempts * failurePenaltyMillis;
                } else {
                    // exponential delay
                    // calculate the base of the exponential equation based on the MAX_FAILED_ATTEMPTS and max offline period
                    // BASE pow MAX_FAILED_ATTEMPTS = MAX_DELAY ==> BASE = MAX_DELAY pow 1/MAX_FAILED_ATTEMPTS
                    double base = Math.pow(maxFailureCacheMillis, 1.0 / (double) MAX_FAILED_ATTEMPTS);
                    nextOnlineCheckDelayMillis = (long) Math.pow(base, failedAttempts);
                    // in any case don't attempt too rapidly
                    nextOnlineCheckDelayMillis = Math.max(100, nextOnlineCheckDelayMillis);
                }
            } else {
                nextOnlineCheckDelayMillis = maxFailureCacheMillis;
            }
            return nextOnlineCheckDelayMillis;
        }

        private boolean checkOnline() {
            // always test with url trailing slash
            String url = PathUtils.addTrailingSlash(getDescriptor().getUrl());
            HttpGet getMethod = new HttpGet(HttpUtils.encodeQuery(url));
            try (CloseableHttpResponse response = getHttpClient().execute(getMethod)) {
                log.debug("Online monitor checking URL: {}", url);
                StatusLine status = response.getStatusLine();
                log.debug("Online monitor http method completed with no exception: {}: {}",
                        status.getStatusCode(), status.getReasonPhrase());
                //TODO: RTFACT-6528 [by YS] consider putting offline if status > 500 && status < 600
                // no exception - consider back online
                return true;
            } catch (IOException e) {
                log.debug("Online monitor http method failed: {}: {}", e.getClass().getName(), e.getMessage());
            }
            return false;
        }
    }

    public class MyRemoteResourceStreamHandle extends RemoteResourceStreamHandle {
        private final BandwidthMonitorInputStream bmis;
        private final String remoteIp;
        private CloseableHttpResponse response;
        private String fullUrl;
        private Request requestForPlugins;
        private PluginsAddon pluginAddon;
        private RepoPath repoPath;

        public MyRemoteResourceStreamHandle(
                CloseableHttpResponse response, String fullUrl, Request requestForPlugins, PluginsAddon pluginAddon,
                RepoPath repoPath, InputStream is) {
            this.response = response;
            this.fullUrl = fullUrl;
            this.requestForPlugins = requestForPlugins;
            this.pluginAddon = pluginAddon;
            this.repoPath = repoPath;
            this.bmis = new BandwidthMonitorInputStream(is);
            TrafficService trafficService = ContextHelper.get().beanForType(TrafficService.class);
            if (trafficService.isActive()) {
                this.remoteIp = HttpUtils.resolveResponseRemoteAddress(response);
            } else {
                this.remoteIp = StringUtils.EMPTY;
            }
        }

        @Override
        public InputStream getInputStream() {
            return bmis;
        }

        @Override
        public long getSize() {
            return -1;
        }

        @Override
        public void close() {
            IOUtils.closeQuietly(bmis);
            IOUtils.closeQuietly(response);
            StatusLine statusLine = response.getStatusLine();

            Throwable throwable = getThrowable();
            if (throwable != null) {
                String exceptionMessage = HttpClientUtils.getErrorMessage(throwable);
                log.error("{}: Failed to download '{}'. Received status code {} and caught exception: {}",
                        HttpRepo.this, fullUrl, statusLine != null ? statusLine.getStatusCode() : "unknown",
                        exceptionMessage);
                String downLoadSummary =
                        StorageUnit.toReadableString(bmis.getTotalBytesRead()) + " at " +
                                StorageUnit.format(StorageUnit.KB.fromBytes(bmis.getBytesPerSec())) + " KB/sec";
                log.debug("Failed to download '{}'. Download summary: {}", fullUrl, downLoadSummary, throwable);
                RepoRequests.logToContext("Failed to download: %s", exceptionMessage);
            } else {
                int status = statusLine != null ? statusLine.getStatusCode() : 0;
                logDownloaded(fullUrl, status, bmis);
                RepoRequests.logToContext("Downloaded content");
            }
            RepoRequests.logToContext("Executing any AfterRemoteDownload user plugins that may exist");
            pluginAddon.execPluginActions(AfterRemoteDownloadAction.class, null, requestForPlugins, repoPath);
            RepoRequests.logToContext("Executed all AfterRemoteDownload user plugins");
        }

        public String getRemoteIp() {
            return remoteIp;
        }
    }
}
