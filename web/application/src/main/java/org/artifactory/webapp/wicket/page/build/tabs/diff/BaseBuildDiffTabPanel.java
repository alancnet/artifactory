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

package org.artifactory.webapp.wicket.page.build.tabs.diff;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.webapp.wicket.page.build.actionable.BuildsDiffActionableItem;
import org.artifactory.webapp.wicket.page.build.actionable.BuildsDiffDependencyActionableItem;
import org.codehaus.plexus.util.StringUtils;
import org.jfrog.build.api.Build;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Shay Yaakov
 */
public abstract class BaseBuildDiffTabPanel extends Panel {

    @SpringBean
    private BuildService buildService;

    protected BaseArtifactsDiffListPanel artifactsDiffListPanel;
    protected BaseDependenciesDiffListPanel dependenciesDiffListPanel;
    protected BasePropertiesDiffListPanel envDiffListPanel;

    private List<BuildsDiffActionableItem> dependencies = Lists.newArrayList();
    protected Build build;

    @WicketProperty
    private boolean excludeInternalDependencies;

    public BaseBuildDiffTabPanel(String id, Build build) {
        super(id);
        this.build = build;
        addArtifactsDiffTable();
        addDependenciesDiffTable();
        addEnvDiffTable();
        addBuildToCompareComboBox(build);
        addExcludeInternalDependenciesCheckBox(build);
    }

    private void addBuildToCompareComboBox(Build build) {
        List<BuildRun> buildsRunList;
        if (build != null) {
            buildsRunList = buildService.getAllPreviousBuilds(build.getName(), build.getNumber(), build.getStarted());
        } else {
            buildsRunList = Lists.newArrayList();
        }
        final DropDownChoice<BuildRun> buildToCompareAgainst = new DropDownChoice<>("buildToCompareAgainst",
                new Model<BuildRun>(), buildsRunList, new ChoiceRenderer<BuildRun>("number") {
            @Override
            public Object getDisplayValue(BuildRun build) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(build.getNumber());
                if (StringUtils.isNotBlank(build.getReleaseStatus())) {
                    buffer.append(" ").append(build.getReleaseStatus());
                }
                return buffer.toString();
            }
        });
        buildToCompareAgainst.setNullValid(true);
        buildToCompareAgainst.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                BuildRun selectedBuildRun = buildToCompareAgainst.getModel().getObject();
                if (selectedBuildRun == null) {
                    return;
                }

                Build selectedBuild = buildService.getBuild(selectedBuildRun);

                artifactsDiffListPanel.getTableDataProvider().setData(
                        artifactsDiffListPanel.getArtifacts(selectedBuild));
                artifactsDiffListPanel.setSecondItemName(selectedBuild.getNumber());
                dependencies = dependenciesDiffListPanel.getDependencies(selectedBuild);
                setDependenciesDataProvider();
                dependenciesDiffListPanel.setSecondItemName(selectedBuild.getNumber());
                envDiffListPanel.getTableDataProvider().setData(envDiffListPanel.getProperties(selectedBuild));
                envDiffListPanel.setSecondItemName(selectedBuild.getNumber());
                target.add(artifactsDiffListPanel, dependenciesDiffListPanel, envDiffListPanel);
            }
        });
        add(buildToCompareAgainst);
        add(new HelpBubble("buildToCompareAgainst.help", getString("buildToCompareAgainst.help")));
    }

    private void addExcludeInternalDependenciesCheckBox(Build build) {
        final StyledCheckbox excludeInternalDependencies = new StyledCheckbox("excludeInternalDependencies",
                new PropertyModel<Boolean>(this, "excludeInternalDependencies"));
        excludeInternalDependencies.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                setDependenciesDataProvider();
                target.add(dependenciesDiffListPanel);
            }
        });
        add(excludeInternalDependencies);
        add(new HelpBubble("excludeInternalDependencies.help", getString("excludeInternalDependencies.help")));
    }

    private void setDependenciesDataProvider() {
        if (excludeInternalDependencies) {
            setDataProviderWithExcludedDependencies();
        } else {
            setDataProviderWithoutExcludedDependencies();
        }
    }

    private void setDataProviderWithExcludedDependencies() {
        List<BuildsDiffActionableItem> artifactsData = artifactsDiffListPanel.getTableDataProvider().getData();
        List<BuildsDiffActionableItem> dependenciesData = dependencies;

        if (artifactsData == null || dependenciesData == null) {
            if (dependencies != null && !dependencies.isEmpty()) {
                setDataProviderWithoutExcludedDependencies();
            }
            return;
        }

        Iterable<BuildsDiffActionableItem> filtered = Iterables.filter(dependenciesData,
                new Predicate<BuildsDiffActionableItem>() {
                    @Override
                    public boolean apply(@Nonnull BuildsDiffActionableItem input) {
                        return !((BuildsDiffDependencyActionableItem) input).isInternalDependency();
                    }
                });

        dependenciesDiffListPanel.getTableDataProvider().setData(Lists.newArrayList(filtered));
    }

    private void setDataProviderWithoutExcludedDependencies() {
        dependenciesDiffListPanel.getTableDataProvider().setData(dependencies);
    }

    protected abstract Panel getArtifactsDiffListPanel(String id);

    protected abstract Panel getDependenciesDiffListPanel(String id);

    protected abstract Panel getEnvDiffListPanel(String id);

    protected void addArtifactsDiffTable() {
        add(getArtifactsDiffListPanel("artifactsDiffPanel"));
    }

    protected void addDependenciesDiffTable() {
        add(getDependenciesDiffListPanel("dependenciesDiffPanel"));
    }

    protected void addEnvDiffTable() {
        add(getEnvDiffListPanel("envDiffPanel"));
    }
}
