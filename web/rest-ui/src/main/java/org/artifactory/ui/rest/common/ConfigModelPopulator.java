package org.artifactory.ui.rest.common;

import org.artifactory.api.license.LicenseInfo;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.bintray.BintrayUIModel;
import org.artifactory.ui.rest.model.admin.configuration.blackduck.BlackDuck;
import org.artifactory.ui.rest.model.admin.configuration.licenses.License;
import org.artifactory.ui.rest.model.admin.configuration.proxy.Proxy;
import org.artifactory.ui.rest.model.empty.EmptyModel;

import javax.annotation.Nonnull;

/**
 * @author Chen Keinan
 */
public class ConfigModelPopulator {

    /**
     * populate proxy descriptor data to Proxy model
     *
     * @param proxyDescriptor - proxy descriptor
     * @return proxy model
     */
    @Nonnull
    public static Proxy populateProxyConfiguration(@Nonnull ProxyDescriptor proxyDescriptor) {
        Proxy proxy = null;
        if (proxyDescriptor != null) {
            proxy = new Proxy(proxyDescriptor);
        }
        return proxy;
    }

    /**
     * populate licenseInfo descriptor data to licenseInfo model
     * @param licenseInfo - licenseInfo descriptor
     * @return licenseInfo model
     */
    @Nonnull
    public static License populateLicenseInfo(@Nonnull LicenseInfo licenseInfo) {
        License license = null;
        if (licenseInfo != null) {
            license = new License(licenseInfo);
        }
        return license;
    }

    /**
     * populate blackDuckSettingsDescriptor descriptor data to blackDuck model
     *
     * @param blackDuckSettingsDescriptor - blackDuck  descriptor
     * @return licenseInfo model
     */
    @Nonnull
    public static RestModel populateBlackDuckInfo(BlackDuckSettingsDescriptor blackDuckSettingsDescriptor) {
        if (blackDuckSettingsDescriptor != null) {
            return  new BlackDuck(blackDuckSettingsDescriptor);

        }
        return new EmptyModel();
    }

    /**
     * populate bintrayConfigDescriptor descriptor data to bintray  model
     *
     * @param bintrayConfigDescriptor - bintray  descriptor
     * @return licenseInfo model
     */
    @Nonnull
    public static RestModel populateBintrayInfo(BintrayConfigDescriptor bintrayConfigDescriptor, String bintrayUrl) {
        BintrayUIModel bintrayUIModel = new BintrayUIModel(bintrayConfigDescriptor);
        bintrayUIModel.setBintrayConfigUrl(bintrayUrl);
        return bintrayUIModel;
    }
}
