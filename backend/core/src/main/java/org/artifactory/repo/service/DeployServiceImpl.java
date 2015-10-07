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

package org.artifactory.repo.service;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.artifactory.api.artifact.ArtifactInfo;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.DeployService;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.request.UploadService;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.InternalArtifactoryResponse;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.util.GlobalExcludes;
import org.artifactory.util.PathUtils;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Provides artifacts deploy services from the UI.
 *
 * @author Yossi Shaul
 */
@Service
public class DeployServiceImpl implements DeployService {
    private static final Logger log = LoggerFactory.getLogger(DeployServiceImpl.class);

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private MavenService mavenService;

    @Override
    public void deploy(RepoDescriptor targetRepo, UnitInfo artifactInfo, File file, Properties properties)
            throws RepoRejectException {
        String pomString = mavenService.getPomModelString(file);
        deploy(targetRepo, artifactInfo, file, pomString, false, false, properties);
    }

    @Override
    public void deploy(RepoDescriptor targetRepo, UnitInfo artifactInfo, File fileToDeploy, String pomString,
            boolean forceDeployPom, boolean partOfBundleDeploy, Properties properties) throws RepoRejectException {
        String path = artifactInfo.getPath();
        if (!artifactInfo.isValid()) {
            throw new IllegalArgumentException("Invalid unit info for '" + path + "'.");
        }

        //Sanity check
        if (targetRepo == null) {
            throw new IllegalArgumentException("No target repository selected for deployment.");
        }
        final LocalRepo localRepo = repositoryService.localRepositoryByKey(targetRepo.getKey());
        if (localRepo == null) {
            throw new IllegalArgumentException("No target repository found for deployment.");
        }

        RepoPath repoPath = InternalRepoPathFactory.create(targetRepo.getKey(), path);

        // upload the main file
        try {
            ArtifactoryDeployRequest request = new ArtifactoryDeployRequestBuilder(repoPath)
                    .fileToDeploy(fileToDeploy).properties(properties).build();
            request.setSkipJarIndexing(partOfBundleDeploy);
            InternalArtifactoryResponse response = new InternalArtifactoryResponse();
            uploadService.upload(request, response);
            assertNotFailedRequest(fileToDeploy.getName(), response);
        } catch (IOException e) {
            String msg = "Cannot deploy file " + fileToDeploy.getName() + ". Cause: " + e.getMessage();
            log.debug(msg, e);
            throw new RepositoryRuntimeException(msg, e);
        }

        //Handle extra pom deployment - add the metadata with the generated pom file to the artifact
        if (forceDeployPom && artifactInfo.isMavenArtifact() && StringUtils.isNotBlank(pomString)) {
            MavenArtifactInfo mavenArtifactInfo = (MavenArtifactInfo) artifactInfo;
            RepoPath pomPath = InternalRepoPathFactory.create(repoPath.getParent(),
                    mavenArtifactInfo.getArtifactId() + "-" + mavenArtifactInfo.getVersion() + ".pom");
            RepoPath uploadPomPath = InternalRepoPathFactory.create(targetRepo.getKey(), pomPath.getPath());
            try {
                ArtifactoryDeployRequest pomRequest = new ArtifactoryDeployRequestBuilder(uploadPomPath)
                        .inputStream(IOUtils.toInputStream(pomString, Charsets.UTF_8.name()))
                        .contentLength(pomString.getBytes().length)
                        .lastModified(fileToDeploy.lastModified())
                        .properties(properties)
                        .build();
                InternalArtifactoryResponse pomResponse = new InternalArtifactoryResponse();
                // upload the POM if needed
                uploadService.upload(pomRequest, pomResponse);
                assertNotFailedRequest(fileToDeploy.getName(), pomResponse);
            } catch (IOException e) {
                String msg = "Cannot deploy file " + pomPath.getName() + ". Cause: " + e.getMessage();
                log.debug(msg, e);
                throw new RepositoryRuntimeException(msg, e);
            }
        }
    }

    private void assertNotFailedRequest(String deployedFileName, InternalArtifactoryResponse response)
            throws RepoRejectException {
        if (!response.isSuccessful()) {
            StringBuilder errorMessageBuilder = new StringBuilder("Cannot deploy file '").append(deployedFileName).
                    append("'. ");
            String statusMessage = response.getStatusMessage();
            if (StringUtils.isNotBlank(statusMessage)) {
                errorMessageBuilder.append(statusMessage);
                if (!StringUtils.endsWith(statusMessage, ".")) {
                    errorMessageBuilder.append(".");
                }
            } else {
                errorMessageBuilder.append("Please view the logs for further information.");
            }
            throw new RepoRejectException(errorMessageBuilder.toString());
        }
    }

    @Override
    public void deployBundle(File bundle, RealRepoDescriptor targetRepo, BasicStatusHolder status, boolean failFast) {
        deployBundle(bundle, targetRepo, status, failFast, "", null);
    }

