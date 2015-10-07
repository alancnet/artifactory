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

package org.artifactory.version.converter.v144;

import org.artifactory.descriptor.security.ldap.group.LdapGroupSetting;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.List;

/**
 * Convert the {@link LdapGroupSetting} descriptor with the appropriate
 * LDAP setting to use. <p/>
 * The converter tries to find an enabled {@link org.artifactory.descriptor.security.ldap.LdapSetting}, if such a
 * setting does not exist, the converter will use the first found {@link org.artifactory.descriptor.security.ldap.LdapSetting#key}
 *
 * @author Tomer Cohen
 */
public class MultiLdapXmlConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element securityElement = rootElement.getChild("security", namespace);
        if (securityElement != null) {
            Element ldapSettings = securityElement.getChild("ldapSettings", namespace);
            if (ldapSettings != null) {
                String firstLdapKey = null;
                String ldapKeyToUse = null;
                List ldapSettingList = ldapSettings.getChildren("ldapSetting", namespace);
                if (ldapSettingList != null && !ldapSettingList.isEmpty()) {
                    for (Object ldapSettingObject : ldapSettingList) {
                        Element ldapSetting = (Element) ldapSettingObject;
                        Element key = ldapSetting.getChild("key", namespace);
                        if (firstLdapKey == null) {
                            firstLdapKey = key.getValue();
                        }
                        Element enabledElement = ldapSetting.getChild("enabled", namespace);
                        if (Boolean.parseBoolean(enabledElement.getValue())) {
                            ldapKeyToUse = ldapSetting.getChild("key", namespace).getValue();
                        }
                    }
                }
                if (ldapKeyToUse == null && firstLdapKey != null) {
                    ldapKeyToUse = firstLdapKey;
                }
                if (ldapKeyToUse != null) {
                    Element ldapGroupSettings = securityElement.getChild("ldapGroupSettings", namespace);
                    if (ldapGroupSettings != null) {
                        List ldapGroupList = ldapGroupSettings.getChildren("ldapGroupSetting", namespace);
                        if (ldapGroupList != null && !ldapGroupList.isEmpty()) {
                            for (Object ldapGroupSettingObject : ldapGroupList) {
                                Element ldapGroupSetting = (Element) ldapGroupSettingObject;
                                Element enabledLdapElement = new Element("enabledLdap", namespace);
                                enabledLdapElement.setText(ldapKeyToUse);
                                Element enabledContent = ldapGroupSetting.getChild("enabled", namespace);
                                int index = ldapGroupSetting.indexOf(enabledContent);
                                ldapGroupSetting.addContent(index, enabledLdapElement);
                                ldapGroupSetting.removeContent(enabledContent);
                            }
                        }
                    }
                }
            }
        }
    }
}
