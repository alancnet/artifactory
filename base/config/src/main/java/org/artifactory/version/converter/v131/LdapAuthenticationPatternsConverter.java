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

package org.artifactory.version.converter.v131;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * Converts:
 *   &lt;ldapSettings&gt;
 *       ...
 *       &lt;userDnPattern&gt;...&lt;/userDnPattern&gt;
 *   &lt;/ldapSettings&gt;
 * into:
 *  &lt;authenticationPatterns&gt;&lt;authenticationPattern&gt;
 *      &lt;userDnPattern&gt;...&lt;/userDnPattern&gt;
 *  &lt;/authenticationPattern&gt;&lt;/authenticationPatterns&gt;
 * </pre>
 * <p/>
 * Was valid until version 1.3.1 of the schema.
 *
 * @author Yossi Shaul
 */
public class LdapAuthenticationPatternsConverter implements XmlConverter {
    private static final Logger log =
            LoggerFactory.getLogger(LdapAuthenticationPatternsConverter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        if (security != null) {
            Element ldapSettings = security.getChild("ldapSettings", ns);
            if (ldapSettings != null) {
                Element userDn = ldapSettings.getChild("userDnPattern", ns);
                if (userDn != null) {
                    log.debug("Moving userDnPattern under authenticationPatterns");
                    int location = ldapSettings.indexOf(userDn);
                    ldapSettings.removeContent(userDn);
                    Element authPatterns = new Element("authenticationPatterns", ns);
                    Element authPattern = new Element("authenticationPattern", ns);
                    authPattern.addContent(userDn);
                    authPatterns.addContent(authPattern);
                    ldapSettings.addContent(location, authPatterns);
                }
            } else {
                log.debug("No ldap settings found");
            }
        }
    }
}
