package org.artifactory.ui.rest.service.setmeup;

import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.filteredresources.FilteredResourcesAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.setmeup.IvySettingModel;
import org.artifactory.ui.rest.model.setmeup.ScriptDownload;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetIvySettingSnippetService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetIvySettingSnippetService.class);

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        boolean downloadScript = Boolean.valueOf(request.getQueryParamByKey("downloadScript"));
        IvySettingModel ivySetting = (IvySettingModel) request.getImodel();
        String servletContextUrl = HttpUtils.getServletContextUrl(request.getServletRequest());
        String ivySnippet = generateSettings(servletContextUrl, response, ivySetting);
        if (downloadScript) {
            //download build.gradle file
            ScriptDownload scriptDownload = new ScriptDownload();
            scriptDownload.setFileContent(ivySnippet);
            ((StreamRestResponse) response).setDownload(true);
            ((StreamRestResponse) response).setDownloadFile("ivysettings.xml");
            response.iModel(scriptDownload);
        } else {
            IvySettingModel ivySettingModel = new IvySettingModel(ivySnippet);
            ivySettingModel.clearProps();
            response.iModel(ivySettingModel);
        }
    }

    public String generateSettings(String servletContextUrl, RestResponse response, IvySettingModel ivySetting) {
        Document document = new Document();
        Element rootNode = new Element("ivy-settings");
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        FilteredResourcesAddon filteredResourcesWebAddon =
                addonsManager.addonByType(FilteredResourcesAddon.class);
        Element settingsElement = new Element("settings");
        settingsElement.setAttribute("defaultResolver", "main");
        rootNode.addContent(settingsElement);

        if (!authorizationService.isAnonymous() || !authorizationService.isAnonAccessEnabled()) {
            rootNode.addContent(
                    new Comment("Authentication required for publishing (deployment). 'Artifactory Realm' is " +
                            "the realm used by Artifactory so don't change it."));

            Element credentialsElement = new Element("credentials");
            try {
                credentialsElement.setAttribute("host", new URL(servletContextUrl).getHost());
            } catch (MalformedURLException e) {
                String errorMessage =
                        "An error occurred while decoding the servlet context URL for the credentials host attribute: ";
                response.error(errorMessage + e.getMessage());
                log.error(errorMessage, e);
            }
            credentialsElement.setAttribute("realm", "Artifactory Realm");

            credentialsElement.setAttribute("username",
                    filteredResourcesWebAddon.getGeneratedSettingsUsernameTemplate());

            credentialsElement.setAttribute("passwd", "@PASS_ATTR_PLACEHOLDER@");

            rootNode.addContent(credentialsElement);
        }

        Element resolversElement = new Element("resolvers");

        Element chainElement = new Element("chain");
        chainElement.setAttribute("name", "main");

        String resolverName = ivySetting.getLibsResolverName();
        resolverName = StringUtils.isNotBlank(resolverName) ? resolverName : "public";

        if (ivySetting.getUseIbiblioResolver()) {

            Element ibiblioElement = new Element("ibiblio");
            ibiblioElement.setAttribute("name", resolverName);
            ibiblioElement.setAttribute("m2compatible", Boolean.TRUE.toString());
            ibiblioElement.setAttribute("root", getFullRepositoryUrl(servletContextUrl, ivySetting.getLibsRepo()));
            chainElement.addContent(ibiblioElement);
        } else {

            Element urlElement = new Element("url");
            urlElement.setAttribute("name", resolverName);

            urlElement.setAttribute("m2compatible", Boolean.toString(ivySetting.getM2Compatible()));

            Element artifactPatternElement = new Element("artifact");
            artifactPatternElement.setAttribute("pattern", getFullArtifactPattern(servletContextUrl,
                    ivySetting.getLibsRepo(), getLayout(ivySetting.getLibsRepoLayout())));
            urlElement.addContent(artifactPatternElement);

            Element ivyPatternElement = new Element("ivy");
            ivyPatternElement.setAttribute("pattern", getFullDescriptorPattern(servletContextUrl,
                    ivySetting.getLibsRepo(), getLayout(ivySetting.getLibsRepoLayout())));
            urlElement.addContent(ivyPatternElement);

            chainElement.addContent(urlElement);
        }

        resolversElement.addContent(chainElement);

        rootNode.addContent(resolversElement);

        document.setRootElement(rootNode);

        String result = new XMLOutputter(Format.getPrettyFormat()).outputString(document);
        // after the xml is generated replace the password placeholder with the template placeholder (otherwise jdom
        // escapes this string)

        return result.replace("@PASS_ATTR_PLACEHOLDER@",
                filteredResourcesWebAddon.getGeneratedSettingsUserCredentialsTemplate(false));
    }

    public String getDescriptorPattern(RepoLayout layout) {
        return RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout);
    }


    public String getArtifactPattern(RepoLayout layout) {
        return RepoLayoutUtils.getArtifactLayoutAsIvyPattern(layout);
    }

    public String getFullDescriptorPattern(String servletContextUrl, String repoKey, RepoLayout layout) {
        return getFullUrl(getFullUrl(servletContextUrl, repoKey),
                RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout));
    }


    private String getFullUrl(String host, String path) {
        return new StringBuilder(host).append("/").append(path).toString();
    }

    public String getFullRepositoryUrl(String servletContextUrl, String repoKey) {
        return getFullUrl(servletContextUrl, repoKey);
    }

    public String getFullArtifactPattern(String servletContextUrl, String repoKey, RepoLayout layout) {
        return getFullUrl(getFullUrl(servletContextUrl, repoKey),
                RepoLayoutUtils.getArtifactLayoutAsIvyPattern(layout));
    }

    /**
     * @param layoutName
     * @return
     */
    private RepoLayout getLayout(String layoutName) {
        RepoLayout layout = null;
        List<RepoLayout> repoLayouts = centralConfigService.getDescriptor().getRepoLayouts();
        for (RepoLayout repoLayout : repoLayouts) {
            if (repoLayout.getName().equals(layoutName)) {
                layout = repoLayout;
                break;
            }
        }
        return layout;
    }
}
