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

package org.artifactory.engine;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.RestCoreAddon;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.exception.FileExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.request.InternalArtifactoryRequest;
import org.artifactory.api.request.UploadService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.checksum.Checksum;
import org.artifactory.io.checksum.policy.ChecksumPolicy;
import org.artifactory.io.checksum.policy.LocalRepoChecksumPolicy;
import org.artifactory.md.MutablePropertiesInfo;
import org.artifactory.md.Properties;
import org.artifactory.md.PropertiesXmlProvider;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.SaveResourceContext;
import org.artifactory.repo.local.ValidDeployPathContext;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapter;
import org.artifactory.repo.snapshot.MavenSnapshotVersionAdapterContext;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.MutableRepoResourceInfo;
import org.artifactory.resource.UnfoundRepoResource;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.traffic.TrafficService;
import org.artifactory.traffic.entry.UploadEntry;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;
import org.artifactory.webapp.servlet.DelayedHttpResponse;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.apache.http.HttpStatus.*;
import static org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType.SERVER;
import static org.artifactory.descriptor.repo.SnapshotVersionBehavior.DEPLOYER;

/**
 * Handles upload of a single item. The item can be file, directory, properties, checksum data etc.
 * This service validates the request and delegates the actual upload to the repo and repo service.
 *
 * @author Yoav Landman
 */
@Service
public class UploadServiceImpl implements UploadService {
    private static final Logger log = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private BinaryStore binaryStore;

    @Autowired
    private BasicAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private TrafficService trafficService;

    private SuccessfulDeploymentResponseHelper successfulDeploymentResponseHelper =
            new SuccessfulDeploymentResponseHelper();

