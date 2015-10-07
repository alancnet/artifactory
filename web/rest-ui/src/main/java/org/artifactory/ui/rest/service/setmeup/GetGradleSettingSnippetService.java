package org.artifactory.ui.rest.service.setmeup;

import org.apache.commons.io.IOUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.setmeup.GradleSettingModel;
import org.artifactory.ui.rest.model.setmeup.ScriptDownload;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

/**
 * @author chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetGradleSettingSnippetService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetGradleSettingSnippetService.class);

    private final String JCENTER_DUMMY_REPO_KEY = "Bintray -> jcenter";
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
        GradleSettingModel gradleSettingModel = (GradleSettingModel) request.getImodel();
        boolean downloadScript = Boolean.valueOf(request.getQueryParamByKey("downloadScript"));
        boolean downloadGradleProps = Boolean.valueOf(request.getQueryParamByKey("gradleProps"));
        String servletContextUrl = HttpUtils.getServletContextUrl(request.getServletRequest());
        // generate maven settings
        String gradleSnippet = generateSettings(servletContextUrl, gradleSettingModel, response);
        if (downloadGradleProps) {
            downLoadGradleProps(response, servletContextUrl);
            return;
        }
        if (downloadScript) {
            downloadGradleBuildFile(response, gradleSnippet);
            return;
        } else {
            // update maven setting model
            GradleSettingModel mavenSnippetModel = new GradleSettingModel(gradleSnippet);
            mavenSnippetModel.clearProps();
            response.iModel(mavenSnippetModel);
        }
    }

    /**
     * download Gradle build File
     *
     * @param response      - encapsulate data related to response
     * @param gradleSnippet - gradle snippet file
     */
    private void downloadGradleBuildFile(RestResponse response, String gradleSnippet) {
        //download build.gradle file
        ScriptDownload scriptDownload = new ScriptDownload();
        scriptDownload.setFileContent(gradleSnippet);
        ((StreamRestResponse) response).setDownload(true);
        ((StreamRestResponse) response).setDownloadFile("build.gradle");
        response.iModel(scriptDownload);
    }

    /**
     * download gradle build props
     *
     * @param response          - encapsulate data related to response
     * @param servletContextUrl - servlet context
     */
    private void downLoadGradleProps(RestResponse response, String servletContextUrl) {
        String gradleProperties = getGradleProperties(response, servletContextUrl);
        ScriptDownload scriptDownload = new ScriptDownload();
        scriptDownload.setFileContent(gradleProperties);
        ((StreamRestResponse) response).setDownload(true);
        ((StreamRestResponse) response).setDownloadFile("gradle.properties");
        response.iModel(scriptDownload);
    }

    /**
     *  generate gradle settings
     * @return
     */
    public String generateSettings(String servletContextUrl,GradleSettingModel gradleSettingModel,RestResponse response) {
        Properties templateProperties = getTemplateProperties(servletContextUrl,gradleSettingModel,response);
        return replaceValuesAndGetTemplateValue("/build.gradle.template", templateProperties,response);
    }

    /**
     *
     * @return
     */
    private Properties getTemplateProperties(String servletContextUrl,GradleSettingModel gradleSettingModel,
            RestResponse response) {
        Properties repoDetailsProperties = new Properties();

        // General
        repoDetailsProperties.setProperty("artifactory.contextUrl", servletContextUrl);
        repoDetailsProperties.setProperty("gradle.build.1tab", "    ");
        repoDetailsProperties.setProperty("gradle.build.2tabs", "        ");
        repoDetailsProperties.setProperty("gradle.build.3tabs", "            ");
        repoDetailsProperties.setProperty("gradle.build.4tabs", "                ");

        //jcenter overrides both maven and ivy
        if(gradleSettingModel.getPluginRepoKey().equals(JCENTER_DUMMY_REPO_KEY)) {
            repoDetailsProperties.setProperty("maven.repo", "jcenter()");
            repoDetailsProperties.setProperty("ivy.repo", "");
            // Plugins
        } else {
            repoDetailsProperties.setProperty("plugins.repository.url",
                    getFullRepositoryUrl(servletContextUrl, gradleSettingModel.getPluginRepoKey()));
            if (gradleSettingModel.getPluginUseMaven()) {
                repoDetailsProperties.setProperty("maven.repo",
                        replaceValuesAndGetTemplateValue("/build.gradle.maven.repo.template", repoDetailsProperties,response));
            } else {
                repoDetailsProperties.setProperty("maven.repo", "");
            }

            if (gradleSettingModel.getPluginUseIvy()) {
                repoDetailsProperties.setProperty("ivy.repo",
                        replaceValuesAndGetTemplateValue("/build.gradle.ivy.repo.template", repoDetailsProperties,response));
            } else {
                repoDetailsProperties.setProperty("ivy.repo", "");
            }
        }
        // Publisher
        if (gradleSettingModel.getPublisherUseIvy()) {
            repoDetailsProperties.setProperty("ivy.publisher",
                    replaceValuesAndGetTemplateValue("/build.gradle.ivy.publisher.template", repoDetailsProperties,response));
            repoDetailsProperties.setProperty("libs.publisher.ivy.pattern",
                    getDescriptorPattern(getLayout(gradleSettingModel.getLibsPublisherLayouts())));
            repoDetailsProperties.setProperty("libs.publisher.artifact.pattern",
                    getArtifactPattern(getLayout(gradleSettingModel.getLibsPublisherLayouts())));
            repoDetailsProperties.setProperty("libs.publisher.maven.compatible",
                    Boolean.toString(getFullDescriptorPattern(servletContextUrl,
                            gradleSettingModel.getLibsPublisherRepoKey()
                            , getLayout(gradleSettingModel.getLibsPublisherLayouts())).contains("pom")));
        } else {
            repoDetailsProperties.setProperty("ivy.publisher", "");
        }
        repoDetailsProperties.setProperty("libs.publisher.repoKey", gradleSettingModel.getLibsPublisherRepoKey());
        repoDetailsProperties.setProperty("libs.publisher.maven", Boolean.toString(gradleSettingModel.getPublisherUseMaven()));

        // Resolver
        if (gradleSettingModel.getResolverUseIvy()) {
            repoDetailsProperties.setProperty("ivy.resolver",
                    replaceValuesAndGetTemplateValue("/build.gradle.ivy.resolver.template", repoDetailsProperties,response));
            repoDetailsProperties.setProperty("libs.resolver.ivy.pattern",
                    getDescriptorPattern(getLayout(gradleSettingModel.getLibsResolverLayout())));
            repoDetailsProperties.setProperty("libs.resolver.artifact.pattern",
                    getArtifactPattern(getLayout(gradleSettingModel.getLibsResolverLayout())));
            repoDetailsProperties.setProperty("libs.resolver.maven.compatible",
                    Boolean.toString(getFullDescriptorPattern(servletContextUrl,gradleSettingModel.getLibsResolverRepoKey(),getLayout(gradleSettingModel.getLibsResolverLayout())).contains("pom")));
        } else {
            repoDetailsProperties.setProperty("ivy.resolver", "");
        }
        repoDetailsProperties.setProperty("libs.resolver.repoKey", gradleSettingModel.getLibsResolverRepoKey());
        repoDetailsProperties.setProperty("libs.resolver.maven", Boolean.toString(gradleSettingModel.getResolverUseMaven()));

        // Credentials
        Properties credentialProperties = new Properties();
        String resolveCreds;
        String publishCreds;
        String pluginsCreds;
        if (!authorizationService.isAnonymous() || !authorizationService.isAnonAccessEnabled()) {
            credentialProperties.setProperty("creds.line.break", "\n            ");
            resolveCreds = replaceValuesAndGetTemplateValue("/build.gradle.resolve.creds.template",
                    credentialProperties,response);
            publishCreds = replaceValuesAndGetTemplateValue("/build.gradle.publish.creds.template",
                    credentialProperties,response);
            pluginsCreds = replaceValuesAndGetTemplateValue("/build.gradle.creds.template", credentialProperties,response);
        } else {
            resolveCreds = "";
            publishCreds = "";
            pluginsCreds = "";
        }
        repoDetailsProperties.setProperty("resolve.creds", resolveCreds);
        repoDetailsProperties.setProperty("publish.creds", publishCreds);
        repoDetailsProperties.setProperty("repo.creds", pluginsCreds);

        return repoDetailsProperties;
    }

    /**
     *
     * @param templateResourcePath
     * @param templateProperties
     * @return
     */
    private String replaceValuesAndGetTemplateValue(String templateResourcePath,
            Properties templateProperties,RestResponse response) {
        InputStream gradleInitTemplateStream = null;
        try {
            gradleInitTemplateStream = getClass().getResourceAsStream(templateResourcePath);
            String gradleTemplate = IOUtils.toString(gradleInitTemplateStream);
            PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");
            return propertyPlaceholderHelper.replacePlaceholders(gradleTemplate, templateProperties);
        } catch (IOException e) {
            String errorMessage = "An error occurred while preparing the Gradle Init Script template: ";
            response.error(errorMessage + e.getMessage());
            log.error(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(gradleInitTemplateStream);
        }
        return "";
    }

    public String getFullRepositoryUrl(String servletContextUrl,String repoKey) {
        return getFullUrl(servletContextUrl,repoKey);
    }

    private String getFullUrl(String host, String path) {
        return new StringBuilder(host).append("/").append(path).toString();
    }

    /**
     *
     * @param layoutName
     * @return
     */
    private RepoLayout getLayout(String layoutName){
        RepoLayout layout = null;
        List<RepoLayout> repoLayouts = centralConfigService.getDescriptor().getRepoLayouts();
       for (RepoLayout repoLayout : repoLayouts){
           if (repoLayout.getName().equals(layoutName)){
               layout = repoLayout;
               break;
           }
       }
        return layout;
    }

    public String getDescriptorPattern(RepoLayout layout) {
        return RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout);
    }


    public String getArtifactPattern(RepoLayout layout) {
        return RepoLayoutUtils.getArtifactLayoutAsIvyPattern(layout);
    }

    public String getFullDescriptorPattern(String servletContextUrl,String repoKey,RepoLayout layout) {
        return getFullUrl(getFullUrl(servletContextUrl,repoKey),
                RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout));
    }

    /**
     * @param restResponse
     * @param servletContextUrl
     * @return
     */
    private String getGradleProperties(RestResponse restResponse, String servletContextUrl) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        FilteredResourcesAddon filteredResourcesWebAddon = addonsManager.addonByType(
                FilteredResourcesAddon.class);

        java.util.Properties credentialProperties = new java.util.Properties();
        credentialProperties.setProperty("auth.username",
                filteredResourcesWebAddon.getGeneratedSettingsUsernameTemplate());

        credentialProperties.setProperty("auth.password",
                filteredResourcesWebAddon.getGeneratedSettingsUserCredentialsTemplate(false));

        credentialProperties.setProperty("auth.contextUrl", servletContextUrl);

        String content = replaceValuesAndGetTemplateValue("/gradle.properties.template", credentialProperties,
                restResponse);
        try {
            String filtered = filteredResourcesWebAddon.filterResource(null,
                    (org.artifactory.md.Properties) InfoFactoryHolder.get().createProperties(),
                    new StringReader(content));
            return filtered;
        } catch (Exception e) {
            log.error("Unable to filter gradle build properties file: " + e.getMessage());
            restResponse.error(e.getMessage());
        }
        return content;
    }

}
