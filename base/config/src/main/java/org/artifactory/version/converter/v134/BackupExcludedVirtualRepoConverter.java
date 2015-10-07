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

package org.artifactory.version.converter.v134;

import com.google.common.collect.Sets;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Checks and removes in reference of a virtual repository from the backup's excluded repository list
 *
 * @author Noam Y. Tenne
 */
public class BackupExcludedVirtualRepoConverter implements XmlConverter {

    @Override
    public void convert(Document doc) {
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        Set<Object> virtualRepoKeys = Sets.newHashSet();

        Element virtualRepositories = root.getChild("virtualRepositories", ns);
        if (virtualRepositories != null) {
            List repos = virtualRepositories.getChildren("virtualRepository", ns);
            for (Object repo : repos) {
                Element repoElem = (Element) repo;
                Element key = repoElem.getChild("key", ns);
                virtualRepoKeys.add(key.getText());
            }
        }

        Element backups = root.getChild("backups", ns);
        if (backups != null) {
            List backupsList = backups.getChildren("backup", ns);
            for (Object backup : backupsList) {
                Element backupElement = (Element) backup;
                Element excludedRepositories = backupElement.getChild("excludedRepositories", ns);
                if (excludedRepositories != null) {
                    List excludedRepoList = excludedRepositories.getChildren("repositoryRef", ns);
                    Iterator excludedRepoIterator = excludedRepoList.iterator();
                    while (excludedRepoIterator.hasNext()) {
                        Element excludedRepo = (Element) excludedRepoIterator.next();
                        if (virtualRepoKeys.contains(excludedRepo.getText())) {
                            excludedRepoIterator.remove();
                        }
                    }
                }
            }
        }
    }
}
