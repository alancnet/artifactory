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

package org.artifactory.webapp.wicket.page.home.settings.ivy.base;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
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
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.page.home.settings.DefaultOptionSelector;

import java.util.List;

/**
 * Represents a settings generator "resolver"
 *
 * @author Noam Y. Tenne
 */
public class IvySettingsRepoSelectorPanel<T extends RepoDescriptor> extends Panel {

    @SpringBean
    private CentralConfigService centralConfigService;

    @WicketProperty
    private VirtualRepoDescriptor repository;

    @WicketProperty
    private RepoLayout layout;

    @WicketProperty
    private String resolverName;

    @WicketProperty
    private boolean useIbiblioResolver;

    @WicketProperty
    private boolean m2Compatible;

    private final String servletContextUrl;

    private DropDownChoice<T> repoDropDownChoice;
    private DropDownChoice<RepoLayout> layoutDropDownChoice;
    private List<RepoLayout> repoLayouts;
    private StyledCheckbox useIbiblioResolverCheckBox;
    private StyledCheckbox m2CompatibleCheckBox;

    public IvySettingsRepoSelectorPanel(String id, List<T> virtualRepoDescriptors,
            String servletContextUrl, RepoType repoType) {
        super(id);
        this.servletContextUrl = servletContextUrl;
        repoLayouts = centralConfigService.getDescriptor().getRepoLayouts();

        addRepoFields(virtualRepoDescriptors, repoType);

        addLayoutFields(repoType);

        addResolverFields(repoType);

        addIbiblioFields();

        addM2CompatibleFields();

        onLayoutChoiceChange(null, layoutDropDownChoice.getModelObject());
    }

    public String getSelectedRepositoryKey() {
        return repository.getKey();
    }

    public RepoLayout getLayout() {
        return layout;
    }

    public String getResolverName() {
        return resolverName;
    }

    public boolean useIbiblioResolver() {
        return useIbiblioResolver;
    }

    public boolean isM2Compatible() {
        return m2Compatible;
    }

    public String getFullRepositoryUrl() {
        return getFullUrl(servletContextUrl, repository.getKey());
    }

    public String getFullArtifactPattern() {
        return getFullUrl(getFullUrl(servletContextUrl, repository.getKey()),
                RepoLayoutUtils.getArtifactLayoutAsIvyPattern(layout));
    }

    public String getFullDescriptorPattern() {
        return getFullUrl(getFullUrl(servletContextUrl, repository.getKey()),
                RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(layout));
    }

    private void addRepoFields(List<T> virtualRepoDescriptors, RepoType repoType) {
        add(new Label("repositoryLabel", getRepositoryLabel(repoType)));
        repoDropDownChoice = new DropDownChoice<>("repository",
                new PropertyModel<T>(this, "repository"), virtualRepoDescriptors);

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

        T defaultSelection = getDefaultSelection(virtualRepoDescriptors, repoType.getDefaultVirtualRepoKeySelector());
        if (defaultSelection == null) {
            defaultSelection = virtualRepoDescriptors.get(0);
        }
        repoDropDownChoice.setDefaultModelObject(defaultSelection);
        repoDropDownChoice.setRequired(true);
        add(repoDropDownChoice);
        add(new HelpBubble("repository.help", new ResourceModel("repository.help")));
    }

    private void addLayoutFields(RepoType repoType) {
        add(new Label("layoutLabel", getLayoutLabel(repoType)));

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
        boolean ibiblioChecked = useIbiblioResolver();

        Boolean newValue = null;

        if (ibiblioChecked && !selectedLayoutIsM2) {
            newValue = Boolean.FALSE;
        } else if (!ibiblioChecked && selectedLayoutIsM2) {
            newValue = Boolean.TRUE;
        }

        if (newValue != null) {
            useIbiblioResolverCheckBox.setModelObject(newValue);
        }
        onIbiblioChoiceChange(target, useIbiblioResolverCheckBox.getModelObject());
        if (target != null) {
            target.add(useIbiblioResolverCheckBox);
        }
    }

