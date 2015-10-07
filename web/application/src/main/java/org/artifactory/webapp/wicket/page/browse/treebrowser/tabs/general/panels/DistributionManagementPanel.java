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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.actionable.RepoAwareActionableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Displays distribution management section for users to add the the pom in order to deploy to a selected local/cached
 * repository.
 *
 * @author Yossi Shaul
 */
public class DistributionManagementPanel extends FieldSetPanel {
    private static final Logger log = LoggerFactory.getLogger(DistributionManagementPanel.class);

    private String firstIndention = "";
    private String secondIndention = "    ";
    private boolean isCache = false;

    public DistributionManagementPanel(String id, RepoAwareActionableItem repoItem) {
        super(id);
        addDistributionManagement(repoItem);
    }

    @Override
    public String getTitle() {
        if (isCache) {
            return "Repository Reference";
        }
        return "Distribution Management";
    }

    private void addDistributionManagement(RepoAwareActionableItem repoItem) {
        final StringBuilder sb = new StringBuilder();
        sb.delete(0, sb.length());
        LocalRepoDescriptor repo = repoItem.getRepo();
        CentralConfigService cc = ContextHelper.get().getCentralConfig();
        String id = cc.getServerName();
        String repoUrl = buildRepoUrl(repo);
        isCache = repo.isCache();
        setIndentions(isCache);
        if (!isCache) {
            sb.append("<distributionManagement>\n");
        }
        if (repo.isHandleReleases()) {
            sb.append(firstIndention);
            sb.append("<repository>\n");
            sb.append(secondIndention);
            sb.append("<id>");
            sb.append(id);
            sb.append("</id>\n");
            sb.append(secondIndention);
            sb.append("<name>");
            sb.append(id);
            sb.append("-releases</name>\n");
            sb.append(secondIndention);
            sb.append("<url>");
            sb.append(repoUrl);
            sb.append("</url>\n");
            sb.append(firstIndention);
            sb.append("</repository>\n");
        }

        if (repo.isHandleSnapshots()) {
            sb.append(firstIndention);
            sb.append("<snapshotRepository>\n");
            sb.append(secondIndention);
            sb.append("<id>");
            sb.append(id);
            sb.append("</id>\n");
            sb.append(secondIndention);
            sb.append("<name>");
            sb.append(id);
            sb.append("-snapshots</name>\n");
            sb.append(secondIndention);
            sb.append("<url>");
            sb.append(repoUrl);
            sb.append("</url>\n");
            sb.append(firstIndention);
            sb.append("</snapshotRepository>\n");
        }
        if (!isCache) {
            sb.append("</distributionManagement>");
        }
        add(WicketUtils.getSyntaxHighlighter("distributionManagementContainer", sb.toString(), Syntax.xml));

        log.debug("Pom definition: {}", sb);
    }

    private String buildRepoUrl(LocalRepoDescriptor repo) {
        HttpServletRequest request = WicketUtils.getHttpServletRequest();
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        if (!servletContextUrl.endsWith("/")) {
            servletContextUrl += "/";
        }
        StringBuilder sb = new StringBuilder();
        if (repo instanceof LocalCacheRepoDescriptor) {
            RemoteRepoDescriptor remoteRepoDescriptor = ((LocalCacheRepoDescriptor) repo).getRemoteRepo();
            if (remoteRepoDescriptor != null) {
                sb.append(servletContextUrl).append(remoteRepoDescriptor.getKey());
            } else {
                String fixedKey = StringUtils.remove(repo.getKey(), "-cache");
                sb.append(servletContextUrl).append(fixedKey);
            }
        } else {
            sb.append(servletContextUrl).append(repo.getKey());
        }
        return sb.toString();
    }

    private void setIndentions(boolean isCache) {
        if (!isCache) {
            firstIndention += "    ";
            secondIndention += "    ";
        }
    }
}