    @Override
    public void deployBundle(File bundle, RealRepoDescriptor targetRepo, final BasicStatusHolder status,
            boolean failFast, String prefix, Properties properties) {
        long start = System.currentTimeMillis();
        if (!bundle.exists()) {
            String message =
                    "Specified location '" + bundle + "' does not exist. Deployment aborted.";
            status.error(message, log);
            return;
        }
        File extractFolder;
        try {
            extractFolder = extractArchive(status, bundle);
        } catch (Exception e) {
            status.error(e.getLocalizedMessage(), e, log);
            return;
        }
        if (extractFolder == null) {
            //We have errors
            return;
        }
        try {
            IOFileFilter deployableFilesFilter = new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    if (NamingUtils.isSystem(file.getAbsolutePath()) || GlobalExcludes.isInGlobalExcludes(file) ||
                            file.getName().contains(MavenNaming.MAVEN_METADATA_NAME)) {
                        status.debug("Excluding '" + file.getAbsolutePath() + "' from bundle deployment.", log);
                        return false;
                    }

                    return true;
                }
            };
            List<File> archiveContent = Lists.newArrayList(FileUtils.listFiles(
                    extractFolder, deployableFilesFilter, DirectoryFileFilter.DIRECTORY));
            Collections.sort(archiveContent);

            Repo repo = repositoryService.repositoryByKey(targetRepo.getKey());
            for (File file : archiveContent) {
                String parentPath = extractFolder.getAbsolutePath();
                String filePath = file.getAbsolutePath();
                String relPath = PathUtils.trimSlashes(
                        prefix + "/" + PathUtils.getRelativePath(parentPath, filePath)).toString();

                ModuleInfo moduleInfo = repo.getItemModuleInfo(relPath);
                if (MavenNaming.isPom(file.getName())) {
                    try {
                        mavenService.validatePomFile(file, relPath, moduleInfo,
                                targetRepo.isSuppressPomConsistencyChecks());
                    } catch (Exception e) {
                        String msg = "The pom: " + file.getName() +
                                " could not be validated, and thus was not deployed.";
                        status.error(msg, e, log);
                        if (failFast) {
                            return;
                        }
                        continue;
                    }
                }

                try {
                    getTransactionalMe().deploy(targetRepo, new ArtifactInfo(relPath), file, null, false, true,
                            properties);
                } catch (IllegalArgumentException iae) {
                    status.error(iae.getMessage(), iae, log);
                    if (failFast) {
                        return;
                    }
                } catch (Exception e) {
                    // Fail fast
                    status.error("Error during deployment: " + e.getMessage(), e, log);
                    if (failFast) {
                        return;
                    }
                }
            }

            String bundleName = bundle.getName();
            String timeTaken = DurationFormatUtils.formatPeriod(start, System.currentTimeMillis(), "s");
            int archiveContentSize = archiveContent.size();

            status.status("Successfully deployed " + archiveContentSize + " artifacts from archive: " + bundleName
                    + " (" + timeTaken + " seconds).", log);
        } catch (Exception e) {
            status.error(e.getMessage(), e, log);
        } finally {
            FileUtils.deleteQuietly(extractFolder);
        }
    }

    private File extractArchive(BasicStatusHolder status, File archive) throws Exception {
        String archiveName = archive.getName();
        String fixedArchiveName = new String(archiveName.getBytes(Charsets.UTF_8.name()), Charsets.UTF_8);
        File fixedArchive = new File(archive.getParentFile(), fixedArchiveName);
        try {
            if (!fixedArchive.exists()) {
                FileUtils.moveFile(archive, fixedArchive);
            }
        } catch (IOException e) {
            throw new Exception("Could not encode archive name to UTF-8.", e);
        }
        File extractFolder = new File(ContextHelper.get().getArtifactoryHome().getTempUploadDir(),
                fixedArchive.getName() + "_extracted_" + System.currentTimeMillis());
        if (extractFolder.exists()) {
            //Clean up any existing folder
            try {
                FileUtils.deleteDirectory(extractFolder);
            } catch (IOException e) {
                status.error("Could not delete existing extracted archive folder: " +
                        extractFolder.getAbsolutePath() + ".", e, log);
                return null;
            }
        }
        try {
            FileUtils.forceMkdir(extractFolder);
        } catch (IOException e) {
            log.error("Could not created the extracted archive folder: " +
                    extractFolder.getAbsolutePath() + ".", log);
            return null;
        }

        try {
            ZipUtils.extract(fixedArchive, extractFolder);
        } catch (Exception e) {
            FileUtils.deleteQuietly(extractFolder);
            if (e.getMessage() == null) {
                String errorMessage;
                if (e instanceof IllegalArgumentException) {
                    errorMessage =
                            "Please make sure the textual values in the archive are encoded in UTF-8.";
                } else {
                    errorMessage = "Please ensure the integrity of the selected archive";
                }
                throw new Exception(errorMessage, e);
            }
            throw e;
        }
        return extractFolder;
    }

    private static DeployService getTransactionalMe() {
        return InternalContextHelper.get().beanForType(DeployService.class);
    }
}
