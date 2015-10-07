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

package org.artifactory.repo.remote.browse;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.RestCoreAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.repo.HttpRepo;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.artifactory.repo.remote.browse.S3RepositorySecuredHelper.getPrefix;

/**
 * Support browsing Amazon S3 repositories.<p/>
 * For more details see: <a href="http://docs.amazonwebservices.com/AmazonS3/latest/API/APIRest.html">Amazon S3 API</a>.
 * <p/>
 * Bucket list API: <a href="http://docs.amazonwebservices.com/AmazonS3/latest/API/RESTBucketGET.html">Bucket List API</a>
 *
 * @author Yossi Shaul
 */
public class S3RepositoryBrowser extends RemoteRepositoryBrowser {
    private static final Logger log = LoggerFactory.getLogger(S3RepositoryBrowser.class);

    private static final String ERROR_CODE_NOSUCHKEY = "NoSuchKey";
    private static final String HEADER_S3_REQUEST_ID = "x-amz-request-id";
    private static final String INVALID_ARGUMENT = "InvalidArgument";
    private static final String UNSUPPORTED_AUTHORIZATION_TYPE = "Unsupported Authorization Type";

    private HttpRepo httpRepo;

    /**
     * The root URL of the S3 repository. This is the bucket url on which list requests should be done.
     */
    private String rootUrl;

    /**
     * Indicates this URL is S3 secured
     */
    private boolean secured;

    public S3RepositoryBrowser(HttpExecutor client) {
        super(client);
    }

    public S3RepositoryBrowser(HttpExecutor client, HttpRepo httpRepo) {
        super(client);
        this.httpRepo = httpRepo;
    }

    @Override
    public List<RemoteItem> listContent(String url) throws IOException {
        if (rootUrl == null) {
            detectRootUrl(url);
        }
        String s3Url = buildS3RequestUrl(url);
        log.debug("Request url: {} S3 url: {}", url, s3Url);
        String result = getFileListContent(s3Url);
        log.debug("S3 result: {}", result);
        return parseResponse(result);
    }

    private String buildS3RequestUrl(String url) {
        url = forceDirectoryUrl(url);
        if (secured) {
            String pfx = getPrefix(url);
            return buildSecuredS3RequestUrl(url, httpRepo, "") + "&prefix=" + pfx + "&delimiter=/";
        }
        // the s3 request should always go to the root and add the rest of the path as the prefix parameter.
        String prefixPath = StringUtils.removeStart(url, rootUrl);
        StringBuilder sb = new StringBuilder(rootUrl).append("?prefix=").append(prefixPath);

        // we assume a file system structure with '/' as the delimiter
        sb.append("&").append("delimiter=/");
        return HttpUtils.encodeQuery(sb.toString());
    }

    /**
     * Detects the bucket url (i.e., root url). The given url is assumed to either point to the root or to "directory"
     * under the root. The most reliable way to get the root is to request non-existing resource and analyze the response.
     *
     * @param url URL to S3 repository
     * @return The root url of the repository (the bucket)
     */
    String detectRootUrl(String url) throws IOException {
        //noinspection RedundantStringConstructorCall
        String copyUrl = new String(url); //defense

        // force non-directory copyUrl. S3 returns 200 for directory paths
        url = url.endsWith("/") ? StringUtils.removeEnd(url, "/") : url;
        // generate a random string to force 404
        String randomString = RandomStringUtils.randomAlphanumeric(16);
        url = url + "/" + randomString;
        HttpGet method = new HttpGet(url);
        try (CloseableHttpResponse response = client.executeMethod(method)) {
            // most likely to get 404 if the repository exists
            assertSizeLimit(url, response);
            String responseString = IOUtils.toString(
                    HttpUtils.getResponseBody(response), Charsets.UTF_8.name());
            log.debug("Detect S3 root url got response code {} with content: {}",
                    response.getStatusLine().getStatusCode(), responseString);
            Document doc = XmlUtils.parse(responseString);
            Element root = doc.getRootElement();
            String errorCode = root.getChildText("Code", root.getNamespace());
            if (ERROR_CODE_NOSUCHKEY.equals(errorCode)) {
                String relativePath = root.getChildText("Key", root.getNamespace());
                rootUrl = StringUtils.removeEnd(url, relativePath);
            } else if (INVALID_ARGUMENT.equals(errorCode)) {
                if (isPro()) {
                    String message = root.getChildText("Message");
                    if (UNSUPPORTED_AUTHORIZATION_TYPE.equals(message)) {
                        rootUrl = detectRootUrlSecured(copyUrl);
                    }
                } else {
                    log.warn("Browsing secured S3 requires Artifactory Pro"); //TODO [mamo] should inform otherwise?
                }
            } else {
                throw new IOException("Couldn't detect S3 root URL. Unknown error code: " + errorCode);
            }
        }
        log.debug("Detected S3 root URL: {}", rootUrl);
        return rootUrl;
    }

