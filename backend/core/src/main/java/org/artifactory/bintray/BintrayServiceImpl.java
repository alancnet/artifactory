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

package org.artifactory.bintray;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.MultipleBintrayCallException;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.handle.Bintray;
import com.jfrog.bintray.client.api.handle.PackageHandle;
import com.jfrog.bintray.client.api.handle.RepositoryHandle;
import com.jfrog.bintray.client.api.handle.SubjectHandle;
import com.jfrog.bintray.client.api.handle.VersionHandle;
import com.jfrog.bintray.client.impl.BintrayClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.bintray.BintrayItemInfo;
import org.artifactory.api.bintray.BintrayPackageInfo;
import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.BintrayUploadInfo;
import org.artifactory.api.bintray.BintrayUser;
import org.artifactory.api.bintray.Repo;
import org.artifactory.api.bintray.RepoPackage;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.mail.MailService;
import org.artifactory.api.search.BintrayItemSearchResults;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.util.AqlSearchablePath;
import org.artifactory.aql.util.AqlUtils;
import org.artifactory.build.ArtifactoryBuildArtifact;
import org.artifactory.build.BuildServiceUtils;
import org.artifactory.build.InternalBuildService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.StatusEntry;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.md.PropertiesFactory;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.sapi.search.VfsQueryService;
import org.artifactory.security.UserInfo;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.util.CollectionUtils;
import org.artifactory.util.EmailException;
import org.artifactory.util.HttpClientConfigurator;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.BintrayUploadInfoOverride;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.artifactory.aql.api.internal.AqlBase.and;
import static org.artifactory.build.BuildServiceUtils.VerifierLogLevel;

/**
 * @author Shay Yaakov
 */
@Service
public class BintrayServiceImpl implements BintrayService {
    private static final Logger log = LoggerFactory.getLogger(BintrayServiceImpl.class);
    private static final String RANGE_LIMIT_TOTAL = "X-RangeLimit-Total";
    @Autowired
    protected CentralConfigService centralConfig;
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    private InternalRepositoryService repoService;
    @Autowired
    private BinaryStore binaryStore;
    @Autowired
    private InternalBuildService buildService;
    @Autowired
    private MailService mailService;
    @Autowired
    private AddonsManager addonsManager;
    @Autowired
    private VfsQueryService vfsQueryService;
    @Autowired
    private AqlService aqlService;

    /**
     * Bintray Rest API request Cache
     */
    private Map<String, BintrayPackageInfo> bintrayPackageCache;

    public BintrayServiceImpl() {
        bintrayPackageCache = initCache(500, TimeUnit.HOURS.toSeconds(1), false);
    }

    @Override
    public BasicStatusHolder pushArtifact(ItemInfo itemInfo, BintrayParams bintrayParams,
            @Nullable Map<String, String> headersMap) throws IOException {
        BasicStatusHolder status = new BasicStatusHolder();

        try (CloseableHttpClient client = createHTTPClient()) {
            if (itemInfo.isFolder()) {
                List<ItemInfo> children = repoService.getChildrenDeeply(itemInfo.getRepoPath());
                for (ItemInfo child : children) {
                    if (!child.isFolder()) {
                        performPush(client, (FileInfo) itemInfo, bintrayParams, status, headersMap);
                    }
                }
            } else {
                performPush(client, (FileInfo) itemInfo, bintrayParams, status, headersMap);
            }
        }

        return status;
    }

    @Override
    public BasicStatusHolder pushBuild(Build build, BintrayParams bintrayParams,
            @Nullable Map<String, String> headersMap) throws IOException {
        BasicStatusHolder status = new BasicStatusHolder();
        String buildNameAndNumber = build.getName() + ":" + build.getNumber();
        status.status("Starting pushing build '" + buildNameAndNumber + "' to Bintray.", log);
        List<FileInfo> artifactsToPush = collectBuildArtifactsToPush(build, null);
        try (CloseableHttpClient client = createHTTPClient()) {
            status.status("Found " + artifactsToPush.size() + " artifacts to push.", log);
            for (FileInfo fileInfo : artifactsToPush) {
                bintrayParams.setPath(fileInfo.getRelPath());
                if (bintrayParams.isUseExistingProps()) {
                    BintrayParams paramsFromProperties = createParamsFromProperties(fileInfo.getRepoPath());
                    bintrayParams.setRepo(paramsFromProperties.getRepo());
                    bintrayParams.setPackageId(paramsFromProperties.getPackageId());
                    bintrayParams.setVersion(paramsFromProperties.getVersion());
                    bintrayParams.setPath(paramsFromProperties.getPath());
                }
                try {
                    performPush(client, fileInfo, bintrayParams, status, headersMap);
                } catch (IOException e) {
                    sendBuildPushNotification(status, buildNameAndNumber);
                    throw e;
                }
            }
        }

        String message = String.format("Finished pushing build '%s' to Bintray with %s errors and %s warnings.",
                buildNameAndNumber, status.getErrors().size(), status.getWarnings().size());
        status.status(message, log);

        if (bintrayParams.isNotify()) {
            sendBuildPushNotification(status, buildNameAndNumber);
        }

        return status;
    }

    private <V> Map<String, V> initCache(int initialCapacity, long expirationSeconds, boolean softValues) {
        CacheBuilder mapMaker = CacheBuilder.newBuilder().initialCapacity(initialCapacity);
        if (expirationSeconds >= 0) {
            mapMaker.expireAfterWrite(expirationSeconds, TimeUnit.SECONDS);
        }
        if (softValues) {
            mapMaker.softValues();
        }

        //noinspection unchecked
        return mapMaker.build().asMap();
    }

    @Override
    public void executeAsyncPushBuild(Build build, BintrayParams bintrayParams,
            @Nullable Map<String, String> headersMap) {
        try {
            pushBuild(build, bintrayParams, headersMap);
        } catch (IOException e) {
            log.error("Push failed with exception: " + e.getMessage());
        }
    }

