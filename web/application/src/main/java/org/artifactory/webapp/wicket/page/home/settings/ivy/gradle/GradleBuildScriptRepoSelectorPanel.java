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

package org.artifactory.webapp.wicket.page.home.settings.ivy.gradle;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.page.home.settings.DefaultOptionSelector;

import java.util.List;

/**
 * Represents a settings generator "resolver"
 *
 * @author Noam Y. Tenne
 */
public class GradleBuildScriptRepoSelectorPanel<T extends RepoDescriptor> extends Panel {

    @SpringBean
    private CentralConfigService centralConfigService;

    @WicketProperty
    private RepoDescriptor repository;

    @WicketProperty
    private RepoLayout layout;

    @WicketProperty
    private boolean useMaven;

    @WicketProperty
    private boolean useIvy;

    private final String servletContextUrl;

    private DropDownChoice<T> repoDropDownChoice;
    private DropDownChoice<RepoLayout> layoutDropDownChoice;
    private List<RepoLayout> repoLayouts;
    private StyledCheckbox useMavenCheckBox;
    private StyledCheckbox useIvyCheckBox;

    public GradleBuildScriptRepoSelectorPanel(String id, List<T> repoDescriptors,
            String servletContextUrl, RepoType repoType, boolean isResolver) {
        super(id);
        this.servletContextUrl = servletContextUrl;
        repoLayouts = centralConfigService.getDescriptor().getRepoLayouts();

        addRepoFields(repoDescriptors, repoType);

        addMavenFields(isResolver);

        addIvyFields(isResolver);

        addLayoutFields(repoType);

        onLayoutChoiceChange(null, layoutDropDownChoice.getModelObject());
    }

    public String getSelectedRepositoryKey() {
        return repository.getKey();
    }

    public boolean isUseMaven() {
        return useMaven;
    }

    public boolean isUseIvy() {
        return useIvy;
    }

    public String getFullRepositoryUrl() {
        return getFullUrl(servletContextUrl, repository.getKey());
    }

    public String getArtifactPattern() {
        return RepoLayoutUtils.getArtifactLayoutAsIvyPattern(layout);
    }

