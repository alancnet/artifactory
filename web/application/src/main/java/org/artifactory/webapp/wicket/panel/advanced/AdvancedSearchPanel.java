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

package org.artifactory.webapp.wicket.panel.advanced;

import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.search.SearchControlsBase;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.collapsible.CollapsibleBehavior;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Eli Givoni
 */
public class AdvancedSearchPanel extends WhiteTitlePanel {

    @SpringBean
    private RepositoryService repoService;

    private CollapsibleBehavior collapsibleBehavior;
    private SearchControlsBase searchControlsBase;

    public AdvancedSearchPanel(String id, IModel<SearchControlsBase> model) {
        super(id, model);
        searchControlsBase = model.getObject();
        add(new CssClass("advanced-search-panel"));

        List<String> repoList = getOrderdRepoKeys();
        ListMultipleChoice choice = new ListMultipleChoice<>("selectedRepoForSearch", repoList);
        choice.setOutputMarkupId(true);
        add(choice);

        collapsibleBehavior = new CollapsibleBehavior().setResizeModal(true).setUseAjax(true);
        add(collapsibleBehavior);
    }

    @Override
    protected Component newToolbar(String id) {
        return new HelpBubble(id, new ResourceModel("advancedHelp"));
    }

    /**
     * Repos list should be expanded iff at least one repo is selected
     */
    public void expandCollapseReposList() {
        collapsibleBehavior.setExpanded(searchControlsBase.isSpecificRepoSearch());
    }

    private List<String> getOrderdRepoKeys() {
        List<RepoDescriptor> repoSet = Lists.newArrayList();
        List<LocalRepoDescriptor> localAndCachedRepoDescriptors = repoService.getLocalAndCachedRepoDescriptors();
        Collections.sort(localAndCachedRepoDescriptors, new LocalAndCachedDescriptorsComparator());
        repoSet.addAll(localAndCachedRepoDescriptors);
        List<String> repoKeys = Lists.newArrayList();
        for (RepoDescriptor descriptor : repoSet) {
            repoKeys.add(descriptor.getKey());
        }
        return repoKeys;
    }

    /**
     * Comparator that compares local and cached repositories according to their type (local or cached local) and then
     * internally sorting them by their key.
     */
    private static class LocalAndCachedDescriptorsComparator implements Comparator<RepoDescriptor> {
        @Override
        public int compare(RepoDescriptor o1, RepoDescriptor o2) {
            if (o1 instanceof LocalCacheRepoDescriptor && !(o2 instanceof LocalCacheRepoDescriptor)) {
                return 1;
            } else if (!(o1 instanceof LocalCacheRepoDescriptor) && (o2 instanceof LocalCacheRepoDescriptor)) {
                return -1;
            } else {
                return o1.getKey().toLowerCase().compareTo(o2.getKey().toLowerCase());
            }
        }
    }
}
