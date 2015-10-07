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

package org.artifactory.version.converter.v132;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Convert:
 * <pre>
 *     &lt;ldapSettings&gt;
 *         &lt;ldapUrl&gt;ldap://mydomain:389/dc=jfrog,dc=org&lt;/ldapUrl&gt;
 *         &lt;authenticationPatterns&gt;
 *             &lt;authenticationPattern&gt;
 *                 &lt;userDnPattern&gt;uid={0}, ou=People&lt;/userDnPattern&gt;
 *             &lt;/authenticationPattern&gt;
 *         &lt;/authenticationPatterns&gt;
 *         &lt;managerDn&gt;ZZZ&lt;/managerDn&gt;
 *         &lt;managerPassword&gt;YYY&lt;/managerPassword&gt;
 *      &lt;/ldapSettings&gt;
 * </pre>
 * To:
 * <pre>
 *     &lt;ldapSettings&gt;
 *         &lt;ldapSetting&gt;
 *             &lt;key&gt;ldap1&lt;/key&gt;
 *             &lt;enabled&gt;true&lt;/enabled&gt;
 *             &lt;ldapUrl&gt;ldap://mydomain:389/dc=jfrog,dc=org&lt;/ldapUrl&gt;
 *             &lt;userDnPattern&gt;uid={0}, ou=People&lt;/userDnPattern&gt;
 *             &lt;search&gt;
 *                 &lt;searchFilter&gt;uid={0}&lt;/searchFilter&gt;
 *                 &lt;managerDn&gt;ZZZ&lt;/managerDn&gt;
 *                 &lt;managerPassword&gt;YYY&lt;/managerPassword&gt;
 *             &lt;/search&gt;
 *         &lt;/ldapSetting&gt;
 *     &lt;/ldapSettings&gt;
 * </pre>
 * <p/>
 * Version 1.3.3 added support for multiple ldap settings, each might contains userDn and one search pattern. If
 * multiple authenticationPattern exist, we convert each one to ldapSetting.
 *
 * @author Yossi Shaul
 */
public class LdapListConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LdapListConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Element security = root.getChild("security", ns);
        if (security == null) {
            log.debug("No security settings defned");
            return;
        }

        Element oldLdapSettings = security.getChild("ldapSettings", ns);
        if (oldLdapSettings == null) {
            log.debug("No ldap settings configured");
            return;
        }

        int location = security.indexOf(oldLdapSettings);
        security.removeContent(oldLdapSettings);

        Element ldapSettings = new Element("ldapSettings", ns);
        security.addContent(location, ldapSettings);

        String ldapUrl = oldLdapSettings.getChildText("ldapUrl", ns);

        // manager dn and password only relevant for search based authentications
        String managerDn = oldLdapSettings.getChildText("managerDn", ns);
        String managerPassword = oldLdapSettings.getChildText("managerPassword", ns);

        // convert authentication patterns
        Element authPatternsElement = oldLdapSettings.getChild("authenticationPatterns", ns);
        List authPatterns = authPatternsElement.getChildren("authenticationPattern", ns);
        log.debug("Found {} patterns to convert" + authPatterns.size());

        // create new ldap setting for each authentication pattern
        int ldapKeyIndex = 1;
        for (Object pattern : authPatterns) {
            Element authPattern = (Element) pattern;

            Element ldapSetting = new Element("ldapSetting", ns);
            ldapSettings.addContent(ldapSetting);

            // add the key
            ldapSetting.addContent(createTextElement("key", ns, "ldap" + ldapKeyIndex++));

            // set enabled true
            ldapSetting.addContent(createTextElement("enabled", ns, "true"));

            // add the ldap url
            ldapSetting.addContent(createTextElement("ldapUrl", ns, ldapUrl));

            // add user dn if not empty
            String userDn = authPattern.getChildText("userDnPattern", ns);
            if (userDn != null) {
                ldapSetting.addContent(createTextElement("userDnPattern", ns, userDn));
            }

            // create and add search element if search filter exists
            String searchFilter = authPattern.getChildText("searchFilter", ns);
            if (searchFilter != null) {
                Element search = new Element("search", ns);
                ldapSetting.addContent(search);

                search.addContent(createTextElement("searchFilter", ns, searchFilter));

                String searchBase = authPattern.getChildText("searchBase", ns);
                if (searchBase != null) {
                    search.addContent(createTextElement("searchBase", ns, searchBase));
                }

                String searchSubTree = authPattern.getChildText("searchSubTree", ns);
                if (searchSubTree != null) {
                    search.addContent(createTextElement("searchSubTree", ns, searchSubTree));
                }

                if (managerDn != null) {
                    search.addContent(createTextElement("managerDn", ns, managerDn));
                }

                if (managerPassword != null) {
                    search.addContent(createTextElement("managerPassword", ns, managerPassword));
                }
            }
        }
    }

    private Element createTextElement(String name, Namespace ns, String value) {
        Element userDnElement = new Element(name, ns);
        userDnElement.setText(value);
        return userDnElement;
    }
}
