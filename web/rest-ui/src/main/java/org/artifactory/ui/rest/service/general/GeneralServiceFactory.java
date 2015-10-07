package org.artifactory.ui.rest.service.general;

import org.artifactory.ui.rest.service.admin.configuration.general.GetUploadLogoService;
import org.artifactory.ui.rest.service.home.GetHomePageService;
import org.artifactory.ui.rest.service.setmeup.GetGradleSettingSnippetService;
import org.artifactory.ui.rest.service.setmeup.GetIvySettingSnippetService;
import org.artifactory.ui.rest.service.setmeup.GetMavenDistributionMgntService;
import org.artifactory.ui.rest.service.setmeup.GetMavenSettingSnippetService;
import org.artifactory.ui.rest.service.setmeup.GetSetMeUpService;
import org.artifactory.ui.rest.service.setmeup.GradleSettingGeneratorService;
import org.artifactory.ui.rest.service.setmeup.IvySettingGeneratorService;
import org.artifactory.ui.rest.service.setmeup.MavenSettingGeneratorService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class GeneralServiceFactory {

    // fetch tree service
    @Lookup
    public abstract GetFooterService getFooterService();

    @Lookup
    public abstract GetHomePageService getHomePage();

    @Lookup
    public abstract GetSetMeUpService getSetMeUp();

    @Lookup
    public abstract MavenSettingGeneratorService mavenSettingGenerator();

    @Lookup
    public abstract GradleSettingGeneratorService gradleSettingGenerator();

    @Lookup
    public abstract IvySettingGeneratorService ivySettingGenerator();

    @Lookup
    public abstract GetMavenSettingSnippetService getMavenSettingSnippet();

    @Lookup
    public abstract GetGradleSettingSnippetService getGradleSettingSnippet();

    @Lookup
    public abstract GetIvySettingSnippetService GetIvySettingSnippet();

    @Lookup
    public abstract GetMavenDistributionMgntService getMavenDistributionMgnt();

    @Lookup
    public abstract GetUploadLogoService getUploadLogo();


}
