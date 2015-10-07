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

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.util.CookieUtils;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.dependency.DependencyDeclarationProvider;
import org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.dependency.DependencyDeclarationProviderType;

/**
 * Displays the build-tool oriented dependency declaration of the selected artifact in the tree
 *
 * @author Noam Y. Tenne
 */
public class DependencyDeclarationPanel extends Panel {

    private static final String COOKIE_NAME = "last-selected-build-tool";

    @WicketProperty
    private DependencyDeclarationProviderType selectedDependencyDeclaration;

    private FieldSetBorder declarationBorder;
    private ModuleInfo moduleInfo;

    /**
     * Main constructor
     *
     * @param id         Panel ID
     * @param moduleInfo Module info
     * @param repoLayout
     */
    public DependencyDeclarationPanel(String id, ModuleInfo moduleInfo, final RepoLayout repoLayout) {
        super(id);
        this.moduleInfo = moduleInfo;

        declarationBorder = new FieldSetBorder("dependencyDeclarationBorder");
        declarationBorder.add(new Label("buildToolSelectorLabel", "Build Tool:"));
        final Label mavenWarningLabel = new Label("mavenWarningLabel", "Maven might not be able to resolve " +
                "this artifact since the repository isn't configured to use the Maven layout.");
        mavenWarningLabel.add(new CssClass("warn")).setOutputMarkupId(true);
        declarationBorder.add(mavenWarningLabel);

        DropDownChoice<DependencyDeclarationProviderType> buildToolSelector =
                new DropDownChoice<>("buildToolSelector",
                        new PropertyModel<DependencyDeclarationProviderType>(this, "selectedDependencyDeclaration"),
                        Lists.newArrayList(DependencyDeclarationProviderType.values()));
        buildToolSelector.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                DependencyDeclarationProviderType selectedType = getSelectedDependencyDeclaration();
                DependencyDeclarationProvider dependencyDeclarationProvider = selectedType.getDeclarationProvider();

                CookieUtils.setCookie(COOKIE_NAME, selectedType.name());
                onBuildToolSelectionChange(dependencyDeclarationProvider);

                mavenWarningLabel.setVisible(shouldDisplayMavenWarning(selectedType, repoLayout));
                target.add(declarationBorder);
                target.add(mavenWarningLabel);
            }
        });
        declarationBorder.add(buildToolSelector);

        declarationBorder.add(new WebMarkupContainer("dependencyDeclaration"));
        declarationBorder.setOutputMarkupId(true);
        add(declarationBorder);

        String lastSelectedBuildTool = CookieUtils.getCookie(COOKIE_NAME);
        if (StringUtils.isNotBlank(lastSelectedBuildTool)) {
            try {
                setSelectedDependencyDeclaration(DependencyDeclarationProviderType.valueOf(lastSelectedBuildTool));
            } catch (IllegalArgumentException e) {
                setSelectedDependencyDeclaration(null);
                CookieUtils.clearCookie(COOKIE_NAME);
            }
        }
        if (getSelectedDependencyDeclaration() == null) {
            setSelectedDependencyDeclaration(guessDependencyDeclarationByRepoLayout(repoLayout));
        }
        DependencyDeclarationProviderType selectedType = getSelectedDependencyDeclaration();
        onBuildToolSelectionChange(selectedType.getDeclarationProvider());
        mavenWarningLabel.setVisible(shouldDisplayMavenWarning(selectedType, repoLayout));
    }

    private DependencyDeclarationProviderType guessDependencyDeclarationByRepoLayout(RepoLayout repoLayout) {
        if (repoLayout.equals(RepoLayoutUtils.IVY_DEFAULT)) {
            return DependencyDeclarationProviderType.IVY;
        } else if (repoLayout.equals(RepoLayoutUtils.GRADLE_DEFAULT)) {
            return DependencyDeclarationProviderType.GRADLE;
        }

        return DependencyDeclarationProviderType.MAVEN;
    }

    private boolean shouldDisplayMavenWarning(DependencyDeclarationProviderType selectedType, RepoLayout repoLayout) {
        return DependencyDeclarationProviderType.MAVEN.equals(selectedType) && !RepoLayoutUtils.isDefaultM2(repoLayout);
    }

    public DependencyDeclarationProviderType getSelectedDependencyDeclaration() {
        return selectedDependencyDeclaration;
    }

    public void setSelectedDependencyDeclaration(DependencyDeclarationProviderType selectedDependencyDeclaration) {
        this.selectedDependencyDeclaration = selectedDependencyDeclaration;
    }

    /**
     * Replace the current dependency declaration with one of the given type
     *
     * @param dependencyDeclarationProvider Provider of new type
     */
    private void onBuildToolSelectionChange(DependencyDeclarationProvider dependencyDeclarationProvider) {
        Component syntaxHighlighter = WicketUtils.getSyntaxHighlighter("dependencyDeclaration",
                dependencyDeclarationProvider.getDependencyDeclaration(moduleInfo),
                dependencyDeclarationProvider.getSyntaxType());
        syntaxHighlighter.setOutputMarkupId(true);
        declarationBorder.replace(syntaxHighlighter);
    }
}