    private void addResolverFields(RepoType repoType) {
        add(new Label("resolverNameLabel", getResolverLabel(repoType)));
        TextField<String> resolverTextField =
                new TextField<>("resolverName", new PropertyModel<String>(this, "resolverName"));
        resolverTextField.setOutputMarkupId(true);
        add(resolverTextField);
        add(new HelpBubble("resolverName.help", new ResourceModel("resolverName.help")));
    }

    private void addIbiblioFields() {
        useIbiblioResolverCheckBox = new StyledCheckbox("useIbiblioResolver",
                new PropertyModel<Boolean>(this, "useIbiblioResolver"));
        useIbiblioResolverCheckBox.setOutputMarkupId(true);
        useIbiblioResolverCheckBox.add(new AjaxFormComponentUpdatingBehavior("onclick") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onIbiblioChoiceChange(target, useIbiblioResolver());
            }
        });
        add(useIbiblioResolverCheckBox);
        add(new HelpBubble("useIbiblioResolver.help", new ResourceModel("useIbiblioResolver.help")));
    }

    private void onIbiblioChoiceChange(AjaxRequestTarget target, boolean ibiblioSelectionState) {
        boolean m2Selected = isM2Compatible();

        boolean m2enabled = true;
        Boolean newValue = null;

        if (ibiblioSelectionState) {
            m2enabled = false;
            if (!m2Selected) {
                newValue = Boolean.TRUE;
            }
        } else if (!ibiblioSelectionState) {
            if (m2Selected) {
                newValue = RepoLayoutUtils.layoutContainsOrgPathToken(layout);
            }
        }

        if (newValue != null) {
            m2CompatibleCheckBox.setModelObject(newValue);
        }
        m2CompatibleCheckBox.setEnabled(m2enabled);
        if (target != null) {
            target.add(m2CompatibleCheckBox);
        }
    }

    private void addM2CompatibleFields() {
        m2CompatibleCheckBox = new StyledCheckbox("m2Compatible", new PropertyModel<Boolean>(this, "m2Compatible"));
        m2CompatibleCheckBox.setOutputMarkupId(true);
        add(m2CompatibleCheckBox);
        add(new HelpBubble("m2Compatible.help", new ResourceModel("m2Compatible.help")));
    }

    private String getFullUrl(String host, String path) {
        return new StringBuilder(host).append("/").append(path).toString();
    }

    private String getRepositoryLabel(RepoType repoType) {
        return getLabel(repoType, "Repository");
    }

    private String getResolverLabel(RepoType repoType) {
        return getLabel(repoType, "Resolver Name");
    }

    private String getLayoutLabel(RepoType repoType) {
        return getLabel(repoType, "Repository Layout");
    }

    private String getLabel(RepoType repoType, String baseLabel) {
        if (repoType == null) {
            return baseLabel;
        }

        return repoType.getTypeLabel() + " " + baseLabel;
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

        LIBS("Libs", "libs"),
        PLUGINS("Plugins", "plugins");

        private String typeLabel;
        private DefaultVirtualRepoKeySelector defaultVirtualRepoKeySelector;

        private RepoType(String typeLabel, String typeAutoSelectKeyword) {

            this.typeLabel = typeLabel;
            this.defaultVirtualRepoKeySelector = new DefaultVirtualRepoKeySelector(typeAutoSelectKeyword);
        }

        public String getTypeLabel() {
            return typeLabel;
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
            public boolean acceptedAsDefault(T virtualRepoDescriptor) {
                return StringUtils.containsIgnoreCase(virtualRepoDescriptor.getKey(), keyword);
            }
        }
    }

    private static class DefaultLayoutSelector extends DefaultOptionSelector<RepoLayout> {

        private final RepoDescriptor virtualRepoDescriptor;

        private DefaultLayoutSelector(RepoDescriptor virtualRepoDescriptor) {
            this.virtualRepoDescriptor = virtualRepoDescriptor;
        }

        @Override
        public boolean acceptedAsDefault(RepoLayout option) {
            RepoLayout virtualRepoLayout = virtualRepoDescriptor.getRepoLayout();

            return (virtualRepoLayout != null) && virtualRepoLayout.getName().equals(option.getName());
        }
    }
}
