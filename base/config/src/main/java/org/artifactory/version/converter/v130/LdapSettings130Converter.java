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

package org.artifactory.version.converter.v130;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema version 1.3.1 removed the authenticationMethod and searchAuthPasswordAttributeName from the ldap settings
 * element.
 *
 * @author Yossi Shaul
 */
public class LdapSettings130Converter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(LdapSettings130Converter.class);

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();
        Element security = root.getChild("security", ns);
        if (security == null) {
            log.debug("no security settings");
            return;
        }

        Element ldap = security.getChild("ldapSettings", ns);
        if (ldap == null) {
            log.debug("no ldap settings");
            return;
        }

        ldap.removeChild("authenticationMethod", ns);
        ldap.removeChild("searchAuthPasswordAttributeName", ns);
    }
}