    private String detectRootUrlSecured(String url) throws IOException {
        String securedUrl = buildSecuredS3RequestUrl(url, httpRepo, "") +
                "&prefix=" + getPrefix(url) + "&delimiter=/&max-keys=1";
        HttpGet method = new HttpGet(securedUrl);
        try (CloseableHttpResponse response = client.executeMethod(method)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String rootUrl = StringUtils.removeEnd(httpRepo.getUrl(), getPrefix(httpRepo.getUrl()));
                if (!rootUrl.endsWith("/")) {
                    rootUrl += "/";
                }
                secured = true;
                return rootUrl;
            }
        }
        return null;
    }

    /**
     * @param url    The URL to check
     * @param client Http client to use
     * @return True if the url points to an S3 repository.
     */
    public static boolean isS3Repository(String url, CloseableHttpClient client) {
        HttpHead headMethod = new HttpHead(HttpUtils.encodeQuery(url));
        try (CloseableHttpResponse response = client.execute(headMethod)) {
            Header s3RequestId = response.getFirstHeader(HEADER_S3_REQUEST_ID);
            return s3RequestId != null;
        } catch (IOException e) {
            log.debug("Failed detecting S3 repository: " + e.getMessage(), e);
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private List<RemoteItem> parseResponse(String content) {
        List<RemoteItem> items = Lists.newArrayList();
        Document document = XmlUtils.parse(content);
        Element root = document.getRootElement();
        Namespace ns = root.getNamespace();
        String prefix = root.getChildText("Prefix", ns);

        // retrieve folders
        List<Element> folders = root.getChildren("CommonPrefixes", ns);
        for (Element folder : folders) {
            String directoryPath = folder.getChildText("Prefix", ns);
            String folderName = StringUtils.removeStart(directoryPath, prefix);
            if (StringUtils.isNotBlank(folderName)) {
                if (secured) {
                    directoryPath = StringUtils.removeStart(directoryPath, getPrefix(rootUrl));
                }
                items.add(new RemoteItem(rootUrl + directoryPath, true));
            }
        }

        // retrieve files
        List<Element> files = root.getChildren("Contents", ns);
        for (Element element : files) {
            String filePath = element.getChildText("Key", ns);
            String fileName = StringUtils.removeStart(filePath, prefix);
            if (StringUtils.isNotBlank(fileName) && !folderDirectoryWithSameNameExists(fileName, items)) {
                // the date format is of the form yyyy-mm-ddThh:mm:ss.timezone, e.g., 2009-02-03T16:45:09.000Z
                String sizeStr = element.getChildText("Size", ns);
                long size = sizeStr == null ? 0 : Long.parseLong(sizeStr);
                String lastModifiedStr = element.getChildText("LastModified", ns);
                long lastModified =
                        lastModifiedStr == null ? 0 : ISODateTimeFormat.dateTime().parseMillis(lastModifiedStr);
                if (secured) {
                    RemoteItem remoteItem = new RemoteItem(rootUrl + filePath, false, size, lastModified);
                    String filePath2 = StringUtils.removeStart(filePath, getPrefix(rootUrl));
                    String url = rootUrl + filePath2;
                    String securedPath = buildSecuredS3RequestUrl(url, httpRepo, getPrefix(url));
                    remoteItem.setEffectiveUrl(securedPath);
                    items.add(remoteItem);
                } else {
                    items.add(new RemoteItem(rootUrl + filePath, false, size, lastModified));
                }
            }
        }

        return items;
    }

    /**
     * some s3 repositories (e.g., terracotta http://repo.terracotta.org/?delimiter=/&prefix=maven2/) has files and
     * folders with the same name (for instance file named 'org' and directory named 'org/' under the same directory)
     * in such a case we prefer the directory and don't return the file
     */
    private boolean folderDirectoryWithSameNameExists(String fileName, List<RemoteItem> items) {
        for (RemoteItem item : items) {
            if (item.getName().equals(fileName)) {
                log.debug("Found file with the same name of a directory: {}", item.getUrl());
                return true;
            }
        }
        return false;
    }

    protected boolean isPro() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        RestCoreAddon restCoreAddon = addonsManager.addonByType(RestCoreAddon.class);
        return !restCoreAddon.isDefault();
    }

    private String buildSecuredS3RequestUrl(String url, HttpRepo httpRepo, String prefix) {
        long expiration = new DateTime().plusSeconds((int) httpRepo.getRetrievalCachePeriodSecs()).getMillis();
        return S3RepositorySecuredHelper.buildSecuredS3RequestUrl(url, prefix, httpRepo, expiration);
    }
}