    public String getFullDescriptorPattern() {
        return getFullUrl(getFullUrl(servletContextUrl, repository.getKey()),
                RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout));
    }

    public String getDescriptorPattern() {
        return RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout);
    }

    private void addRepoFields(List<T> repoDescriptors, RepoType repoType) {
        add(new Label("repositoryLabel", getRepositoryLabel()));
        repoDropDownChoice = new DropDownChoice<>("repository",
                new PropertyModel<T>(this, "repository"), repoDescriptors);

        repoDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                RepoLayout selection = getDefaultSelection(repoLayouts, new DefaultLayoutSelector(repository));
                boolean enabled = false;
                if (selection == null) {
                    selection = repoLayouts.get(0);
                    enabled = true;
                }
                layoutDropDownChoice.setEnabled(enabled);
                layoutDropDownChoice.setDefaultModelObject(selection);
                onLayoutChoiceChange(target, selection);
                target.add(layoutDropDownChoice);
            }
        });

        T defaultSelection = getDefaultSelection(repoDescriptors, repoType.getDefaultVirtualRepoKeySelector());
        if (defaultSelection == null) {
            defaultSelection = repoDescriptors.get(0);
        }
        repoDropDownChoice.setDefaultModelObject(defaultSelection);
        repoDropDownChoice.setRequired(true);
        add(repoDropDownChoice);
        add(new HelpBubble("repository.help", new ResourceModel("repository.help")));
    }

    private void addLayoutFields(RepoType repoType) {
        add(new Label("layoutLabel", getLayoutLabel()));

        layoutDropDownChoice = new DropDownChoice<>("layout",
                new PropertyModel<RepoLayout>(this, "layout"), repoLayouts,
                new ChoiceRenderer<RepoLayout>("name"));

        layoutDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onLayoutChoiceChange(target, layout);
            }
        });

        RepoDescriptor defaultRepo = repoDropDownChoice.getModelObject();
        RepoLayout defaultSelection = getDefaultSelection(repoLayouts, new DefaultLayoutSelector(defaultRepo));
        boolean enabled = false;
        if (defaultSelection == null) {
            defaultSelection = repoLayouts.get(0);
            enabled = true;
        }
        layoutDropDownChoice.setEnabled(enabled);
        layoutDropDownChoice.setDefaultModelObject(defaultSelection);
        layoutDropDownChoice.setRequired(true);
        add(layoutDropDownChoice);
        add(new HelpBubble("layout.help", new ResourceModel("layout.help")));
    }

    private void onLayoutChoiceChange(AjaxRequestTarget target, RepoLayout selectedLayout) {
        boolean selectedLayoutIsM2 = RepoLayoutUtils.isDefaultM2(selectedLayout);
        boolean selectedLayoutIsIvy = RepoLayoutUtils.isDefaultIvy(selectedLayout);
        boolean selectedLayoutIsGradle = RepoLayoutUtils.isDefaultGradle(selectedLayout);

        useMavenCheckBox.setModelObject(selectedLayoutIsM2);
        if (target != null) {
            target.add(useMavenCheckBox);
        }

        useIvyCheckBox.setModelObject(selectedLayoutIsIvy || selectedLayoutIsGradle);
        onUseIvyChoiceChange(target, useIvyCheckBox.getModelObject());
        if (target != null) {
            target.add(useIvyCheckBox);
        }
    }

    private void addMavenFields(boolean isResolver) {
        useMavenCheckBox = new StyledCheckbox("useMaven", new PropertyModel<Boolean>(this, "useMaven"));
        useMavenCheckBox.setTitle(getUseMavenLabel(isResolver));
        useMavenCheckBox.setOutputMarkupId(true);
        useMavenCheckBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onUseMavenChoiceChange(target, isUseMaven());
            }
        });
        add(useMavenCheckBox);
        add(new HelpBubble("useMaven.help", new ResourceModel("useMaven.help")));
    }

    private void addIvyFields(boolean isResolver) {
        useIvyCheckBox = new StyledCheckbox("useIvy", new PropertyModel<Boolean>(this, "useIvy"));
        useIvyCheckBox.setTitle(getUseIvyLabel(isResolver));
        useIvyCheckBox.setOutputMarkupId(true);
        useIvyCheckBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onUseIvyChoiceChange(target, isUseIvy());
            }
        });
        add(useIvyCheckBox);
        add(new HelpBubble("useIvy.help", new ResourceModel("useIvy.help")));
    }

    private void onUseMavenChoiceChange(AjaxRequestTarget target, boolean useMavenSelectionState) {
        if (!useMavenSelectionState) {
            useIvyCheckBox.setModelObject(true);
            onUseIvyChoiceChange(target, true);
            if (target != null) {
                target.add(useIvyCheckBox);
            }
        }
    }

    private void onUseIvyChoiceChange(AjaxRequestTarget target, boolean useIvySelectionState) {
        layoutDropDownChoice.setEnabled(useIvySelectionState);
        if (target != null) {
            target.add(layoutDropDownChoice);
        }
        if (!useIvySelectionState) {
            useMavenCheckBox.setModelObject(true);
            if (target != null) {
                target.add(useMavenCheckBox);
            }
        }
    }

    private String getFullUrl(String host, String path) {
        return new StringBuilder(host).append("/").append(path).toString();
    }

    private String getRepositoryLabel() {
        return "Repository Key";
    }

    private String getLayoutLabel() {
        return "Repository Layout";
    }

    private String getUseMavenLabel(boolean isResolver) {
        if (isResolver) {
            return "Use Maven";
        } else {
            // Publisher
            return "Publish Maven Descriptor";
        }
    }

    private String getUseIvyLabel(boolean isResolver) {
        if (isResolver) {
            return "Use Ivy";
        } else {
            // Publisher
            return "Publish Ivy Descriptor";
        }
    }

    private <Y> Y getDefaultSelection(List<Y> options, DefaultOptionSelector selector) {

        for (Y option : options) {
            if (selector.acceptedAsDefault(option)) {
                return option;
            }
        }

        return null;
    }

    public enum RepoType {

        PLUGINS_RESOLVER("plugins"),
        LIBS_RESOLVER("libs"),
        LIBS_PUBLISHER("libs");

        private DefaultVirtualRepoKeySelector defaultVirtualRepoKeySelector;

        private RepoType(String typeAutoSelectKeyword) {
            this.defaultVirtualRepoKeySelector = new DefaultVirtualRepoKeySelector(typeAutoSelectKeyword);
        }

        public DefaultVirtualRepoKeySelector getDefaultVirtualRepoKeySelector() {
            return defaultVirtualRepoKeySelector;
        }

        public static class DefaultVirtualRepoKeySelector<T extends RepoDescriptor> extends DefaultOptionSelector<T> {

            private final String keyword;

            private DefaultVirtualRepoKeySelector(String keyword) {
                this.keyword = keyword;
            }

            @Override
            public boolean acceptedAsDefault(T repoDescriptor) {
                return StringUtils.containsIgnoreCase(repoDescriptor.getKey(), keyword);
            }
        }
    }

    private static class DefaultLayoutSelector extends DefaultOptionSelector<RepoLayout> {

        private final RepoDescriptor repoDescriptor;

        private DefaultLayoutSelector(RepoDescriptor repoDescriptor) {
            this.repoDescriptor = repoDescriptor;
        }

        @Override
        public boolean acceptedAsDefault(RepoLayout option) {
            RepoLayout virtualRepoLayout = repoDescriptor.getRepoLayout();

            return (virtualRepoLayout != null) && virtualRepoLayout.getName().equals(option.getName());
        }
    }
}
