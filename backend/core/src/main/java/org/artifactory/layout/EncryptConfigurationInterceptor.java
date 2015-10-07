/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.layout;

import org.apache.commons.lang.StringUtils;
import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.external.BlackDuckSettingsDescriptor;
import org.artifactory.descriptor.external.ExternalProvidersDescriptor;
import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.debian.DebianSettings;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.descriptor.security.ldap.SearchPattern;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.security.crypto.CryptoHelper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Fred Simon
 */
@Component
public class EncryptConfigurationInterceptor implements ConfigurationChangesInterceptor {

    public static void decrypt(MutableCentralConfigDescriptor descriptor) {
        encryptOrDecrypt(descriptor, false);
    }

    private static void encryptOrDecrypt(MutableCentralConfigDescriptor descriptor, boolean encrypt) {
        for (RemoteRepoDescriptor remoteRepoDescriptor : descriptor.getRemoteRepositoriesMap().values()) {
            if (remoteRepoDescriptor instanceof HttpRepoDescriptor) {
                HttpRepoDescriptor httpRepoDescriptor = (HttpRepoDescriptor) remoteRepoDescriptor;
                String newPassword = getNewPassword(encrypt, httpRepoDescriptor.getPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    httpRepoDescriptor.setPassword(newPassword);
                }
            }
        }
        for (LocalReplicationDescriptor localReplicationDescriptor : descriptor.getLocalReplications()) {
            String newPassword = getNewPassword(encrypt, localReplicationDescriptor.getPassword());
            if (StringUtils.isNotBlank(newPassword)) {
                localReplicationDescriptor.setPassword(newPassword);
            }
        }
        SecurityDescriptor security = descriptor.getSecurity();
        for (LdapSetting ldapSetting : security.getLdapSettings()) {
            SearchPattern search = ldapSetting.getSearch();
            if (search != null) {
                String newPassword = getNewPassword(encrypt, search.getManagerPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    search.setManagerPassword(newPassword);
                }
            }
        }
        DebianSettings debianSettings = security.getDebianSettings();
        if (debianSettings != null) {
            String newPassword = getNewPassword(encrypt, debianSettings.getPassphrase());
            if (StringUtils.isNotBlank(newPassword)) {
                debianSettings.setPassphrase(newPassword);
            }
        }
        CrowdSettings crowdSettings = security.getCrowdSettings();
        if (crowdSettings != null) {
            String newPassword = getNewPassword(encrypt, crowdSettings.getPassword());
            if (StringUtils.isNotBlank(newPassword)) {
                crowdSettings.setPassword(newPassword);
            }
        }
        List<ProxyDescriptor> proxies = descriptor.getProxies();
        if (proxies != null) {
            for (ProxyDescriptor proxy : proxies) {
                String newPassword = getNewPassword(encrypt, proxy.getPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    proxy.setPassword(newPassword);
                }
            }
        }
        MailServerDescriptor mailServer = descriptor.getMailServer();
        if (mailServer != null) {
            String newPassword = getNewPassword(encrypt, mailServer.getPassword());
            if (StringUtils.isNotBlank(newPassword)) {
                mailServer.setPassword(newPassword);
            }
        }
        ExternalProvidersDescriptor externalProviders = descriptor.getExternalProvidersDescriptor();
        if (externalProviders != null) {
            BlackDuckSettingsDescriptor bdSettings = externalProviders.getBlackDuckSettingsDescriptor();
            if (bdSettings != null) {
                String newPassword = getNewPassword(encrypt, bdSettings.getPassword());
                if (StringUtils.isNotBlank(newPassword)) {
                    bdSettings.setPassword(newPassword);
                }
            }
        }
        BintrayConfigDescriptor bintraySettings = descriptor.getBintrayConfig();
        if (bintraySettings != null) {
            String newApiKey = getNewPassword(encrypt, bintraySettings.getApiKey());
            if (StringUtils.isNotBlank(newApiKey)) {
                bintraySettings.setApiKey(newApiKey);
            }
        }
    }

    private static String getNewPassword(boolean encrypt, String password) {
        if (StringUtils.isNotBlank(password)) {
            if (encrypt) {
                return CryptoHelper.encryptIfNeeded(password);
            } else {
                return CryptoHelper.decryptIfNeeded(password);
            }
        }
        return null;
    }

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        if (newDescriptor instanceof MutableCentralConfigDescriptor && CryptoHelper.hasMasterKey()) {
            // Find all sensitive data and encrypt them
            encrypt((MutableCentralConfigDescriptor) newDescriptor);
        }
    }

    private void encrypt(MutableCentralConfigDescriptor descriptor) {
        encryptOrDecrypt(descriptor, true);
    }

}
