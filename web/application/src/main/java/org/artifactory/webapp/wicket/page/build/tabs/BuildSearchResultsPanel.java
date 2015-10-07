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

package org.artifactory.webapp.wicket.page.build.tabs;

import com.google.common.collect.Sets;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonType;
import org.artifactory.api.build.BuildService;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.NopFormComponentUpdatingBehavior;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.model.SelectedItemModel;
import org.artifactory.webapp.wicket.page.search.SaveSearchResultsPanel;
import org.jfrog.build.api.Build;

import java.util.Set;

/**
 * Implementation of the results management panel for build info
 *
 * @author Noam Y. Tenne
 */
public class BuildSearchResultsPanel extends SaveSearchResultsPanel {

    @SpringBean
    protected BuildService buildService;

    @WicketProperty
    protected boolean artifacts = true;

    @WicketProperty
    protected boolean dependencies = true;

    protected Build build;

    protected Set<String> scopes;
    protected Set<String> selectedScopes;

    public BuildSearchResultsPanel(AddonType requestingAddon, Build build) {
        super("saveSearchResultsPanel", new Model(), requestingAddon);
        this.build = build;

        scopes = buildService.findScopes(build);
        selectedScopes = Sets.newHashSet(scopes);

        add(new CssClass("build-save-results"));
    }

    @Override
    public String getTitle() {
        return "Save to Search Results";
    }

    @Override
    protected void addAdditionalFields(Form form) {
        final MarkupContainer scopesContainer = createScopesContainer();
        form.add(scopesContainer);

        final StyledCheckbox artifactsCheckbox = addCheckBox(form, "artifacts",
                "If marked, published module artifacts are saved as search results.", true);
        artifactsCheckbox.add(new NopFormComponentUpdatingBehavior("onclick"));

        final StyledCheckbox dependenciesCheckbox = addCheckBox(form, "dependencies",
                "If marked, published module dependencies are saved as search results.\nYou can optionally select the dependency scopes to include.",
                false);
        if (scopes.isEmpty()) {
            dependenciesCheckbox.add(new NopFormComponentUpdatingBehavior("onclick"));
        } else {
            dependenciesCheckbox.setLabel(Model.of("Include Dependencies of the following scopes:"));
            dependenciesCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(scopesContainer);
                }

                @Override
                protected IAjaxCallDecorator getAjaxCallDecorator() {
                    return new NoAjaxIndicatorDecorator();
                }
            });
        }
    }

    private MarkupContainer createScopesContainer() {
        WebMarkupContainer scopesContainer = new WebMarkupContainer("scopesContainer");
        scopesContainer.setOutputMarkupId(true);
        if (scopes.isEmpty()) {
            scopesContainer.setVisible(false);
            return scopesContainer;
        }

        RepeatingView scopesView = new RepeatingView("scopeCheckbox");
        scopesContainer.add(scopesView);

        for (String scope : scopes) {
            StyledCheckbox checkbox =
                    new StyledCheckbox(scopesView.newChildId(), new SelectedItemModel<>(selectedScopes, scope)) {
                        @Override
                        public boolean isEnabled() {
                            return dependencies && super.isEnabled();
                        }
                    };
            checkbox.setLabel(Model.of(scope));
            scopesView.add(checkbox);
        }
        return scopesContainer;
    }

    /**
     * Adds a checkbox to the given form
     *
     * @param form           Form to add the checkbox to
     * @param id             ID to assign to the checkbox
     * @param helpMessage    Help message to display for the checkbox
     * @param checkByDefault Should the checkbox be checked by default
     */
    private StyledCheckbox addCheckBox(Form form, String id, String helpMessage, boolean checkByDefault) {
        StyledCheckbox checkbox = new StyledCheckbox(id, new PropertyModel<Boolean>(this, id));
        checkbox.setDefaultModelObject(checkByDefault);
        checkbox.setOutputMarkupId(true);
        form.add(checkbox);
        form.add(new HelpBubble(id + ".help", helpMessage));
        return checkbox;
    }
}