package org.artifactory.ui.rest.service.general;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.AddonsWebManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.general.Footer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetFooterService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;
    @Autowired
    private AuthorizationService authorizationService;
    private boolean helpLinksEnabled;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String versionInfo = getVersionInfo();
        String versionID = getVersionID(versionInfo);
        Footer footer = new Footer(getFooterLicenseInfo(), versionInfo, getCopyrights(), getCopyRightsUrl(),
                getBuildNum(), isAol(), isGlobalRepoEnabled(), versionID, isUserLogo(), getLogoUrl(), getServer(),
                getSystemMessage(), isHelpLinksEnabled());
        response.iModel(footer);
    }

    /**
     * get version id (OSS / PRO / ENT)
     *
     * @param versionInfo - edition version info
     */
    private String getVersionID(String versionInfo) {
        String versionID = "OSS";
        switch (versionInfo) {
            case "Artifactory Enterprise":
                versionID = "ENT";
                break;
            case "Artifactory Professional":
                versionID = "PRO";
                break;
            case "Artifactory AOL":
                versionID = "PRO";
                break;
            default:
                versionID = "OSS";
                break;
        }
        return versionID;
    }

    /**
     * return version info
     *
     * @return version info text
     */
    private String getVersionInfo() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        if (addonsManager instanceof OssAddonsManager){
            return "Artifactory OSS";
        }
        if (isAol()) {
            return "Artifactory AOL";
        } else if (addonsManager.isHaLicensed()) {
            return "Artifactory Enterprise";
        } else if (addonsManager.getLicenseDetails()[2].equals("Commercial")) {
            return "Artifactory Professional";
        }  else if (addonsManager.getLicenseDetails()[2].equals("Trial")) {
            return "Artifactory OSS";
        } else {
            // No license and we know that the instance is PRO instance
            return "Artifactory Professional";
        }
    }

    private boolean isAol() {
        return ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol();
    }

    private boolean isGlobalRepoEnabled() {
        return !ConstantValues.disableGlobalRepoAccess.getBoolean();
    }

    /**
     * return version info
     *
     * @return version info text
     */
    private String getBuildNum() {
        CoreAddons addon =  ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class);
        return addon.getBuildNum();
    }


    /**
     * return footer license message
     *
     * @return footer text message
     */
    private String getFooterLicenseInfo() {
        AddonsWebManager addonsManager = ContextHelper.get().beanForType(AddonsWebManager.class);
        return addonsManager.getFooterMessage(authorizationService.isAdmin());
    }

    /**
     * get copyrights data
     *
     * @return copy rights data
     */
    private String getCopyrights() {
        LocalDate localDate = LocalDate.now();
        String copyRights = "© Copyright " + localDate.getYear() + " JFrog Ltd";
        return copyRights;
    }

    /**
     * get copyrights url
     *
     * @return copyrights url
     */
    private String getCopyRightsUrl() {
        return "http://www.jfrog.com";
    }

    /**
     * check if user logo exist
     *
     * @return true if user logo exist
     */
    private boolean isUserLogo() {
        String logoDir = ContextHelper.get().getArtifactoryHome().getLogoDir().getAbsolutePath();
        File sourceFile = new File(logoDir, "logo");
        boolean fileExist = sourceFile.canRead();
        if (fileExist) {
            return true;
        }
        return false;
    }

    /**
     * return logo url link
     *
     * @return
     */
    private String getLogoUrl() {
        return centralConfigService.getDescriptor().getLogo();
    }

    /**
     * return logo url link
     *
     * @return
     */
    private String getServer() {
        return centralConfigService.getDescriptor().getServerName();
    }

    /**
     *  System message descriptor
     */
    private SystemMessageDescriptor getSystemMessage() {
        return Optional.ofNullable(centralConfigService.getDescriptor().getSystemMessageConfig())
                .orElse(new SystemMessageDescriptor());
    }

    public boolean isHelpLinksEnabled() {
        return centralConfigService.getDescriptor().isHelpLinksEnabled();
    }
}
