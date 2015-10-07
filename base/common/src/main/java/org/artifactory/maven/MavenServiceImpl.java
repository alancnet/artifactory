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

package org.artifactory.maven;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.maven.MavenSettings;
import org.artifactory.api.maven.MavenSettingsMirror;
import org.artifactory.api.maven.MavenSettingsRepository;
import org.artifactory.api.maven.MavenSettingsServer;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.mime.MavenNaming;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

/**
 * Main implementation of the MavenService interface
 *
 * @author Noam Tenne
 */
@Service
public class MavenServiceImpl implements MavenService {
    private static final Logger log = LoggerFactory.getLogger(MavenServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSettings(MavenSettings mavenSettings) throws IOException {
        Settings settings = transformSettings(mavenSettings);
        SettingsXpp3Writer settingsWriter = new SettingsXpp3Writer();
        StringWriter stringWriter = new StringWriter();
        settingsWriter.write(stringWriter, settings);
        String settingsXml = stringWriter.toString();
        settingsXml = settingsXml.replace("<settings>", "<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\"\n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\n" +
                "                      http://maven.apache.org/xsd/settings-1.0.0.xsd\">");
        return settingsXml;
    }

    /**
     * Recieves artifactories MavenSettings object and transforms it to maven's Settings object
     *
     * @param mavenSettings Settings to transform
     * @return Settings - Transformed settings
     */
    private Settings transformSettings(MavenSettings mavenSettings) {
        String contextUrl = mavenSettings.getUrl();

        Settings settings = new Settings();

        Profile profile = new Profile();
        profile.setId("artifactory");

        //Add plugin and releases repositories to the profile
        addReleaseRepositories(contextUrl, profile, mavenSettings.getReleaseRepositories());
        addPluginRepositories(contextUrl, profile, mavenSettings.getPluginRepositories());
        settings.addProfile(profile);
        settings.setActiveProfiles(Collections.singletonList(profile.getId()));

        //Add mirrors to the settings
        addMirrors(contextUrl, settings, mavenSettings.getMirrorRepositories());

        addServers(settings, mavenSettings.getServers());

        return settings;
    }

    /**
     * Adds a list of release repository configurations to the maven profile object
     *
     * @param contextUrl   System URL
     * @param profile      Maven profile to append to
     * @param repositories Repositories to add to the profile
     */
    private void addReleaseRepositories(String contextUrl, Profile profile,
            List<MavenSettingsRepository> repositories) {
        for (MavenSettingsRepository repository : repositories) {
            Repository repoToAdd = new Repository();
            repoToAdd.setId(repository.getId());
            String repositoryName = repository.getName();
            repoToAdd.setName(repositoryName);
            repoToAdd.setUrl(contextUrl + repositoryName);

            boolean handlesSnapshots = repository.isHandlesSnapshots();
            RepositoryPolicy snapshotPolicy = new RepositoryPolicy();
            snapshotPolicy.setEnabled(handlesSnapshots);
            repoToAdd.setSnapshots(snapshotPolicy);

            profile.addRepository(repoToAdd);
        }
    }

    /**
     * Adds a list of plugin repository configurations to the maven profile object
     *
     * @param contextUrl   System URL
     * @param profile      Maven profile to append to
     * @param repositories Repositories to add to the profile
     */
    private void addPluginRepositories(String contextUrl, Profile profile, List<MavenSettingsRepository> repositories) {
        for (MavenSettingsRepository repository : repositories) {
            Repository repoToAdd = new Repository();
            repoToAdd.setId(repository.getId());
            String repositoryName = repository.getName();
            repoToAdd.setName(repositoryName);
            repoToAdd.setUrl(contextUrl + repositoryName);

            boolean handlesSnapshots = repository.isHandlesSnapshots();
            RepositoryPolicy snapshotPolicy = new RepositoryPolicy();
            snapshotPolicy.setEnabled(handlesSnapshots);
            repoToAdd.setSnapshots(snapshotPolicy);

            profile.addPluginRepository(repoToAdd);
        }
    }

    /**
     * Adds a list of mirror configurations to the maven settings object
     *
     * @param contextUrl System URL
     * @param settings   Maven Settings to append to
     * @param mirrors    Mirrors to add to the settings
     */
    private void addMirrors(String contextUrl, Settings settings, List<MavenSettingsMirror> mirrors) {
        for (MavenSettingsMirror mirror : mirrors) {
            Mirror mirrorToAdd = new Mirror();
            String mirrorId = mirror.getId();
            mirrorToAdd.setId(mirrorId);
            mirrorToAdd.setName(mirrorId);
            mirrorToAdd.setUrl(contextUrl + mirrorId);
            mirrorToAdd.setMirrorOf(mirror.getMirrorOf());

            settings.addMirror(mirrorToAdd);
        }
    }

    /**
     * Adds a list of server configurations to the maven settings object
     *
     * @param settings Maven Settings to append to
     * @param servers  Servers to add to the settings
     */
    private void addServers(Settings settings, List<MavenSettingsServer> servers) {
        for (MavenSettingsServer server : servers) {
            Server serverToAdd = new Server();
            serverToAdd.setId(server.getId());

            String username = server.getUsername();
            String password = server.getPassword();

            if (StringUtils.isNotBlank(username)) {
                serverToAdd.setUsername(username);
            }
            if (StringUtils.isNotBlank(password)) {
                serverToAdd.setPassword(password);
            }
            settings.addServer(serverToAdd);
        }
    }

    @Override
    public void validatePomContent(String pomContent, String relPath, ModuleInfo moduleInfo,
            boolean suppressPomConsistencyChecks) throws IOException {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        File tempFile = File.createTempFile("pom.validation", ".tmp", artifactoryHome.getTempWorkDir());
        try {
            FileUtils.writeStringToFile(tempFile, pomContent, "utf-8");
            validatePomFile(tempFile, relPath, moduleInfo, suppressPomConsistencyChecks);
        } finally {
            FileUtils.forceDelete(tempFile);
        }
    }

    @Override
    public void validatePomFile(File pomFile, String relPath, ModuleInfo moduleInfo,
            boolean suppressPomConsistencyChecks) throws BadPomException {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(pomFile));
            new PomTargetPathValidator(relPath, moduleInfo).validate(inputStream, suppressPomConsistencyChecks);
        } catch (Exception e) {
            String message = "Error while validating POM for path: " + relPath +
                    ". Please assure the validity of the POM file.";
            throw new BadPomException(message);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public MavenArtifactInfo getMavenArtifactInfo(File uploadedFile) {
        return MavenModelUtils.artifactInfoFromFile(uploadedFile);
    }

    @Override
    public String getPomModelString(File file) {
        if (MavenNaming.isPom(file.getAbsolutePath())) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                return IOUtils.toString(fileInputStream);
            } catch (IOException e) {
                log.error("The following error occurred while reading {} {}", file.getAbsolutePath(), e);
            }
        }
        String pomFromJar = MavenModelUtils.getPomFileAsStringFromJar(file);
        if (StringUtils.isNotBlank(pomFromJar)) {
            try {
                MavenModelUtils.stringToMavenModel(pomFromJar);
                return pomFromJar;
            } catch (RepositoryRuntimeException rre) {
                log.error("Failed to validate the model of the POM file within '{}'.", file.getAbsolutePath());
            }
        }
        MavenArtifactInfo model = getMavenArtifactInfo(file);
        return MavenModelUtils.mavenModelToString(MavenModelUtils.toMavenModel(model));
    }
}