    @Override
    public void upload(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException,
            RepoRejectException {
        log.debug("Request: {}", request);

        addonsManager.interceptResponse(response);
        if (responseWasIntercepted(response)) {
            return;
        }

        validateRequestAndUpload(request, response);
    }

    private boolean responseWasIntercepted(ArtifactoryResponse response) {
        return response.isError();
    }

    private void validateRequestAndUpload(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        if (isRequestedRepoKeyInvalid(request)) {
            response.sendError(SC_NOT_FOUND, "No target local repository specified in deploy request.", log);
            return;
        }

        LocalRepo targetRepository = getTargetRepository(request);
        if (isTargetRepositoryInvalid(targetRepository)) {
            sendInvalidTargetRepositoryError(request, response);
            return;
        }

        if (NamingUtils.isProperties(request.getPath())) {
            validateAndUploadProperties(request, response, targetRepository);
            return;
        }

        try {
            long contentLength = request.getContentLength();
            RepoPath repoPath = InternalRepoPathFactory.create(targetRepository.getKey(), request.getPath(),
                    request.isDirectoryRequest());
            String requestSha1 = HttpUtils.getSha1Checksum(request);
            repoService.assertValidDeployPath(new ValidDeployPathContext.Builder(targetRepository, repoPath)
                    .contentLength(contentLength).requestSha1(requestSha1).build());
        } catch (RepoRejectException e) {
            handleInvalidDeployPathError(request, response, e);
            return;
        }

        adjustResponseAndUpload(request, response, targetRepository);
    }

    private boolean isRequestedRepoKeyInvalid(ArtifactoryRequest request) {
        return StringUtils.isBlank(request.getRepoKey());
    }

    private LocalRepo getTargetRepository(ArtifactoryRequest request) {
        return repoService.localRepositoryByKey(request.getRepoKey());
    }

    private boolean isTargetRepositoryInvalid(LocalRepo targetRepository) {
        return targetRepository == null;
    }

    private void sendInvalidTargetRepositoryError(ArtifactoryRequest request, ArtifactoryResponse response)
            throws IOException {
        int responseStatus;
        String responseMessage;
        String repoKey = request.getRepoKey();

        if (isKeyOfVirtualRepository(repoKey)) {

            response.setHeader("Allow", "GET");
            responseStatus = HttpStatus.SC_METHOD_NOT_ALLOWED;
            responseMessage = "A virtual repository cannot be used for deployment (" + repoKey +
                    "). Use a local repository as deployment target.";
        } else {

            responseStatus = SC_NOT_FOUND;
            responseMessage = "Could not find a local repository named " + repoKey + " to deploy to.";
        }
        response.sendError(responseStatus, responseMessage, log);
    }

    private boolean isKeyOfVirtualRepository(String repoKey) {
        return repoService.virtualRepoDescriptorByKey(repoKey) != null;
    }

    private void adjustResponseAndUpload(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo targetRepository) throws IOException {
        if (processOriginatedExternally(response)) {
            response = new DelayedHttpResponse((HttpArtifactoryResponse) response);
        }
        try {
            if (request.isDirectoryRequest()) {
                createDirectory(request, response);
            } else if (request.isChecksum()) {
                validateAndUploadChecksum(request, response, targetRepository);
            } else if (NamingUtils.isMetadata(request.getPath())) {
                response.sendError(SC_CONFLICT, "Old metadata notation is not supported anymore: " +
                        request.getRepoPath(), log);
            } else {
                uploadArtifact(request, response, targetRepository);
            }
        } catch (RepoRejectException e) {
            //Catch rejections on save
            response.sendError(e.getErrorCode(), e.getMessage(), log);
            return;
        }
        commitResponseIfDelayed(response);
    }

    private void handleInvalidDeployPathError(ArtifactoryRequest request, ArtifactoryResponse response,
            RepoRejectException rejectionException)
            throws IOException {
        if (rejectionSignifiesRequiredAuthorization(rejectionException)) {
            consumeRequestBody(request);
            String realmName = authenticationEntryPoint.getRealmName();
            response.sendAuthorizationRequired(rejectionException.getMessage(), realmName);
        } else {
            response.sendError(rejectionException.getErrorCode(), rejectionException.getMessage(), log);
        }
    }

    private void consumeRequestBody(ArtifactoryRequest request) throws IOException {
        IOUtils.copy(request.getInputStream(), new NullOutputStream());
    }

    private boolean rejectionSignifiesRequiredAuthorization(RepoRejectException rejectionException) {
        return (rejectionException.getErrorCode() == HttpStatus.SC_FORBIDDEN) && authService.isAnonymous();
    }

    private boolean processOriginatedExternally(ArtifactoryResponse response) {
        //Must check the type of the response instead of the request since the HTTP request object isn't accessible here
        return response instanceof HttpArtifactoryResponse;
    }

    private void commitResponseIfDelayed(ArtifactoryResponse response) throws IOException {
        if (response instanceof DelayedHttpResponse) {
            ((DelayedHttpResponse) response).commitResponseCode();
        }
    }

    private void createDirectory(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        RepoPath repoPath = request.getRepoPath();
        log.info("MKDir request to '{}'", request.getRepoPath());

        repoService.mkdirs(repoPath);
        annotateWithRequestPropertiesIfPermitted(request, repoPath);

        sendSuccessfulResponse(request, response, repoPath, true);
        log.info("Successfully created directory '{}'", request.getRepoPath());
    }

    private void annotateWithRequestPropertiesIfPermitted(ArtifactoryRequest request, RepoPath repoPath) {
        if (authService.canAnnotate(repoPath)) {
            Properties properties = request.getProperties();
            repoService.setProperties(repoPath, properties);
        }
    }

    private void validateAndUploadChecksum(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws IOException {
        long length = request.getContentLength();
        if (isAbnormalChecksumContentLength(length)) {
            // something is fishy, checksum file should not be so big...
            response.sendError(SC_CONFLICT, "Suspicious checksum file, content length of " + length +
                    " bytes is bigger than allowed.", log);
            return;
        }

        log.info("Deploy to '{}' Content-Length: {}", request.getRepoPath(), length < 0 ? "unspecified" : length);

        String checksumPath = request.getPath();
        if (NamingUtils.isMetadataChecksum(checksumPath) || MavenNaming.isMavenMetadataChecksum(checksumPath)) {
            //Ignore request - we maintain our self-calculated checksums for metadata
            consumeContentAndRespondWithSuccess(request, response);
            return;
        }

        validatePathAndUploadChecksum(request, response, repo);
    }

    /**
     * @see <a href="http://wiki.jfrog.org/confluence/display/RTF30/Artifactory%27s+REST+API#Artifactory'sRESTAPI-SetItemProperties">Set Item Properties REST API</a>
     * @deprecated can use the set item properties REST API instead, see
     */
    private void validateAndUploadProperties(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws IOException {
        //TORE: [by YS] this doesn't belong here
        long length = request.getContentLength();
        if (isAbnormalPropertiesContentLength(length)) {
            // something is fishy, checksum file should not be so big...
            response.sendError(SC_CONFLICT, "Properties content length of " + length +
                    " bytes is bigger than allowed.", log);
            return;
        }

        log.info("Deploy properties to '{}' Content-Length: {}", request.getRepoPath(),
                length < 0 ? "unspecified" : length);

        String path = request.getPath();
        if (isMavenRepo(repo)) {
            path = adjustMavenSnapshotPath(repo, request);
        }

        RepoPathImpl itemRepoPath = new RepoPathImpl(request.getRepoKey(),
                NamingUtils.stripMetadataFromPath(path));

        try {
            String propertiesStr = IOUtils.toString(request.getInputStream());
            PropertiesXmlProvider propertiesXmlProvider = new PropertiesXmlProvider();
            MutablePropertiesInfo propertiesInfo = propertiesXmlProvider.fromXml(propertiesStr);
            Properties properties = new PropertiesImpl(propertiesInfo);
            boolean success = repoService.setProperties(itemRepoPath, properties);
            if (success) {
                response.setStatus(SC_CREATED);
            } else {
                response.sendError(SC_NOT_FOUND, "Failed to set properties on " + itemRepoPath, log);
            }
        } catch (Exception e) {
            log.debug("Failed to deploy properties to '" + itemRepoPath + "'", e);
            response.sendError(SC_CONFLICT, "Failed to deploy properties : " + e.getMessage() +
                    " on path " + request.getRepoPath(), log);
        }
    }

    private boolean isAbnormalChecksumContentLength(long length) {
        return length > 1024;
    }

    private boolean isAbnormalPropertiesContentLength(long length) {
        return length > 4092;
    }

    private void consumeContentAndRespondWithSuccess(ArtifactoryRequest request, ArtifactoryResponse response)
            throws IOException {
        consumeRequestBody(request);
        response.sendSuccess();
    }

    private void validatePathAndUploadChecksum(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo repo) throws IOException {

        String uploadedChecksum;
        try {
            uploadedChecksum = getChecksumContentAsString(request);
        } catch (IOException e) {
            response.sendError(SC_CONFLICT, "Failed to read checksum from file: " + e.getMessage() +
                    " for path " + request.getRepoPath(), log);
            return;
        }

        ChecksumType checksumType = ChecksumType.forFilePath(request.getPath());
        RepoPath targetFileRepoPath = adjustAndGetChecksumTargetRepoPath(request, repo);
        try {
            ChecksumInfo checksumInfo = repoService.setClientChecksum(repo, checksumType, targetFileRepoPath,
                    uploadedChecksum);
            if (isChecksumValidAccordingToPolicy(uploadedChecksum, checksumInfo)) {
                sendUploadedChecksumResponse(request, response, targetFileRepoPath);
            } else {
                String message = String.format("Checksum error for '%s': received '%s' but actual is '%s'",
                        request.getPath(), uploadedChecksum, checksumInfo.getActual());
                sendInvalidUploadedChecksumResponse(request, response, repo, targetFileRepoPath, message);
            }
        } catch (ItemNotFoundRuntimeException e) {
            response.sendError(SC_NOT_FOUND, "Target file to set checksum on doesn't exist: " + targetFileRepoPath,
                    log);
        } catch (FileExpectedException e) {
            response.sendError(SC_CONFLICT, "Checksum only supported for files (but found folder): " +
                    targetFileRepoPath, log);
        }
    }

    private RepoPath adjustAndGetChecksumTargetRepoPath(ArtifactoryRequest request, LocalRepo repo) {
        String checksumTargetFile = request.getPath();
        if (isMavenRepo(repo)) {
            checksumTargetFile = adjustMavenSnapshotPath(repo, request);
        }
        return repo.getRepoPath(PathUtils.stripExtension(checksumTargetFile));
    }

    private String getChecksumContentAsString(ArtifactoryRequest request)
            throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
            return Checksum.checksumStringFromStream(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private boolean isChecksumValidAccordingToPolicy(String checksum, ChecksumInfo checksumInfo) {
        return checksum.equalsIgnoreCase(checksumInfo.getActual());
    }

    private void sendInvalidUploadedChecksumResponse(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo targetRepo, RepoPath repoPath, String errorMessage) throws IOException {
        ChecksumPolicy checksumPolicy = targetRepo.getChecksumPolicy();
        if (checksumPolicy instanceof LocalRepoChecksumPolicy &&
                ((LocalRepoChecksumPolicy) checksumPolicy).getPolicyType().equals(SERVER)) {
            log.debug(errorMessage);
            sendUploadedChecksumResponse(request, response, repoPath);
        } else {
            response.sendError(SC_CONFLICT, errorMessage, log);
        }
    }

    private void sendUploadedChecksumResponse(ArtifactoryRequest request, ArtifactoryResponse response,
            RepoPath targetFileRepoPath) {
        response.setHeader("Location", buildArtifactUrl(request, targetFileRepoPath));
        response.setStatus(SC_CREATED);
        response.sendSuccess();
    }

    private void uploadArtifact(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws IOException, RepoRejectException {
        if (isDeployArchiveBundle(request)) {
            RestCoreAddon restCoreAddon = addonsManager.addonByType(RestCoreAddon.class);
            restCoreAddon.deployArchiveBundle(request, response, repo);
            return;
        }

        long length = request.getContentLength();
        log.info("Deploy to '{}' Content-Length: {}", request.getRepoPath(), length < 0 ? "unspecified" : length);
        uploadFile(request, response, repo);
    }

    private void uploadFile(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo)
            throws RepoRejectException, IOException {
        String path = request.getPath();
        if (isMavenRepo(repo)) {
            if (isRepoSnapshotPolicyNotDeployer(repo) && MavenNaming.isSnapshotMavenMetadata(path)) {
                // Skip the maven metadata deployment - use the metadata calculated after the pom is deployed
                consumeContentAndRespondAccepted(request, response);
                return;
            }
            path = adjustMavenSnapshotPath(repo, request);
        }

        RepoPath fileRepoPath = repo.getRepoPath(path);
        MutableFileInfo fileInfo = InfoFactoryHolder.get().createFileInfo(fileRepoPath);
        boolean isChecksumDeploy = isChecksumDeploy(request);
        setFileInfoChecksums(request, fileInfo, isChecksumDeploy);
        FileResource fileResource = new FileResource(fileInfo);

        uploadItem(request, response, repo, fileResource);
    }

    private boolean isMavenRepo(LocalRepo repo) {
        return repo.getDescriptor().isMavenRepoLayout();
    }

    private boolean isRepoSnapshotPolicyNotDeployer(LocalRepo repo) {
        SnapshotVersionBehavior mavenSnapshotVersionBehavior = repo.getMavenSnapshotVersionBehavior();
        return !mavenSnapshotVersionBehavior.equals(DEPLOYER);
    }

    private void consumeContentAndRespondAccepted(ArtifactoryRequest request, ArtifactoryResponse response)
            throws IOException {
        log.trace("Skipping deployment of maven metadata file {}", request.getPath());
        consumeRequestBody(request);
        response.setStatus(HttpStatus.SC_ACCEPTED);
    }

    private void uploadItem(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo, RepoResource res)
            throws IOException, RepoRejectException {

        if (isChecksumDeploy(request)) {
            uploadItemWithReusedContent(request, response, repo, res);
        } else if (ConstantValues.httpUseExpectContinue.getBoolean() && HttpUtils.isExpectedContinue(request)) {
            uploadItemWithReusedOrProvidedContent(request, response, repo, res);
        } else {
            uploadItemWithProvidedContent(request, response, repo, res);
        }
    }

    private boolean isChecksumDeploy(ArtifactoryRequest request) {
        return Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.CHECKSUM_DEPLOY));
    }

    private boolean isDeployArchiveBundle(ArtifactoryRequest request) {
        return Boolean.parseBoolean(request.getHeader(ArtifactoryRequest.EXPLODE_ARCHIVE));
    }

    private void uploadItemWithReusedContent(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo repo, RepoResource res) throws IOException, RepoRejectException {
        String sha1 = HttpUtils.getSha1Checksum(request);
        if (StringUtils.isBlank(sha1)) {
            response.sendError(SC_NOT_FOUND, "Checksum deploy failed. SHA1 header '" +
                    ArtifactoryRequest.CHECKSUM_SHA1 + "' doesn't exist", log);
            return;
        }
        log.debug("Checksum deploy to '{}' with SHA1: {}", res.getRepoPath(), sha1);
        if (!ChecksumType.sha1.isValid(sha1)) {
            response.sendError(SC_NOT_FOUND, "Checksum deploy failed. Invalid SHA1: " + sha1, log);
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = binaryStore.getBinary(sha1);
            uploadItemWithContent(request, response, repo, res, inputStream);
        } catch (BinaryNotFoundException e) {
            response.sendError(SC_NOT_FOUND, "Checksum deploy failed. No existing file with SHA1: " + sha1, log);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void uploadItemWithReusedOrProvidedContent(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo repo, RepoResource res) throws IOException, RepoRejectException {

        log.debug("Client '{}' supports Expect 100/continue", request.getHeader(HttpHeaders.USER_AGENT));
        String sha1 = HttpUtils.getSha1Checksum(request);
        if (ChecksumType.sha1.isValid(sha1)) {
            log.debug("Expect continue deploy to '{}' with SHA1: {}", res.getRepoPath(), sha1);
            InputStream inputStream = null;
            try {
                inputStream = binaryStore.getBinary(sha1);
                uploadItemWithContent(request, response, repo, res, inputStream);
                return;
            } catch (BinaryNotFoundException e) {
                log.warn("Could not get original stream from with SHA1 '{}': {}", sha1, e.getMessage());
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
        uploadItemWithProvidedContent(request, response, repo, res);
    }

    private void uploadItemWithProvidedContent(ArtifactoryRequest request, ArtifactoryResponse response,
            LocalRepo repo, RepoResource res) throws IOException, RepoRejectException {
        InputStream inputStream = null;
        try {
            long remoteUploadStartTime = System.currentTimeMillis();
            inputStream = request.getInputStream();
            uploadItemWithContent(request, response, repo, res, inputStream);
            fireUploadTrafficEvent(res, remoteUploadStartTime);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void uploadItemWithContent(ArtifactoryRequest request, ArtifactoryResponse response, LocalRepo repo,
            RepoResource res, InputStream inputStream) throws RepoRejectException, IOException {
        //Update the last modified
        long lastModified = request.getLastModified() > 0 ? request.getLastModified() : System.currentTimeMillis();
        ((MutableRepoResourceInfo) res.getInfo()).setLastModified(lastModified);

        Properties properties = null;
        RepoPath repoPath = res.getRepoPath();
        if (authService.canAnnotate(repoPath)) {
            properties = request.getProperties();
        }
        SaveResourceContext.Builder contextBuilder = new SaveResourceContext.Builder(res, inputStream)
                .properties(properties);
        populateItemInfoFromHeaders(request, res, contextBuilder);
        try {
            RepoResource resource = repoService.saveResource(repo, contextBuilder.build());
            if (!resource.isFound()) {
                response.sendError(SC_NOT_FOUND, ((UnfoundRepoResource) resource).getDetail(), log);
                return;
            }
            sendSuccessfulResponse(request, response, repoPath, false);
        } catch (BadPomException bpe) {
            response.sendError(SC_CONFLICT, bpe.getMessage(), log);
        }
    }

    private void sendSuccessfulResponse(ArtifactoryRequest request, ArtifactoryResponse response, RepoPath repoPath,
            boolean isDirectory) throws IOException {
        String url = buildArtifactUrl(request, repoPath);
        successfulDeploymentResponseHelper.writeSuccessfulDeploymentResponse(repoService, response, repoPath,
                url, isDirectory);
    }

    private void fireUploadTrafficEvent(RepoResource resource, long remoteUploadStartTime) {
        if (remoteUploadStartTime > 0) {
            String remoteAddress = HttpUtils.getRemoteClientAddress();
            // fire upload event only if the resource is really uploaded from the remote client
            UploadEntry uploadEntry = new UploadEntry(resource.getRepoPath().getId(),
                    resource.getSize(), System.currentTimeMillis() - remoteUploadStartTime, remoteAddress);
            trafficService.handleTrafficEntry(uploadEntry);
        }
    }

    private String buildArtifactUrl(ArtifactoryRequest request, RepoPath repoPath) {
        return request.getServletContextUrl() + "/" + repoPath.getRepoKey() + "/" + repoPath.getPath();
    }

    private void populateItemInfoFromHeaders(ArtifactoryRequest request, RepoResource res,
            SaveResourceContext.Builder contextBuilder) {
        if (authService.isAdmin()) {

            setItemLastModifiedInfoFromHeaders(request, res);
            setItemCreatedInfoFromHeaders(request, contextBuilder);
            setItemCreatedByInfoFromHeaders(request, contextBuilder);
            setItemModifiedInfoFromHeaders(request, contextBuilder);
        }
    }

    private void setItemLastModifiedInfoFromHeaders(ArtifactoryRequest request, RepoResource res) {
        String lastModifiedString = request.getHeader(ArtifactoryRequest.LAST_MODIFIED);
        if (StringUtils.isNotBlank(lastModifiedString)) {
            long lastModified = Long.parseLong(lastModifiedString);
            if (lastModified > 0) {
                ((MutableRepoResourceInfo) res.getInfo()).setLastModified(lastModified);
            }
        }
    }

    private void setItemCreatedInfoFromHeaders(ArtifactoryRequest request, SaveResourceContext.Builder contextBuilder) {
        String createdString = request.getHeader(ArtifactoryRequest.CREATED);
        if (StringUtils.isNotBlank(createdString)) {
            long created = Long.parseLong(createdString);
            if (created > 0) {
                contextBuilder.created(created);
            }
        }
    }

    private void setItemCreatedByInfoFromHeaders(ArtifactoryRequest request,
            SaveResourceContext.Builder contextBuilder) {
        String createBy = request.getHeader(ArtifactoryRequest.CREATED_BY);
        if (StringUtils.isNotBlank(createBy)) {
            contextBuilder.createdBy(createBy);
        }
    }

    private void setItemModifiedInfoFromHeaders(ArtifactoryRequest request,
            SaveResourceContext.Builder contextBuilder) {
        String modifiedBy = request.getHeader(ArtifactoryRequest.MODIFIED_BY);
        if (StringUtils.isNotBlank(modifiedBy)) {
            contextBuilder.modifiedBy(modifiedBy);
        }
    }

    private void setFileInfoChecksums(ArtifactoryRequest request, MutableFileInfo fileInfo, boolean checksumDeploy) {
        if (checksumDeploy || (request instanceof InternalArtifactoryRequest &&
                ((InternalArtifactoryRequest) request).isTrustServerChecksums())) {
            fileInfo.createTrustedChecksums();
            return;
        }

        // set checksums if attached to the request headers
        String sha1 = HttpUtils.getSha1Checksum(request);
        String md5 = HttpUtils.getMd5Checksum(request);
        if (StringUtils.isNotBlank(sha1) || StringUtils.isNotBlank(md5)) {
            Set<ChecksumInfo> checksums = Sets.newHashSet();
            if (StringUtils.isNotBlank(sha1)) {
                log.debug("Found sha1 '{}' for file '{}", sha1, fileInfo.getRepoPath());
                checksums.add(new ChecksumInfo(ChecksumType.sha1, sha1, null));
            }
            if (StringUtils.isNotBlank(md5)) {
                log.debug("Found md5 '{}' for file '{}", md5, fileInfo.getRepoPath());
                checksums.add(new ChecksumInfo(ChecksumType.md5, md5, null));
            }
            fileInfo.setChecksums(checksums);
        }
    }

    private String adjustMavenSnapshotPath(LocalRepo repo, ArtifactoryRequest request) {
        String path = request.getPath();
        ModuleInfo itemModuleInfo = repo.getItemModuleInfo(path);
        MavenSnapshotVersionAdapter adapter = repo.getMavenSnapshotVersionAdapter();
        MavenSnapshotVersionAdapterContext context = new MavenSnapshotVersionAdapterContext(
                repo.getRepoPath(path), itemModuleInfo);

        Properties properties = request.getProperties();
        if (properties != null) {
            String timestamp = properties.getFirst("build.timestamp");
            if (StringUtils.isNotBlank(timestamp)) {
                context.setTimestamp(timestamp);
            }
        }
        String adjustedPath = adapter.adaptSnapshotPath(context);
        if (!adjustedPath.equals(path)) {
            log.debug("Snapshot file path '{}' adjusted to: '{}'", path, adjustedPath);
        }
        return adjustedPath;
    }

}