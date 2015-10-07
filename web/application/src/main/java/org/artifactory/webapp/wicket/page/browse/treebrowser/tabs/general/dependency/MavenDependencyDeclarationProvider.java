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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.dependency;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;

/**
 * The Maven dependency declaration generator
 *
 * @author Noam Y. Tenne
 */
public class MavenDependencyDeclarationProvider implements DependencyDeclarationProvider {

    @Override
    public Syntax getSyntaxType() {
        return Syntax.xml;
    }

    @Override
    public String getDependencyDeclaration(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<dependency>\n");
        sb.append("    <groupId>").append(moduleInfo.getOrganization()).append("</groupId>\n");
        sb.append("    <artifactId>").append(moduleInfo.getModule()).append("</artifactId>\n");
        sb.append("    <version>").append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("</version>\n");

        String classifier = moduleInfo.getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            sb.append("    <classifier>").append(classifier).append("</classifier>\n");
        }

        String ext = moduleInfo.getExt();
        if (StringUtils.isNotBlank(ext) && !"jar".equalsIgnoreCase(ext)) {
            sb.append("    <type>").append(moduleInfo.getExt()).append("</type>\n");
        }

        return sb.append("</dependency>").toString();
    }
}