    private void sendBuildPushNotification(BasicStatusHolder statusHolder, String buildNameAndNumber)
            throws IOException {
        log.info("Sending logs for push build '{}' by mail.", buildNameAndNumber);
        InputStream stream = null;
        try {
            //Get message body from properties and substitute variables
            stream = getClass().getResourceAsStream("/org/artifactory/email/messages/bintrayPushBuild.properties");
            ResourceBundle resourceBundle = new PropertyResourceBundle(stream);
            String body = resourceBundle.getString("body");
            String logBlock = getLogBlock(statusHolder);
            UserInfo currentUser = getCurrentUser();
            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                if (StringUtils.isBlank(userEmail)) {
                    log.warn("Couldn't find valid email address. Skipping push build to bintray email notification");
                } else {
                    log.debug("Sending push build to Bintray notification to '{}'.", userEmail);
                    String message = MessageFormat.format(body, logBlock);
                    mailService.sendMail(new String[]{userEmail}, "Push Build to Bintray Report", message);
                }
            }
        } catch (EmailException e) {
            log.error("Error while notification of: '" + buildNameAndNumber + "' messages.", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private UserInfo getCurrentUser() {
        // currentUser() is not enough since the user might have changed his details from the profile page so the
        // database has the real details while currentUser() is the authenticated user which was not updated.
        try {
            String username = userGroupService.currentUser().getUsername();
            UserInfo userInfo = userGroupService.findUser(username);
            return userInfo;
        } catch (UsernameNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns an HTML list block of messages extracted from the status holder
     *
     * @param statusHolder Status holder containing messages that should be included in the notification
     * @return HTML list block
     */
    private String getLogBlock(BasicStatusHolder statusHolder) {
        StringBuilder builder = new StringBuilder();

        for (StatusEntry entry : statusHolder.getEntries()) {

            //Make one line per row
            String message = entry.getMessage();
            Throwable throwable = entry.getException();
            if (throwable != null) {
                String throwableMessage = throwable.getMessage();
                if (StringUtils.isNotBlank(throwableMessage)) {
                    message += ": " + throwableMessage;
                }
            }
            builder.append(message).append("<br>");
        }

        builder.append("<p>");

        return builder.toString();
    }

    @Override
    public BintrayParams createParamsFromProperties(RepoPath repoPath) {
        BintrayParams bintrayParams = new BintrayParams();
        Properties properties = repoService.getProperties(repoPath);
        if (properties != null) {
            bintrayParams.setRepo(properties.getFirst(BINTRAY_REPO));
            bintrayParams.setPackageId(properties.getFirst(BINTRAY_PACKAGE));
            bintrayParams.setVersion(properties.getFirst(BINTRAY_VERSION));
            bintrayParams.setPath(properties.getFirst(BINTRAY_PATH));
        }

        return bintrayParams;
    }

    /**
     * Sets file properties with the bintray details after push is successful
     */
    private void createUpdatePropsForPushedArtifacts(List<FileInfo> pushedFiles, BintrayUploadInfo uploadInfo,
            BasicStatusHolder status) {

        boolean canAnnotateAll = true;
        log.debug("Setting properties on pushed artifacts");
        BintrayParams bintrayParams = new BintrayParams();
        String repo = uploadInfo.getPackageDetails().getRepo();
        String pkg = uploadInfo.getPackageDetails().getName();
        String ver = uploadInfo.getVersionDetails().getName();
        String path = uploadInfo.getPackageDetails().getSubject() + "/" + repo + "/" + pkg + "/" + ver + "/";
        bintrayParams.setRepo(repo);
        bintrayParams.setPackageId(pkg);
        bintrayParams.setVersion(ver);
        for (FileInfo info : pushedFiles) {
            bintrayParams.setPath(path + info.getRepoPath().getPath());
            if (authorizationService.canAnnotate(info.getRepoPath())) {
                savePropertiesOnRepoPath(info.getRepoPath(), bintrayParams);
            } else {
                canAnnotateAll = false;
            }
        }
        if (!canAnnotateAll) {
            String message = "You do not have annotate permissions on some or all of the published files in " +
                    "Artifactory. Bintray package and version properties will not be recorded for these files.";
            status.warn(message, log);
        }
    }

    @Override
    public void savePropertiesOnRepoPath(RepoPath repoPath, BintrayParams bintrayParams) {
        Properties properties = repoService.getProperties(repoPath);
        if (properties == null) {
            properties = PropertiesFactory.create();
        }
        properties.replaceValues(BINTRAY_REPO, newArrayList(bintrayParams.getRepo()));
        properties.replaceValues(BINTRAY_PACKAGE, newArrayList(bintrayParams.getPackageId()));
        properties.replaceValues(BINTRAY_VERSION, newArrayList(bintrayParams.getVersion()));
        properties.replaceValues(BINTRAY_PATH, newArrayList(bintrayParams.getPath()));
        repoService.setProperties(repoPath, properties);
    }

    /**
     * Uses the {@link org.artifactory.api.build.BuildService} to retrieve all build artifacts and filters out missing
     * entries (Artifacts that don't exist return a null FileInfo mapping).
     * Logs missing artifacts with level warn
     *
     * @param build  Build to retrieve artifacts for.
     * @param status StatusHolder for logging.
     * @return List of FileInfo objects that represent this build's (found) artifacts
     */
    private List<FileInfo> collectBuildArtifactsToPush(Build build, @Nullable BasicStatusHolder status) {
        status = status == null ? new BasicStatusHolder() : status;
        log.info("Collecting Build artifacts to push for build {}:{}", build.getName(), build.getNumber());
        Set<ArtifactoryBuildArtifact> infos = buildService.getBuildArtifactsFileInfos(build, false, StringUtils.EMPTY);
        BuildServiceUtils.verifyAllArtifactInfosExistInSet(build, true, status, infos, VerifierLogLevel.warn);
        return Lists.newArrayList(BuildServiceUtils.toFileInfoList(infos));
    }

    private void performPush(CloseableHttpClient client, FileInfo fileInfo, BintrayParams bintrayParams,
            BasicStatusHolder status, @Nullable Map<String, String> headersMap) throws IOException {
        if (!bintrayParams.isValid()) {
            String message = String.format("Skipping push for '%s' since one of the Bintray properties is missing.",
                    fileInfo.getRelPath());
            status.warn(message, log);
            return;
        }

        if (!authorizationService.canAnnotate(fileInfo.getRepoPath())) {
            String message = "You do not have annotate permissions on the published files in Artifactory. " +
                    "Bintray package and version properties will not be recorded.";
            status.warn(message, log);
        }

        String path = bintrayParams.getPath();
        status.status("Pushing artifact " + path + " to Bintray.", log);
        String requestUrl = getBaseBintrayApiUrl() + PATH_CONTENT + "/" + bintrayParams.getRepo() + "/"
                + bintrayParams.getPackageId() + "/" + bintrayParams.getVersion() + "/" + path;

        CloseableHttpResponse response = null;
        try {
            InputStream elementInputStream = binaryStore.getBinary(fileInfo.getSha1());
            HttpEntity requestEntity = new InputStreamEntity(elementInputStream, fileInfo.getSize());
            HttpPut putMethod = createPutMethod(requestUrl, headersMap, requestEntity);
            response = client.execute(putMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            String message;
            if (statusCode != HttpStatus.SC_CREATED) {
                message = String.format("Push failed for '%s' with status: %s %s", path, statusCode,
                        response.getStatusLine().getReasonPhrase());
                status.error(message, statusCode, log);
            } else {
                message = String.format(
                        "Successfully pushed '%s' to repo: '%s', package: '%s', version: '%s' in Bintray.",
                        path, bintrayParams.getRepo(), bintrayParams.getPackageId(), bintrayParams.getVersion());
                status.status(message, log);
                if (!bintrayParams.isUseExistingProps()) {
                    savePropertiesOnRepoPath(fileInfo.getRepoPath(), bintrayParams);
                }
            }
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    @Override
    // TODO: [by dan] this is the newer version, based on the bintray-java-client and should replace pushBuild in a later version
    public BasicStatusHolder pushPromotedBuild(Build build, String gpgPassphrase, Boolean gpgSignOverride,
            BintrayUploadInfoOverride override) {
        BasicStatusHolder status = new BasicStatusHolder();
        if (!validCredentialsExist(status)) {
            return status;
        }
        log.info("Gathering information for build: " + build.getName() + " Number: " + build.getNumber());
        BintrayUploadInfo uploadInfo = getUplaodInfoForBuild(build, override, status);
        if (status.hasErrors()) {
            return status;
        }
        //Get artifacts from build and filter out descriptor
        List<FileInfo> artifactsToPush = collectBuildArtifactsToPush(build, status);
        filterOutJsonFileFromArtifactsToPush(artifactsToPush, null, status);

        //No artifacts in build
        if (CollectionUtils.isNullOrEmpty(artifactsToPush)) {
            status.error("No artifacts found to push to Bintray, aborting operation", SC_NOT_FOUND, log);
            return status;
        }

        //Filter artifacts by properties (if exist) in descriptor
        filterBuildArtifactsByDescriptor(uploadInfo, artifactsToPush, status);
        if (status.hasErrors()) {
            return status;
        }

        status.merge(pushVersion(uploadInfo, artifactsToPush, gpgSignOverride, gpgPassphrase));
        return status;
    }

    @Override
    // TODO: [by dan] this is the newer version, based on the bintray-java-client and should replace (or accommodate) pushArtifact in a later version
    public BasicStatusHolder pushVersionFilesAccordingToSpec(FileInfo jsonFile, Boolean gpgSignOverride,
            String gpgPassphrase) {
        BasicStatusHolder status = new BasicStatusHolder();

        if (!validCredentialsExist(status)) {
            return status;
        }
        BintrayUploadInfo uploadInfo = validateUploadInfoFile(jsonFile, status);
        if (status.hasErrors()) {
            return status;
        }

        List<FileInfo> artifactsToPush = collectArtifactsToPushBasedOnDescriptor(jsonFile, uploadInfo, status);
        if (status.hasErrors()) {
            return status;
        }

        status.merge(pushVersion(uploadInfo, artifactsToPush, gpgSignOverride, gpgPassphrase));
        return status;
    }

    /**
     * Pushes all given files as a version in Bintray, if the version \ package don't exist they are created
     *
     * @param uploadInfo      Info about the package \ version to push
     * @param artifactsToPush All artifacts to be pushed under the version
     * @param gpgSignOverride Indicates if to override the version sign
     * @param gpgPassphrase   The key that is used with the subject's Bintray-stored gpg key to sign the version
     * @return StatusHolder containing all push results.
     */
    private BasicStatusHolder pushVersion(BintrayUploadInfo uploadInfo, List<FileInfo> artifactsToPush,
            Boolean gpgSignOverride, String gpgPassphrase) {

        BasicStatusHolder status = new BasicStatusHolder();
        String subject = uploadInfo.getPackageDetails().getSubject();
        VersionHandle bintrayVersionHandle;

        try (Bintray client = createBintrayClient(status)) {
            validatePushRequestParams(subject, artifactsToPush, status);

            RepositoryHandle bintrayRepoHandle = validateRepoAndCreateIfNeeded(uploadInfo, client, status);
            PackageHandle bintrayPackageHandle = createOrUpdatePackage(uploadInfo, bintrayRepoHandle, status);
            bintrayVersionHandle = createOrUpdateVersion(uploadInfo, bintrayPackageHandle, status);

            pushArtifactsToVersion(uploadInfo, artifactsToPush, status, bintrayVersionHandle);
            signVersion(bintrayVersionHandle, uploadInfo.getVersionDetails().isGpgSign(), gpgSignOverride,
                    gpgPassphrase, artifactsToPush.size(), status);

            //Publish comes last so that gpg sign files will get published too
            publishFiles(uploadInfo, status, bintrayVersionHandle);
        } catch (Exception e) {
            if (!(e instanceof BintrayCallException) && !(e instanceof MultipleBintrayCallException)) {
                status.error("Operation failed: " + e.getMessage(), HttpStatus.SC_CONFLICT, e, log);
            }
            return status;
        }
        createUpdatePropsForPushedArtifacts(artifactsToPush, uploadInfo, status);
        String end;
        if (status.hasErrors()) {
            end = "with errors";
        } else if (status.hasWarnings()) {
            end = "with warnings";
        } else {
            end = "successfully";
        }
        status.status(String.format("Push to bintray completed %s", end), log);
        return status;
    }

    private void publishFiles(BintrayUploadInfo uploadInfo, BasicStatusHolder status,
            VersionHandle bintrayVersionHandle) {
        if (uploadInfo.getPublish() != null && uploadInfo.getPublish()) {
            log.info("Publishing files...");
            try {
                bintrayVersionHandle.publish();
            } catch (BintrayCallException bce) {
                status.error(bce.toString(), bce.getStatusCode(), log);
            }
        }
    }

    private boolean bintrayRepoExists(RepositoryHandle bintrayRepoHandle, BasicStatusHolder status) {
        try {
            if (!bintrayRepoHandle.exists()) {  //Repo exists?
                return false;
            }
        } catch (BintrayCallException bce) {
            status.error(bce.toString(), bce.getStatusCode(), log);
            return false;
        }
        return true;
    }

    private void pushArtifactsToVersion(BintrayUploadInfo uploadInfo, List<FileInfo> artifactsToPush,
            BasicStatusHolder status, VersionHandle bintrayVersionHandle) throws MultipleBintrayCallException {
        List<RepoPath> artifactPaths = Lists.newArrayList();
        Map<String, InputStream> streamMap = Maps.newHashMap();
        try {
            for (FileInfo fileInfo : artifactsToPush) {
                artifactPaths.add(fileInfo.getRepoPath());
                ResourceStreamHandle handle = repoService.getResourceStreamHandle(fileInfo.getRepoPath());
                streamMap.put(fileInfo.getRelPath(), new AutoCloseInputStream(handle.getInputStream()));
            }
            status.status("Starting to push the requested files to " + String.format("into %s/%s/%s/%s: ",
                    uploadInfo.getPackageDetails().getSubject(), uploadInfo.getPackageDetails().getRepo(),
                    uploadInfo.getPackageDetails().getName(), uploadInfo.getVersionDetails().getName()), log);

            log.info("Pushing {} files...", streamMap.keySet().size());
            log.debug("Pushing the following files into Bintray: {}", Arrays.toString(artifactPaths.toArray()));
            bintrayVersionHandle.upload(streamMap);
        } catch (MultipleBintrayCallException mbce) {
            for (BintrayCallException bce : mbce.getExceptions()) {
                status.error(bce.toString(), bce.getStatusCode(), log);
            }
            throw mbce;
        } finally {
            for (InputStream stream : streamMap.values()) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private void validatePushRequestParams(String subject, List<FileInfo> artifactsToPush, BasicStatusHolder status)
            throws BintrayCallException {

        int fileUploadLimit = getFileUploadLimit();
        if (fileUploadLimit != 0 && artifactsToPush.size() > fileUploadLimit) { //0 is unlimited
            status.error(String.format("The amount of artifacts that are about to be pushed(%s) exceeds the maximum" +
                    " amount set by the administrator(%s), aborting operation", artifactsToPush.size(), fileUploadLimit)
                    , SC_BAD_REQUEST, log);
            throw new BintrayCallException(SC_BAD_REQUEST, status.getLastError().getMessage(), "");
        }

        //Subject must be specified in json
        if (StringUtils.isBlank(subject)) {
            status.error("Bintray subject must be defined in the spec or given as an override param - aborting",
                    SC_BAD_REQUEST, log);
            throw new BintrayCallException(SC_BAD_REQUEST, status.getLastError().getMessage(), "");
        }
    }

    private RepositoryHandle validateRepoAndCreateIfNeeded(BintrayUploadInfo uploadInfo, Bintray client,
            BasicStatusHolder status) throws Exception {

        String subjectName = uploadInfo.getPackageDetails().getSubject();
        String bintrayRepo = uploadInfo.getPackageDetails().getRepo();
        SubjectHandle subject = client.subject(subjectName);
        //No 'repo' clause --> return RepositoryHandle matching the 'repo' field in the 'package' clause
        if (!hasRepoClause(uploadInfo)) {
            RepositoryHandle bintrayRepoHandle = subject.repository(bintrayRepo);
            if (!bintrayRepoExists(bintrayRepoHandle, status)) {
                //Doesn't matter what the exception holds, only the status is returned by the calling method
                status.error("No such Repository " + bintrayRepo + " for subject " + subjectName, SC_NOT_FOUND, log);
                throw new BintrayCallException(SC_BAD_REQUEST, "no such repo ", bintrayRepo);
            } else {
                return bintrayRepoHandle;
            }
            //'repo' clause exists -> verify name consistency with 'repo' field in the 'package' clause if exists
        } else if (!repoNamesMatch(uploadInfo, bintrayRepo)) {
            status.error("Mismatch between the 'name' field in the 'repo' clause and the 'repo' field in the " +
                    "'package' clause, aborting operation", SC_BAD_REQUEST, log);
            //Doesn't matter what the exception holds, only the status is returned by the calling method
            throw new BintrayCallException(SC_BAD_REQUEST, "mismatch between repo name fields", bintrayRepo);
        }
        //Create or update the repo
        return createOrUpdateRepo(uploadInfo.getRepositoryDetails(), subject, status);
    }

    private boolean hasRepoClause(BintrayUploadInfo uploadInfo) {
        return uploadInfo.getRepositoryDetails() != null
                && StringUtils.isNotBlank(uploadInfo.getRepositoryDetails().getName());
    }

    private boolean repoNamesMatch(BintrayUploadInfo uploadInfo, String bintrayRepo) {
        return StringUtils.isBlank(bintrayRepo)
                || uploadInfo.getRepositoryDetails().getName().equalsIgnoreCase(bintrayRepo);
    }

    @Override
    public Bintray createBintrayClient(BasicStatusHolder status) throws IllegalArgumentException {
        UsernamePasswordCredentials credsToUse = getCurrentUserBintrayCreds();
        CloseableHttpClient httpClient = createHTTPClient(credsToUse);
        Bintray client = BintrayClient.create(httpClient, PathUtils.trimTrailingSlashes(getBaseBintrayApiUrl()),
                ConstantValues.bintrayClientThreadPoolSize.getInt(),
                ConstantValues.bintrayClientSignRequestTimeout.getInt());
        return client;
    }

    @Override
    public List<Repo> getReposToDeploy(@Nullable Map<String, String> headersMap) throws IOException, BintrayException {
        UsernamePasswordCredentials creds = getCurrentUserBintrayCreds();
        String requestUrl = getBaseBintrayApiUrl() + PATH_REPOS + "/" + creds.getUserName();
        InputStream responseStream = null;
        try {
            responseStream = executeGet(requestUrl, creds, headersMap);
            if (responseStream != null) {
                return JacksonReader.streamAsValueTypeReference(responseStream, new TypeReference<List<Repo>>() {
                });
            }
        } finally {
            IOUtils.closeQuietly(responseStream);
        }
        return null;
    }

    @Override
    public List<String> getPackagesToDeploy(String repoKey, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException {
        UsernamePasswordCredentials creds = getCurrentUserBintrayCreds();
        String requestUrl = getBaseBintrayApiUrl() + PATH_REPOS + "/" + repoKey + "/packages";
        InputStream responseStream = null;
        try {
            responseStream = executeGet(requestUrl, creds, headersMap);
            if (responseStream != null) {
                return getPackagesList(responseStream);
            }
        } finally {
            IOUtils.closeQuietly(responseStream);
        }
        return null;
    }

    private List<String> getPackagesList(InputStream responseStream) throws IOException {
        List<String> packages = newArrayList();
        JsonNode packagesTree = JacksonReader.streamAsTree(responseStream);
        Iterator<JsonNode> elements = packagesTree.getElements();
        while (elements.hasNext()) {
            JsonNode packageElement = elements.next();
            String packageName = packageElement.get("name").asText();
            boolean linked = packageElement.get("linked").asBoolean();
            if (!linked) {
                packages.add(packageName);
            }
        }
        return packages;
    }

    @Override
    public List<String> getVersions(String repoKey, String packageId, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException {
        UsernamePasswordCredentials creds = getCurrentUserBintrayCreds();
        String requestUrl = getBaseBintrayApiUrl() + PATH_PACKAGES + "/" + repoKey + "/" + packageId;
        InputStream responseStream = null;
        try {
            responseStream = executeGet(requestUrl, creds, headersMap);
            if (responseStream != null) {
                RepoPackage repoPackage = JacksonReader.streamAsClass(responseStream, RepoPackage.class);
                return repoPackage.getVersions();
            }
        } finally {
            IOUtils.closeQuietly(responseStream);
        }
        return null;
    }

    @Override
    public String getVersionFilesUrl(BintrayParams bintrayParams) {
        return getBaseBintrayUrl() + bintrayParams.getRepo() + "/"
                + bintrayParams.getPackageId() + "/" + bintrayParams.getVersion() + "/view/files";
    }

    @Override
    public BintrayUser getBintrayUser(String username, String apiKey, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException {
        String requestUrl = getBaseBintrayApiUrl() + PATH_USERS + "/" + username;
        InputStream responseStream = null;
        try {
            responseStream = executeGet(requestUrl, new UsernamePasswordCredentials(username, apiKey), headersMap);
            if (responseStream != null) {
                return JacksonReader.streamAsValueTypeReference(responseStream, new TypeReference<BintrayUser>() {
                });
            }
        } finally {
            IOUtils.closeQuietly(responseStream);
        }
        return null;
    }

    @Override
    public BintrayUser getBintrayUser(String username, String apiKey) throws IOException, BintrayException {
        return getBintrayUser(username, apiKey, null);
    }

    private boolean validCredentialsExist(BasicStatusHolder status) {
        if (!isUserHasBintrayAuth()) {
            status.error("No Bintray Authentication defined for user", HttpStatus.SC_UNAUTHORIZED, log);
            return false;
        }
        return true;
    }

    @Override
    public boolean hasBintraySystemUser() {
        return StringUtils.isNotBlank(ConstantValues.bintraySystemUser.getString())
                || getBintrayGlobalConfig().globalCredentialsExist();
    }

    @Override
    public boolean isUserHasBintrayAuth() {
        UserInfo userInfo = getCurrentUser();
        if (userInfo != null) {
            String bintrayAuth = userInfo.getBintrayAuth();
            if (StringUtils.isNotBlank(bintrayAuth)) {
                String[] bintrayAuthTokens = StringUtils.split(bintrayAuth, ":");
                if (bintrayAuthTokens.length == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getBintrayRegistrationUrl() {
        String licenseKeyHash = addonsManager.getLicenseKeyHash();
        StringBuilder builder = new StringBuilder(ConstantValues.bintrayUrl.getString()).append("?source=artifactory");
        if (StringUtils.isNotBlank(licenseKeyHash)) {
            builder.append(":").append(licenseKeyHash);
        }
        return builder.toString();
    }

    @Override
    public BintrayItemSearchResults<BintrayItemInfo> searchByName(String query,
            @Nullable Map<String, String> headersMap) throws IOException, BintrayException {
        String requestUrl = getBaseBintrayApiUrl() + "search/file/?subject=bintray&repo=jcenter&name=" + query;
        log.debug("requestUrl=\"" + requestUrl + "\"");
        try (CloseableHttpClient client = createHTTPClient(new UsernamePasswordCredentials("", ""))) {
            HttpGet getMethod = createGetMethod(requestUrl, headersMap);
            CloseableHttpResponse response = client.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new BintrayException(response.getStatusLine().getReasonPhrase(), statusCode);
            } else {
                try {
                    int rangeLimitTotal = Integer.parseInt(response.getFirstHeader(RANGE_LIMIT_TOTAL).getValue());
                    InputStream responseStream = response.getEntity().getContent();
                    List<BintrayItemInfo> listResult = JacksonReader.streamAsValueTypeReference(
                            responseStream, new TypeReference<List<BintrayItemInfo>>() {
                            }
                    );
                    List<BintrayItemInfo> distinctResults = listResult.stream().distinct().collect(Collectors.toList());
                    BintrayItemSearchResults<BintrayItemInfo> results = new BintrayItemSearchResults<>(distinctResults,
                            rangeLimitTotal);
                    fillLocalRepoPaths(distinctResults);
                    fixDateFormat(distinctResults);
                    return results;
                } finally {
                    IOUtils.closeQuietly(response);
                }
            }
        }
    }

    private void fixDateFormat(List<BintrayItemInfo> listResult) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Build.STARTED_FORMAT);
        for (BintrayItemInfo bintrayItemInfo : listResult) {
            String createdDateFromBintray = bintrayItemInfo.getCreated();
            long createdDate = ISODateTimeFormat.dateTime().parseMillis(createdDateFromBintray);
            bintrayItemInfo.setCreated(simpleDateFormat.format(new Date(createdDate)));
        }
    }

    private void fillLocalRepoPaths(List<BintrayItemInfo> listResult) {
        for (BintrayItemInfo row : listResult) {
            RepoPath repo = getRepoPath(row);
            row.setCached(repo != null);
            row.setLocalRepoPath(repo);
        }
    }

    private RepoPath getRepoPath(BintrayItemInfo row) {
        RemoteRepoDescriptor jCenterRepo = getJCenterRepo();
        VfsQueryResult result = vfsQueryService.createQuery().addPathFilter(
                row.getPath().replace(row.getName(), "")).name(
                row.getName()).execute(100);
        RepoPath repoPath = null;
        for (VfsQueryRow vfsQueryRow : result.getAllRows()) {
            RepoPath tempRepoPath = vfsQueryRow.getItem().getRepoPath();
            LocalRepoDescriptor localRepoDescriptor = repoService.localOrCachedRepoDescriptorByKey(
                    tempRepoPath.getRepoKey());
            // If The the descriptor is "jcenter-cached" then return it immediately
            if (localRepoDescriptor != null && tempRepoPath.getRepoKey().equals(
                    jCenterRepo.getKey() + LocalCacheRepoDescriptor.PATH_SUFFIX)) {
                return tempRepoPath;
            }
            // Keep the first repoPath we encounter
            if (repoPath == null && localRepoDescriptor != null) {
                repoPath = tempRepoPath;
            }
        }
        return repoPath;
    }

    @Override
    public RemoteRepoDescriptor getJCenterRepo() {
        String jcenterHost = "jcenter.bintray.com";
        String url = ConstantValues.jCenterUrl.getString();
        try {
            URI uri = new URIBuilder(url).build();
            jcenterHost = uri.getHost();
        } catch (URISyntaxException e) {
            log.warn("Unable to construct a valid URI from '{}': {}", url);
        }

        List<RemoteRepoDescriptor> remoteRepoDescriptors = repoService.getRemoteRepoDescriptors();
        for (RemoteRepoDescriptor remoteRepoDescriptor : remoteRepoDescriptors) {
            if (remoteRepoDescriptor.getUrl().contains(jcenterHost)) {
                return remoteRepoDescriptor;
            }
        }
        return null;
    }

    @Override
    public BintrayPackageInfo getBintrayPackageInfo(String sha1, @Nullable Map<String, String> headersMap) {
        return getPackageInfoFromCache(sha1, headersMap);
    }

    private BintrayPackageInfo getPackageInfoFromCache(String sha1, @Nullable Map<String, String> headersMap) {
        BintrayPackageInfo bintrayPackageInfo = bintrayPackageCache.get(sha1);
        // Try to get info from bintray if cache is empty
        if (bintrayPackageInfo == null) {
            populatePackageCacheFromBintray(sha1, headersMap);
        }
        return bintrayPackageCache.get(sha1);
    }

    private BintrayItemInfo getBintrayItemInfoByChecksum(final String sha1, @Nullable Map<String, String> headersMap) {
        String itemInfoRequest = String.format("%ssearch/file/?sha1=%s&subject=bintray&repo=jcenter",
                getBaseBintrayApiUrl(), sha1);
        BintrayItemInfo result = null;
        CloseableHttpClient client = getUserOrSystemApiKeyHttpClient();
        CloseableHttpResponse response = null;
        try {
            log.debug("Bintray item request:{}", itemInfoRequest);
            HttpGet getMethod = createGetMethod(itemInfoRequest, headersMap);
            response = client.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    String userName = getCurrentUser().getUsername();
                    log.info("Bintray authentication failure: item {}, user {}", sha1, userName);
                } else {
                    log.info("Bintray request info failure for item {}", sha1);
                }
            } else {
                int rangeLimitTotal = Integer.parseInt(response.getFirstHeader(RANGE_LIMIT_TOTAL).getValue());
                InputStream responseStream = response.getEntity().getContent();
                List<BintrayItemInfo> listResult = JacksonReader.streamAsValueTypeReference(
                        responseStream, new TypeReference<List<BintrayItemInfo>>() {
                        }
                );
                BintrayItemSearchResults<BintrayItemInfo> results = new BintrayItemSearchResults<>(
                        listResult,
                        rangeLimitTotal);
                if (results.getResults().size() > 0) {
                    result = results.getResults().get(0);
                } else {
                    log.debug("No item found for request: {}", itemInfoRequest);
                }
            }

        } catch (Exception e) {
            log.warn("Failure during Bintray fetching package {}: {}", sha1, e.getMessage());
            log.debug("Failure during Bintray fetching package {}: {}", sha1, e);
        } finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(client);
        }
        return result;
    }

    private void populatePackageCacheFromBintray(final String sha1, final @Nullable Map<String, String> headersMap) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            BintrayPackageInfo result = null;
            // Try to get Bintray info for item by sha1
            BintrayItemInfo bintrayItemInfo = getBintrayItemInfoByChecksum(sha1, headersMap);
            // If item found update cache
            if (bintrayItemInfo == null) {
                return;
            }

            // Item exists in Bintray therefore try to get package info from Bintray
            StringBuilder urlBuilder = new StringBuilder(getBaseBintrayApiUrl()).
                    append("packages").append("/").
                    append(bintrayItemInfo.getOwner()).append("/").
                    append(bintrayItemInfo.getRepo()).append("/").
                    append(bintrayItemInfo.getPackage());
            final String url = urlBuilder.toString();
            log.debug("Bintray package request:{}", url);
            HttpGet getMethod = createGetMethod(url, headersMap);
            client = getUserOrSystemApiKeyHttpClient();
            response = client.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                    String userName = getCurrentUser().getUsername();
                    log.info("Bintray authentication failure: user {}", userName);
                }
            } else {
                InputStream responseStream = response.getEntity().getContent();
                result = JacksonReader.streamAsValueTypeReference(
                        responseStream, new TypeReference<BintrayPackageInfo>() {
                        }
                );
            }

            if (result != null) {
                bintrayPackageCache.put(sha1, result);
            }
        } catch (Exception e) {
            log.warn("Failure during Bintray fetching package {}: {}", sha1, e.getMessage());
            log.debug("Failure during Bintray fetching package {}: {}", sha1, e);
        } finally {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(client);
        }
    }

    private InputStream executeGet(String requestUrl, UsernamePasswordCredentials creds,
            @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException {
        HttpGet getMethod = createGetMethod(requestUrl, headersMap);
        CloseableHttpClient client = createHTTPClient(creds);
        CloseableHttpResponse response = client.execute(getMethod);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new BintrayException(response.getStatusLine().getReasonPhrase(),
                    response.getStatusLine().getStatusCode());
        } else {
            return response.getEntity().getContent();
        }
    }

    private String getBaseBintrayUrl() {
        return PathUtils.addTrailingSlash(ConstantValues.bintrayUrl.getString());
    }

    private String getBaseBintrayApiUrl() {
        return PathUtils.addTrailingSlash(ConstantValues.bintrayApiUrl.getString());
    }

    private HttpPut createPutMethod(String requestUrl, @Nullable Map<String, String> headersMap,
            HttpEntity requestEntity) {
        HttpPut putMethod = new HttpPut(HttpUtils.encodeQuery(requestUrl));
        putMethod.setEntity(requestEntity);
        updateHeaders(headersMap, putMethod);
        return putMethod;
    }

    private HttpGet createGetMethod(String requestUrl, @Nullable Map<String, String> headersMap) {
        HttpGet getMethod = new HttpGet(HttpUtils.encodeQuery(requestUrl));
        updateHeaders(headersMap, getMethod);
        return getMethod;
    }

    private void updateHeaders(Map<String, String> headersMap, HttpRequestBase method) {
        method.setHeader(HttpHeaders.USER_AGENT, HttpUtils.getArtifactoryUserAgent());
        if (headersMap != null) {
            String headerVal = HttpUtils.adjustRefererValue(headersMap, headersMap.get("Referer".toUpperCase()));
            method.setHeader("Referer", headerVal);
        }
    }

    private UsernamePasswordCredentials getCurrentUserBintrayCreds() {
        UserInfo userInfo = getCurrentUser();
        String bintrayAuth = userInfo == null ? "" : userInfo.getBintrayAuth();
        if (StringUtils.isNotBlank(bintrayAuth)) {
            String[] bintrayAuthTokens = StringUtils.split(bintrayAuth, ":");
            if (bintrayAuthTokens.length != 2) {
                throw new IllegalArgumentException("Found invalid Bintray credentials.");
            }
            return new UsernamePasswordCredentials(bintrayAuthTokens[0], bintrayAuthTokens[1]);
        }
        throw new IllegalArgumentException(
                "Couldn't find Bintray credentials, please configure them from the user profile page.");
    }

    private UsernamePasswordCredentials getGlobalBintrayCreds() {
        if (hasBintraySystemUser()) {
            String userName =
                    (StringUtils.isNotEmpty(centralConfig.getDescriptor().getBintrayConfig().getUserName())) ?
                            getBintrayGlobalConfig().getUserName() : ConstantValues.bintraySystemUser.getString();

            String apiKey =
                    (StringUtils.isNotEmpty(centralConfig.getDescriptor().getBintrayConfig().getApiKey())) ?
                            getBintrayGlobalConfig().getApiKey() : ConstantValues.bintraySystemUserApiKey.getString();

            return new UsernamePasswordCredentials(userName, apiKey);
        }
        throw new IllegalArgumentException(
                "Couldn't find Global Bintray credentials, please configure them from the admin page.");
    }

    private CloseableHttpClient getUserOrSystemApiKeyHttpClient() {
        CloseableHttpClient client;
        if (isUserHasBintrayAuth()) {
            client = createHTTPClient();
        } else if (hasBintraySystemUser()) {
            client = createHTTPClient(getGlobalBintrayCreds());
        } else {
            throw new IllegalStateException("User doesn't have bintray credentials");
        }
        return client;
    }

    private CloseableHttpClient createHTTPClient() {
        return createHTTPClient(getCurrentUserBintrayCreds());
    }

    private CloseableHttpClient createHTTPClient(UsernamePasswordCredentials creds) {
        ProxyDescriptor proxy = ContextHelper.get().getCentralConfig().getDescriptor().getDefaultProxy();

        return new HttpClientConfigurator()
                .hostFromUrl(getBaseBintrayApiUrl())
                .soTimeout(ConstantValues.bintrayClientRequestTimeout.getInt())
                .connectionTimeout(ConstantValues.bintrayClientRequestTimeout.getInt())
                .noRetry()
                .proxy(proxy)
                .authentication(creds)
                .maxTotalConnections(30)
                .defaultMaxConnectionsPerHost(30)
                .getClient();
    }

    private BintrayUploadInfo getUplaodInfoForBuild(Build build, BintrayUploadInfoOverride override,
            BasicStatusHolder status) {

        //Override given
        if (override != null) {
            if (override.isValid()) {
                return new BintrayUploadInfo(override);
            } else if (!override.isEmpty()) {
                status.error("Invalid override parameters given, aborting operation.", SC_BAD_REQUEST, log);
                return null;
            }
        }
        //No override - find descriptor and validate
        FileInfo descriptorFile = getDescriptorFromBuild(build, status);
        if (status.hasErrors()) {
            return null;
        }
        return validateUploadInfoFile(descriptorFile, status);
    }

    /**
     * Uses an aql query to get the json descriptor that's included in the build artifacts.
     */
    private FileInfo getDescriptorFromBuild(Build build, BasicStatusHolder status) {
        AqlApiItem aql = AqlApiItem.create().
                filter(
                        and(
                                AqlApiItem.name().matches("*bintray-info*.json"),
                                AqlApiItem.property().property("build.name", AqlComparatorEnum.equals, build.getName()),
                                AqlApiItem.property().property("build.number", AqlComparatorEnum.equals,
                                        build.getNumber())
                        )
                );
        AqlEagerResult<AqlItem> results = aqlService.executeQueryEager(aql);

        if (results.getSize() == 0) {
            status.error("Descriptor not found in build artifacts, aborting operation", SC_NOT_FOUND, log);
            return null;
        }

        int matchedFilesCounter = 0;
        RepoPath path = null;
        //Aql searches don't support regex - and other files might contain similar names - filter by regex now
        for (AqlItem result : results.getResults()) {
            path = InternalRepoPathFactory.create(result.getRepo(), result.getPath() + "/" + result.getName());
            if (isBintrayJsonInfoFile(path.getPath())) {
                log.debug("Found descriptor for build {} : {} in path {}", build.getName(), build.getNumber(), path);
                matchedFilesCounter++;
            }
        }
        if (matchedFilesCounter > 1) {
            status.error("Found More than one Descriptor in build artifacts, aborting operation", SC_BAD_REQUEST, log);
            return null;
        }
        return repoService.getFileInfo(path);
    }

    private BintrayUploadInfo validateUploadInfoFile(FileInfo descriptorJson, BasicStatusHolder status) {
        if (!isBintrayJsonInfoFile(descriptorJson.getRepoPath().getName())) {
            status.error("The path specified: " + descriptorJson.getRepoPath() + ", does not point to a descriptor. " +
                    "The file name must contain 'bintray-info' and have a .json extension", SC_NOT_FOUND, log);
            return null;
        }
        BintrayUploadInfo uploadInfo = null;
        InputStream jsonContentStream = binaryStore.getBinary(descriptorJson.getSha1());
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            uploadInfo = mapper.readValue(jsonContentStream, BintrayUploadInfo.class);
        } catch (IOException e) {
            log.debug("{}", e);
            status.error("Can't process the json file: " + e.getMessage(), SC_BAD_REQUEST, log);
        } finally {
            IOUtils.closeQuietly(jsonContentStream);
        }
        return uploadInfo;
    }

    private void filterBuildArtifactsByDescriptor(BintrayUploadInfo uploadInfo, List<FileInfo> artifactsToPush,
            BasicStatusHolder status) {

        List<String> descriptorArtifactPaths = uploadInfo.getArtifactPaths();
        //null - applyToFiles field doesn't exist
        if (descriptorArtifactPaths != null && !descriptorArtifactPaths.isEmpty()) {
            //size == 1 && get(0) == "" --> field looks like "applyToFiles": ""  (jackson deserialization edge case)
            if (!(descriptorArtifactPaths.size() == 1 && StringUtils.isBlank(descriptorArtifactPaths.get(0)))) {
                status.error("Json file contains paths to artifacts, this command pushes whole builds only, aborting " +
                        "operation", SC_BAD_REQUEST, log);
                return;
            }
        }

        //applyToProps has values - prepare a file list and send it and the props list to the aql search to be filtered
        if (uploadInfo.getFilterProps() != null) {
            List<AqlSearchablePath> artifactPaths = Lists.newArrayList();
            for (FileInfo file : artifactsToPush) {
                artifactPaths.add(new AqlSearchablePath(file.getRepoPath()));
            }
            artifactsToPush = collectArtifactItemInfos(artifactPaths,
                    getMapFromUploadInfoMultiSet(uploadInfo.getFilterProps()));
        }

        //applyToProps filtered out all files
        if (CollectionUtils.isNullOrEmpty(artifactsToPush)) {
            status.error("The 'applyToProps' field in the json descriptor contains one or more properties that " +
                    "caused all artifacts to be filtered out, aborting operation", SC_BAD_REQUEST, log);
        }
    }

    /**
     * Remove json file\s from file list that's being pushed to Bintray, handles cases where more than one file was
     * found.
     * In case no json file was specified (as in pushing a build) and more than one was found in the artifact
     * list the most recent file will be used
     *
     * @param artifactsToPush   List of artifacts that are about to be pushed to Bintray
     * @param specifiedJsonPath Path to specific json file to use - if the user has specified one (can be null)
     * @param status            status holder of entire operation
     * @return the most recent bintray upload info json file found
     */
    private FileInfo filterOutJsonFileFromArtifactsToPush(List<FileInfo> artifactsToPush, RepoPath specifiedJsonPath,
            BasicStatusHolder status) {
        List<FileInfo> uploadInfoFiles = Lists.newArrayList();
        List<String> uploadInfoFileNames = Lists.newArrayList();
        //Find all matches for the descriptor json file
        for (FileInfo file : artifactsToPush) {
            if (isBintrayJsonInfoFile(file.getName())) {
                uploadInfoFiles.add(file);
                uploadInfoFileNames.add(file.getRepoPath().toString());
            }
        }
        if (uploadInfoFiles.isEmpty()) { //no json - special case for build-oriented operation only (when using override)
            return null;
        }
        FileInfo mostRecentJson = uploadInfoFiles.get(0);

        //More than one json matched the pattern - but a specific upload info file was specified
        if (specifiedJsonPath != null) {
            if (uploadInfoFiles.size() > 1) {
                status.warn("Found more than one descriptor, using the one specified by user: " + specifiedJsonPath,
                        log);
                log.debug("Found bintray-info.json files: {}", Arrays.toString(uploadInfoFileNames.toArray()));
            }
            mostRecentJson = repoService.getFileInfo(specifiedJsonPath);
        }

        //Else, find latest modified (newest) json
        else if (uploadInfoFiles.size() > 1) {
            status.warn("Found more than one descriptor, using the most recent one", log);
            for (int i = 1; i < uploadInfoFiles.size(); i++) {
                if (uploadInfoFiles.get(i).getLastModified() > mostRecentJson.getLastModified()) {
                    mostRecentJson = uploadInfoFiles.get(i);
                }
            }
            log.debug("Most recent descriptor found: {}, with last modified value: {}",
                    mostRecentJson.getRepoPath().toString(), mostRecentJson.getLastModified());
        }
        artifactsToPush.removeAll(uploadInfoFiles);
        return mostRecentJson;
    }


    /**
     * Create or update an existing Bintray Repository with the specified info
     *
     * @param repositoryDetails BintrayUploadInfo representing the supplied json file
     * @param subjectHandle     SubjectHandle retrieved by the Bintray Java Client
     * @param status            status holder of entire operation
     * @return a RepositoryHandle   pointing to the created/updated repository
     * @throws Exception on any error occurred
     */
    private RepositoryHandle createOrUpdateRepo(RepositoryDetails repositoryDetails, SubjectHandle subjectHandle,
            BasicStatusHolder status) throws Exception {

        String repoName = repositoryDetails.getName();
        RepositoryHandle bintrayRepoHandle = subjectHandle.repository(repoName);
        try {
            if (!bintrayRepoExists(bintrayRepoHandle, status)) {
                //Repo doesn't exist - create it using the RepoDetails
                status.status("Creating repo " + repoName + " for subject " + bintrayRepoHandle.owner().name(), log);
                bintrayRepoHandle = subjectHandle.createRepo(repositoryDetails);
            } else if (repositoryDetails.getUpdateExisting() != null && repositoryDetails.getUpdateExisting()) {
                //Repo exists - update only if indicated
                status.status("Updating repo " + repoName + " with values taken from descriptor", log);
                bintrayRepoHandle.update(repositoryDetails);
            }
        } catch (BintrayCallException bce) {
            status.error(bce.getMessage() + ":" + bce.getReason(), bce.getStatusCode(), log);
            throw bce;
        } catch (IOException ioe) {
            log.debug("{}", ioe);
            throw ioe;
        }
        //Repo exists and should not be updated
        return bintrayRepoHandle;
    }

    /**
     * Create or update an existing Bintray Package with the specified info
     *
     * @param info             BintrayUploadInfo representing the supplied json file
     * @param repositoryHandle RepositoryHandle retrieved by the Bintray Java Client
     * @param status           status holder of entire operation
     * @return a PackageHandle pointing to the created/updated package
     * @throws Exception on any error occurred
     */
    private PackageHandle createOrUpdatePackage(BintrayUploadInfo info, RepositoryHandle repositoryHandle,
            BasicStatusHolder status) throws Exception {

        PackageDetails pkgDetails = info.getPackageDetails();
        PackageHandle packageHandle;
        packageHandle = repositoryHandle.pkg(pkgDetails.getName());
        try {
            if (!packageHandle.exists()) {
                status.status("Package " + pkgDetails.getName() + " doesn't exist, creating it", log);
                packageHandle = repositoryHandle.createPkg(pkgDetails);
            } else {
                packageHandle.update(pkgDetails);
            }
            log.debug("Package {} created", packageHandle.get().name());
        } catch (BintrayCallException bce) {
            status.error(bce.toString(), bce.getStatusCode(), bce, log);
            throw bce;
        } catch (IOException ioe) {
            log.debug("{}", ioe);
            throw ioe;
        }
        return packageHandle;
    }

    /**
     * Create or update an existing Bintray Package with the specified info
     *
     * @param info          BintrayUploadInfo representing the supplied json file
     * @param packageHandle PackageHandle retrieved by the Bintray Java Client or by {@link #createOrUpdatePackage}
     * @param status        status holder of entire operation
     * @return a VersionHandle pointing to the created/updated version
     * @throws Exception on any error occurred
     */
    private VersionHandle createOrUpdateVersion(BintrayUploadInfo info, PackageHandle packageHandle,
            BasicStatusHolder status) throws Exception {

        VersionDetails versionDetails = info.getVersionDetails();
        VersionHandle versionHandle = packageHandle.version(versionDetails.getName());
        try {
            if (!versionHandle.exists()) {
                status.status("Version " + versionDetails.getName() + " doesn't exist, creating it", log);
                versionHandle = packageHandle.createVersion(versionDetails);
            } else {
                versionHandle.update(versionDetails);
            }
            log.debug("Version {} created", versionHandle.get().name());
        } catch (BintrayCallException bce) {
            status.error(bce.toString(), bce.getStatusCode(), bce, log);
            throw bce;
        } catch (IOException ioe) {
            log.debug("{}", ioe);
            throw ioe;
        }
        return versionHandle;
    }

    private Multimap<String, String> getMapFromUploadInfoMultiSet(Set<Map<String, Collection<String>>> elements) {
        Multimap<String, String> elementsMap = HashMultimap.create();
        if (CollectionUtils.isNullOrEmpty(elements)) {
            return elementsMap;
        }
        for (Map<String, Collection<String>> element : elements) {
            String key = element.keySet().iterator().next();
            Collection<String> values = element.get(key);
            elementsMap.putAll(key, values);
        }
        return elementsMap;
    }

    //Collects a list of artifacts to push using an aql query, based on the descriptor's content or location
    private List<FileInfo> collectArtifactsToPushBasedOnDescriptor(FileInfo jsonFile, BintrayUploadInfo uploadInfo,
            BasicStatusHolder status) {

        List<AqlSearchablePath> artifactPaths = Lists.newArrayList();
        Multimap<String, String> propsToFilterBy = getMapFromUploadInfoMultiSet(uploadInfo.getFilterProps());
        boolean descriptorHasPaths = CollectionUtils.notNullOrEmpty(uploadInfo.getArtifactPaths());
        boolean descriptorHasRelPaths = CollectionUtils.notNullOrEmpty(uploadInfo.getArtifactRelativePaths());

        if (!descriptorHasPaths && !descriptorHasRelPaths) {
            if (propsToFilterBy.isEmpty()) {
                status.status("The descriptor doesn't contain file paths and no properties to filter by were " +
                        "specified , pushing everything under " + jsonFile.getRepoPath().getParent(), log);
            } else {
                status.status("The descriptor doesn't contain file paths, pushing everything under "
                        + jsonFile.getRepoPath().getParent() + " , filtered by the properties specified.", log);
            }
            artifactPaths = AqlUtils.getSearchablePathForCurrentFolderAndSubfolders(jsonFile.getRepoPath().getParent());
        } else {
            try {
                if (descriptorHasPaths) {
                    artifactPaths = AqlSearchablePath.fullPathToSearchablePathList(uploadInfo.getArtifactPaths());
                }
                if (descriptorHasRelPaths) {
                    artifactPaths.addAll(AqlSearchablePath.relativePathToSearchablePathList(
                            uploadInfo.getArtifactRelativePaths(), jsonFile.getRepoPath().getParent()));
                }
            } catch (IllegalArgumentException iae) {
                status.error("Paths in the descriptor must point to a file or use a valid wildcard that denotes " +
                        "several files (i.e. /*.*)", SC_BAD_REQUEST, iae, log);
                return null;
            }
        }
        List<FileInfo> artifactsToPush = collectArtifactItemInfos(artifactPaths, propsToFilterBy);
        filterOutJsonFileFromArtifactsToPush(artifactsToPush, jsonFile.getRepoPath(), status);

        //aql search returned no artifacts for query
        if (CollectionUtils.isNullOrEmpty(artifactsToPush)) {
            status.error("No artifacts found to push to Bintray, aborting operation", SC_NOT_FOUND, log);
        }
        return artifactsToPush;
    }

    /**
     * Searches for all Files defined in the supplied params.
     * The relation between each path is OR, and between each parameter is AND
     * The relation between parameters and paths is AND
     *
     * @param aqlSearchablePaths   Paths (repository paths) in the AqlSearchablePath form
     * @param propertiesFilterList List of property name and values to filter the file list by
     * @return A list of file infos that represents the results aql returned
     */
    private List<FileInfo> collectArtifactItemInfos(List<AqlSearchablePath> aqlSearchablePaths,
            Multimap<String, String> propertiesFilterList) {
        //Searching without any path at all is performance-risky...
        if (CollectionUtils.isNullOrEmpty(aqlSearchablePaths)) {
            return null;
        }
        AqlApiItem.AndClause rootFilterClause = AqlApiItem.and();
        AqlApiItem.AndClause propertiesAndClause = AqlApiItem.and();
        //Resolve patterned path or patterned file names, as well as direct paths
        AqlBase.OrClause artifactsPathOrClause = AqlUtils.getSearchClauseForPaths(aqlSearchablePaths);

        //Filter results by property key and value
        for (String key : propertiesFilterList.keySet()) {
            for (String value : propertiesFilterList.get(key)) {
                log.debug("Adding property {}, with value {} to artifact search query", key, value);
                propertiesAndClause.append(AqlApiItem.property().property(key, AqlComparatorEnum.equals, value));
            }
        }
        rootFilterClause.append(artifactsPathOrClause);
        rootFilterClause.append(propertiesAndClause);
        rootFilterClause.append(AqlApiItem.type().equal("file"));
        AqlApiItem artifactQuery = AqlApiItem.create().filter(rootFilterClause);

        List<FileInfo> itemInfoList = Lists.newArrayList();
        List<RepoPath> itemInfoPaths = Lists.newArrayList();
        AqlEagerResult<AqlItem> results = aqlService.executeQueryEager(artifactQuery);
        for (AqlItem result : results.getResults()) {
            RepoPath path = InternalRepoPathFactory.create(result.getRepo(), result.getPath() + "/" + result.getName());
            itemInfoList.add(repoService.getFileInfo(path));
            itemInfoPaths.add(path);
        }
        log.debug("BintaryService Artifact search returned the following artifacts: {}",
                Arrays.toString(itemInfoPaths.toArray()));
        return itemInfoList;
    }

    private BintrayConfigDescriptor getBintrayGlobalConfig() {
        BintrayConfigDescriptor bintrayDescriptor = centralConfig.getDescriptor().getBintrayConfig();
        return bintrayDescriptor != null ? bintrayDescriptor : new BintrayConfigDescriptor();
    }

    //Match anything as long as it has bintray-info in the name (case insensitive) and .json extension
    private boolean isBintrayJsonInfoFile(String fileName) {
        return fileName.matches("(?i)[\\s\\S]*bintray-info[\\s\\S]*.json");
    }

    //0 is unlimited
    private int getFileUploadLimit() {
        return getBintrayGlobalConfig().getFileUploadLimit();
    }

    /**
     * Handles signing the version according to the descriptor, override flag and passphrase (if given)
     */
    private void signVersion(VersionHandle versionHandle, boolean descriptorGpgSign, Boolean gpgSignOverride,
            String gpgPassphrase, int fileCount, BasicStatusHolder status) {

        String signed = " - the version will be signed";
        String notSigned = " - the version will not be signed";
        // TODO: [by dan] I know this is ugly, but there are so many edge cases :(
        try {
            if (StringUtils.isNotBlank(gpgPassphrase)) {
                String passphraseSent = "A passphrase was sent as a parameter to the command";
                if (gpgSignOverride != null) {
                    if (gpgSignOverride) {
                        status.status(passphraseSent + ", and the gpgSign override flag was set to true" + signed, log);
                        versionHandle.sign(gpgPassphrase, fileCount);
                    } else {
                        status.warn(passphraseSent + ", but the gpgSign override flag was set to false" + notSigned,
                                log);
                    }
                } else {
                    if (descriptorGpgSign) {
                        status.status(passphraseSent + " without an override, and the gpgSign flag in the descriptor" +
                                " was set to true" + signed, log);
                        versionHandle.sign(gpgPassphrase, fileCount);
                    } else {
                        status.warn(
                                passphraseSent + "without an override, and the gpgSign flag in the descriptor was" +
                                        " set to false" + notSigned, log);
                    }
                }
            } else if (gpgSignOverride != null) {
                if (gpgSignOverride) {
                    status.status("The gpgSign override flag is set to true and no passphrase was given, attempting" +
                            " to sign the version without a passphrase", log);
                    versionHandle.sign(fileCount);
                } else {
                    status.status("The gpgSign override flag is set to false" + notSigned, log);
                }
            } else {
                //no override - default to descriptor
                if (descriptorGpgSign) {
                    status.status("The gpgSign flag in the descriptor is set to true, attempting to sign the " +
                            "version without a passphrase", log);
                    versionHandle.sign(fileCount);
                }
            }
        } catch (BintrayCallException bce) {
            status.error("Error while signing the version: " + bce.toString(), bce.getStatusCode(), log);
        }
    }
}
