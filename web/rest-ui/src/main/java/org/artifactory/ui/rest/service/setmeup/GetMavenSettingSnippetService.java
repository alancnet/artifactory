package org.artifactory.ui.rest.service.setmeup;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.maven.MavenSettings;
import org.artifactory.api.maven.MavenSettingsMirror;
import org.artifactory.api.maven.MavenSettingsRepository;
import org.artifactory.api.maven.MavenSettingsServer;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.setmeup.MavenSettingModel;
import org.artifactory.ui.rest.model.setmeup.ScriptDownload;
import org.artifactory.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetMavenSettingSnippetService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetMavenSettingSnippetService.class);

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    MavenService mavenService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean downloadScript = Boolean.valueOf(request.getQueryParamByKey("downloadScript"));
        MavenSettingModel mavenSettingModel = (MavenSettingModel) request.getImodel();
        String servletContextUrl = HttpUtils.getServletContextUrl(request.getServletRequest());
        // generate maven settings
        String mavenSnippet = generateSettings(servletContextUrl, mavenSettingModel, response);
        if (downloadScript) {
            String mavenSnippetForDownload = getMavenSnippetForDownload(mavenSnippet);
            //download build.gradle file
            ScriptDownload scriptDownload = new ScriptDownload();
            scriptDownload.setFileContent(mavenSnippetForDownload);
            ((StreamRestResponse) response).setDownload(true);
            ((StreamRestResponse) response).setDownloadFile("settings.xml");
            response.iModel(scriptDownload);
        } else {
            // update maven setting model
            MavenSettingModel mavenSnippetModel = new MavenSettingModel(mavenSnippet);
            mavenSnippetModel.clearProps();
            response.iModel(mavenSnippetModel);
        }
    }

    /**
     * Prepares a local maven settings model to be used by the maven service.<br>
     * Published as a protected method because a simple extension point is needed by the dashboard to append servers to
     * the model.
     *
     * @param servletContextUrl Current context URL
     * @return Local settings model
     */
    protected MavenSettings assembleLocalSettingsModel(String servletContextUrl,
            MavenSettingModel mavenSettingModel) {
        //Build settings object from the user selections in the panel
        MavenSettings mavenSettings = new MavenSettings(servletContextUrl);

        //Add release and snapshot choices
        String releases = mavenSettingModel.getRelease();
        mavenSettings.addReleaseRepository(new MavenSettingsRepository("central", releases, false));
        String snapshots = mavenSettingModel.getSnapshot();
        mavenSettings.addReleaseRepository(new MavenSettingsRepository("snapshots", snapshots, true));

        //Add plugin release and snapshot choices
        String pluginReleases = mavenSettingModel.getPluginRelease();
        mavenSettings.addPluginRepository(new MavenSettingsRepository("central", pluginReleases, false));
        String pluginSnapshots = mavenSettingModel.getPluginSnapshot();
        mavenSettings.addPluginRepository(new MavenSettingsRepository("snapshots", pluginSnapshots, true));

        //Add the "mirror any" repo, if the user has selected it
        if (!StringUtils.isEmpty(mavenSettingModel.getMirror())) {
            String mirror = mavenSettingModel.getMirror();
            mavenSettings.addMirrorRepository(new MavenSettingsMirror(mirror, mirror, "*"));
        }

        if (!authorizationService.isAnonymous() || !authorizationService.isAnonAccessEnabled()) {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            FilteredResourcesAddon filteredResourcesWebAddon =
                    addonsManager.addonByType(FilteredResourcesAddon.class);
            String currentUsername = filteredResourcesWebAddon.getGeneratedSettingsUsernameTemplate();
            String currentPassword = filteredResourcesWebAddon.getGeneratedSettingsUserCredentialsTemplate(true);

            mavenSettings.addServer(new MavenSettingsServer("central", currentUsername, currentPassword));
            mavenSettings.addServer(new MavenSettingsServer("snapshots", currentUsername, currentPassword));
        }
        return mavenSettings;
    }

    /**
     * generate maven settings
     * @param servletContextUrl - servlet context
     * @param mavenSettingModel - maven settings model
     * @param restResponse - rest response
     * @return
     */
    public String generateSettings(String servletContextUrl,MavenSettingModel mavenSettingModel,RestResponse restResponse) {
        //Make sure URL ends with slash
        if (!servletContextUrl.endsWith("/")) {
            servletContextUrl += "/";
        }

        MavenSettings mavenSettings = assembleLocalSettingsModel(servletContextUrl,mavenSettingModel);
        try {
            //Generate XML settings content
            return mavenService.generateSettings(mavenSettings);
        } catch (
                IOException ioe) {
            String message = ioe.getMessage();
           log.error(message);
            restResponse.error("Maven settings could no be generated.");
        }
        return null;
    }

    /**
     * generate maven settigs for download
     *
     * @param mavenTemplateSnippet - maven template snippet
     * @return - maven settings
     */
    String getMavenSnippetForDownload(String mavenTemplateSnippet) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        FilteredResourcesAddon filteredResourcesWebAddon = addonsManager.addonByType(
                FilteredResourcesAddon.class);
        String filtered = null;
        try {
            filtered = filteredResourcesWebAddon.filterResource(null,
                    (Properties) InfoFactoryHolder.get().createProperties(),
                    new StringReader(mavenTemplateSnippet));
        } catch (Exception e) {
            log.error("Unable to filter settings: " + e.getMessage());

        }
        return filtered;
    }

}
