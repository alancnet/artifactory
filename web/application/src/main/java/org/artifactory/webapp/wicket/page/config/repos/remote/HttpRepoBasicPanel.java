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

package org.artifactory.webapp.wicket.page.config.repos.remote;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.repos.RepoGeneralSettingsPanel;
import org.artifactory.webapp.wicket.util.validation.UriValidator;

import java.util.Arrays;

/**
 * @author Noam Y. Tenne
 */
public class HttpRepoBasicPanel extends Panel {

    public HttpRepoBasicPanel(String id, final HttpRepoDescriptor repoDescriptor) {
        super(id);
        add(new CssClass("advanced-remote-repo"));

        HttpRepoGeneralSettingsPanel generalSettings = new HttpRepoGeneralSettingsPanel("generalSettings");
        add(generalSettings);

        // url
        TextField<String> urlField = new TextField<>("url");
        urlField.add(new UriValidator("http", "https"));
        generalSettings.add(urlField);
        generalSettings.add(new SchemaHelpBubble("url.help"));

        final TextField<Integer> maxUniqueSnapshots = new TextField<>("maxUniqueSnapshots", Integer.class);
        maxUniqueSnapshots.setEnabled(repoDescriptor.isHandleSnapshots());
        maxUniqueSnapshots.setRequired(true);
        maxUniqueSnapshots.setOutputMarkupId(true);
        add(maxUniqueSnapshots);
        add(new SchemaHelpBubble("maxUniqueSnapshots.help"));

        // checksumPolicyType
        ChecksumPolicyType[] checksumPolicies = ChecksumPolicyType.values();
        DropDownChoice<ChecksumPolicyType> checksumPoliciesDC = new DropDownChoice<>(
                "checksumPolicyType", Arrays.asList(checksumPolicies));
        checksumPoliciesDC.setChoiceRenderer(new ChecksumPolicyChoiceRenderer());
        add(checksumPoliciesDC);
        add(new SchemaHelpBubble("remoteRepoChecksumPolicyType.help", "checksumPolicyType"));

        // checkboxes
        add(new StyledCheckbox("handleReleases"));
        add(new SchemaHelpBubble("handleReleases.help"));

        StyledCheckbox handleSnapshots = new StyledCheckbox("handleSnapshots");
        handleSnapshots.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                maxUniqueSnapshots.setEnabled(repoDescriptor.isHandleSnapshots());
                if (!repoDescriptor.isHandleSnapshots()) {
                    maxUniqueSnapshots.setModelObject(0);
                }
                target.add(maxUniqueSnapshots);
            }
        });
        add(handleSnapshots);
        add(new SchemaHelpBubble("handleSnapshots.help"));

        add(new StyledCheckbox("blackedOut"));
        add(new SchemaHelpBubble("blackedOut.help"));

        add(new StyledCheckbox("offline"));
        add(new SchemaHelpBubble("offline.help"));

        add(new StyledCheckbox("shareConfiguration"));
        add(new SchemaHelpBubble("shareConfiguration.help"));
    }

    private static class ChecksumPolicyChoiceRenderer extends ChoiceRenderer<ChecksumPolicyType> {
        @Override
        public String getDisplayValue(ChecksumPolicyType policy) {
            switch (policy) {
                case FAIL:
                    return "Fail";
                case GEN_IF_ABSENT:
                    return "Generate if absent";
                case IGNORE_AND_GEN:
                    return "Ignore and generate";
                case PASS_THRU:
                    return "Ignore and pass-through";
                default:
                    return String.valueOf(policy);
            }
        }
    }

    private static class HttpRepoGeneralSettingsPanel extends RepoGeneralSettingsPanel {
        public HttpRepoGeneralSettingsPanel(String id) {
            super(id);
        }
    }
}
