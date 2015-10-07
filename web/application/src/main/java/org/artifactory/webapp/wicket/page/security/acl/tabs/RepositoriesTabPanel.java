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

package org.artifactory.webapp.wicket.page.security.acl.tabs;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.dnd.select.DragDropSelection;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.webapp.wicket.components.SortedRepoDragDropSelection;
import org.artifactory.webapp.wicket.page.security.acl.CommonPathPattern;
import org.artifactory.webapp.wicket.page.security.acl.PermissionTargetCreateUpdatePanel;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class RepositoriesTabPanel extends BasePermissionTabPanel {
    private DragDropSelection<RealRepoDescriptor> repoKeysSelection;

    private PermissionTargetCreateUpdatePanel parent;

    public RepositoriesTabPanel(String id, PermissionTargetCreateUpdatePanel parent) {
        super(id);
        add(new MyResourcePackage());

        this.parent = parent;
        setOutputMarkupId(true);

        StringResourceModel helpMessage = new StringResourceModel("help.patterns", this, null);
        List<CommonPathPattern> includesExcludesSuggestions = Arrays.asList(CommonPathPattern.values());

        addIncludesPatternFields(helpMessage, includesExcludesSuggestions);
        addExcludesPatternFields(helpMessage, includesExcludesSuggestions);

        addRepoKeysCheckboxes();
        addRepositoriesSelections();
    }

    /**
     * @return True if the current user is a system admin (not just the current permission target admin). Non system
     *         admins can only change the receipients table.
     */
    private boolean isSystemAdmin() {
        return parent.isSystemAdmin();
    }


    private void addRepositoriesSelections() {
        List<RealRepoDescriptor> repos = new ArrayList<>();
        repos.addAll(parent.getLocalRepositoryDescriptors());
        repos.addAll(parent.getRemoteRepositoryDescriptors());
        PermissionTargetCreateUpdatePanel.RepoKeysData repoKeysData = parent.getRepoKeysData();
        repoKeysSelection = new RepoDragDropSelection("repoKeys", repoKeysData, repos);

        repoKeysSelection.setOutputMarkupId(true);
        repoKeysSelection.setEnabled(isSystemAdmin());

        add(repoKeysSelection);
    }

    private void addRepoKeysCheckboxes() {
        final StyledCheckbox anyLocalRepositoryCheckbox = new StyledCheckbox("anyLocalRepository",
                new PropertyModel<Boolean>(parent.getRepoKeysData(), "anyLocalRepository"));
        anyLocalRepositoryCheckbox.setEnabled(isSystemAdmin());
        add(anyLocalRepositoryCheckbox);

        anyLocalRepositoryCheckbox.add(new AjaxFormSubmitBehavior("onclick") {
            @Override
            protected void onError(AjaxRequestTarget target) {
                AjaxUtils.refreshFeedback(target);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                PermissionTargetCreateUpdatePanel.RepoKeysData repoKeysData = parent.getRepoKeysData();
                if (repoKeysData.isAnyLocalRepository()) {
                    repoKeysData.addRepoDescriptors(parent.getLocalRepositoryDescriptors());
                } else {
                    repoKeysData.removeRepoDescriptors(parent.getLocalRepositoryDescriptors());
                }
                target.add(RepositoriesTabPanel.this.get("repoKeys"));
                target.appendJavaScript("PermissionTabPanel.resize();");
                AjaxUtils.refreshFeedback(target);
            }
        });

        final StyledCheckbox anyRemoteRepositoryCheckbox = new StyledCheckbox("anyRemoteRepository",
                new PropertyModel<Boolean>(parent.getRepoKeysData(), "anyRemoteRepository"));
        anyRemoteRepositoryCheckbox.setEnabled(isSystemAdmin());
        add(anyRemoteRepositoryCheckbox);

        anyRemoteRepositoryCheckbox.add(new AjaxFormSubmitBehavior("onclick") {
            @Override
            protected void onError(AjaxRequestTarget target) {
                AjaxUtils.refreshFeedback(target);
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                PermissionTargetCreateUpdatePanel.RepoKeysData repoKeysData = parent.getRepoKeysData();
                if (repoKeysData.isAnyRemoteRepository()) {
                    repoKeysData.addRepoDescriptors(parent.getRemoteRepositoryDescriptors());
                } else {
                    repoKeysData.removeRepoDescriptors(parent.getRemoteRepositoryDescriptors());
                }
                target.add(RepositoriesTabPanel.this.get("repoKeys"));
                target.appendJavaScript("PermissionTabPanel.resize();");
                AjaxUtils.refreshFeedback(target);
            }
        });
    }

    private void addIncludesPatternFields(StringResourceModel helpMessage,
            final List<CommonPathPattern> includesExcludesSuggestions) {
        TextArea includesTa = new TextArea("includesPattern");
        includesTa.setEnabled(isSystemAdmin());
        includesTa.setOutputMarkupId(true);
        add(includesTa);

        add(new HelpBubble("includesHelp", helpMessage));

        Model<CommonPathPattern> include = new Model<>();
        DropDownChoice<CommonPathPattern> includesSuggest = new DropDownChoice<>(
                "includesSuggest", include, includesExcludesSuggestions);
        if (!includesExcludesSuggestions.isEmpty()) {
            includesSuggest.setDefaultModelObject(includesExcludesSuggestions.get(0));
        }
        includesSuggest.add(new UpdatePatternsBehavior(include, includesTa));
        if (parent.isCreate()) {
            includesSuggest.setDefaultModelObject(CommonPathPattern.ANY);
        }
        includesSuggest.setEnabled(isSystemAdmin());
        add(includesSuggest);
    }

    private void addExcludesPatternFields(StringResourceModel helpMessage,
            final List<CommonPathPattern> includesExcludesSuggestions) {
        TextArea excludesTa = new TextArea("excludesPattern");
        excludesTa.setEnabled(isSystemAdmin());
        excludesTa.setOutputMarkupId(true);
        add(excludesTa);

        add(new HelpBubble("excludesHelp", helpMessage));

        //Excludes suggestions
        Model<CommonPathPattern> exclude = new Model<>();
        DropDownChoice<CommonPathPattern> excludesSuggest = new DropDownChoice<>(
                "excludesSuggest", exclude, includesExcludesSuggestions);
        if (!includesExcludesSuggestions.isEmpty()) {
            excludesSuggest.setDefaultModelObject(includesExcludesSuggestions.get(0));
        }
        excludesSuggest.add(new UpdatePatternsBehavior(exclude, excludesTa));
        excludesSuggest.setEnabled(isSystemAdmin());
        add(excludesSuggest);
    }

    private static class UpdatePatternsBehavior extends AjaxFormComponentUpdatingBehavior {
        private final Model<CommonPathPattern> comboBoxModel;
        private final Component textArea;

        private UpdatePatternsBehavior(Model<CommonPathPattern> comboBoxModel, TextArea textArea) {
            super("onChange");
            this.comboBoxModel = comboBoxModel;
            this.textArea = textArea;
        }

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            CommonPathPattern commonPathPattern = comboBoxModel.getObject();
            String pattern = commonPathPattern.getPattern();
            String existingPattern = textArea.getDefaultModelObjectAsString();
            if (CommonPathPattern.NONE.equals(commonPathPattern) ||
                    !StringUtils.hasText(existingPattern)) {
                textArea.setDefaultModelObject(pattern);
            } else {
                textArea.setDefaultModelObject(existingPattern + ", " + pattern);
            }
            target.add(textArea);
        }
    }

    private class RepoDragDropSelection extends SortedRepoDragDropSelection<RealRepoDescriptor> {
        private RepoDragDropSelection(String id, PermissionTargetCreateUpdatePanel.RepoKeysData repoKeysData,
                List<RealRepoDescriptor> repos) {
            super(id, new PropertyModel<RealRepoDescriptor>(repoKeysData, "repoDescriptors"), repos);
        }

        @Override
        protected String getAcceptedTargetTypes() {
            return getMarkupId() + "," + getMarkupId() + "-targetOnly";
        }

        @Override
        protected String getDndValue(ListItem item) {
            PermissionTargetCreateUpdatePanel.RepoKeysData repoKeysData = parent.getRepoKeysData();
            boolean anyLocal = repoKeysData.isAnyLocalRepository();
            boolean anyRemote = repoKeysData.isAnyRemoteRepository();

            RealRepoDescriptor repo = (RealRepoDescriptor) item.getDefaultModelObject();
            boolean local = anyLocal && repo.isLocal();
            boolean remote = anyRemote && !repo.isLocal();
            if (local || remote) {
                item.add(new CssClass("disabled"));
                return getMarkupId() + "-targetOnly";
            }
            return getMarkupId();
        }
    }

    private class MyResourcePackage extends ResourcePackage {
        private MyResourcePackage() {
            super(RepositoriesTabPanel.class);
            addJavaScriptTemplate();
        }

        public String getRepoListId() {
            return repoKeysSelection.getMarkupId();
        }
    }
}
