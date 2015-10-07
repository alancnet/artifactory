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
 * The Ivy dependency declaration generator
 *
 * @author Noam Y. Tenne
 */
public class IvyDependencyDeclarationProvider implements DependencyDeclarationProvider {

    @Override
    public Syntax getSyntaxType() {
        return Syntax.xml;
    }

    @Override
    public String getDependencyDeclaration(ModuleInfo moduleInfo) {
        String module = moduleInfo.getModule();

        StringBuilder sb = new StringBuilder("<dependency org=\"").append(moduleInfo.getOrganization()).append("\" ")
                .append("name=\"").append(module).append("\" ").append("rev=\"").append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("\"");

        String classifier = moduleInfo.getClassifier();
        String type = moduleInfo.getType();

        boolean validClassifier = StringUtils.isNotBlank(classifier);
        boolean validType = StringUtils.isNotBlank(type);

        if (validClassifier || !"jar".equals(type)) {
            sb.append(">\n")
                    .append("    <artifact name=\"").append(module).append("\"");

            if (validType && (validClassifier || !"jar".equals(type))) {
                sb.append(" type=\"").append(type).append("\"");
            }

            if (validClassifier) {
                sb.append(" m:classifier=\"").append(classifier).append("\"");
            }

            sb.append(" ext=\"").append(moduleInfo.getExt()).append("\"/>\n")
                    .append("</dependency>");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }
}
