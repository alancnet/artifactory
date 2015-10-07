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

package org.artifactory.webapp.wicket.page.config.repos.local;

import org.apache.commons.lang.WordUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.repos.RepoGeneralSettingsPanel;

import java.util.Arrays;
import java.util.List;

import static org.artifactory.descriptor.repo.SnapshotVersionBehavior.*;

/**
 * @author Noam Y. Tenne
 */
public class LocalRepoBasicPanel extends Panel {
    @SpringBean
    private CentralConfigService centralConfigService;

    private final LocalRepoDescriptor descriptor;
    private TextField<Integer> maxUniqueSnapshots;
    private DropDownChoice<SnapshotVersionBehavior> snapshotVersionDropDown;

    public LocalRepoBasicPanel(String id, final LocalRepoDescriptor repoDescriptor, boolean create) {
        super(id);
        descriptor = repoDescriptor;

        add(new RepoGeneralSettingsPanel("generalSettings"));

        // snapshotVersionBehavior
        LocalRepoChecksumPolicyType[] checksumPolicyTypes = LocalRepoChecksumPolicyType.values();
        DropDownChoice checksumPolicyDropDown = new DropDownChoice<>("checksumPolicyType",
                Arrays.asList(checksumPolicyTypes), new ChecksumPolicyChoiceRenderer());
        add(checksumPolicyDropDown);
        add(new SchemaHelpBubble("localRepoChecksumPolicyType.help", "checksumPolicyType"));


        add(new StyledCheckbox("handleReleases"));
        add(new SchemaHelpBubble("handleReleases.help"));

        StyledCheckbox handleSnapshots = new StyledCheckbox("handleSnapshots");
        handleSnapshots.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(maxUniqueSnapshots);
            }
        });
        add(handleSnapshots);
        add(new SchemaHelpBubble("handleSnapshots.help"));

        add(new StyledCheckbox("blackedOut"));
        add(new SchemaHelpBubble("blackedOut.help"));

        // maxUniqueSnapshots
        maxUniqueSnapshots = new MaxUniqueSnapshotsTextField("maxUniqueSnapshots");
        add(maxUniqueSnapshots);
        add(new SchemaHelpBubble("maxUniqueSnapshots.help"));

        // snapshotVersionBehavior
        SnapshotVersionBehavior[] versions = SnapshotVersionBehavior.values();
        snapshotVersionDropDown = new DropDownChoice<>(
                "snapshotVersionBehavior", Arrays.asList(versions));

        boolean isMavenRepoLayout = descriptor.isMavenRepoLayout() || (repoDescriptor.getRepoLayout() == null);

        snapshotVersionDropDown.setEnabled(isMavenRepoLayout);
        snapshotVersionDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (NONUNIQUE.equals(descriptor.getSnapshotVersionBehavior()) && descriptor.isMavenRepoLayout()) {
                    descriptor.setMaxUniqueSnapshots(0);
                }
                target.add(maxUniqueSnapshots);
            }
        });
        snapshotVersionDropDown.setChoiceRenderer(new SnapshotVersionChoiceRenderer());
        add(snapshotVersionDropDown);
        add(new SchemaHelpBubble("snapshotVersionBehavior.help"));

        final StyledCheckbox suppressPomConsistencyChecks = new StyledCheckbox("suppressPomConsistencyChecks");
        suppressPomConsistencyChecks.setOutputMarkupId(true);
        suppressPomConsistencyChecks.setEnabled(isMavenRepoLayout);

        add(suppressPomConsistencyChecks);
        add(new SchemaHelpBubble("suppressPomConsistencyChecks.help"));

        add(new StyledCheckbox("archiveBrowsingEnabled"));
        add(new SchemaHelpBubble("archiveBrowsingEnabled.help"));

        List<RepoLayout> layouts = centralConfigService.getDescriptor().getRepoLayouts();

        DropDownChoice<RepoLayout> repoLayout = new DropDownChoice<>("repoLayout", layouts,
                new ChoiceRenderer<RepoLayout>("name"));
        repoLayout.setRequired(true);
        repoLayout.setNullValid(false);
        if (repoDescriptor.getRepoLayout() == null) {
            repoLayout.setModel(new PropertyModel<RepoLayout>(repoDescriptor, "repoLayout"));
            repoLayout.setDefaultModelObject(RepoLayoutUtils.MAVEN_2_DEFAULT);
        }
        repoLayout.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                suppressPomConsistencyChecks.setEnabled(repoDescriptor.isMavenRepoLayout());
                suppressPomConsistencyChecks.setDefaultModelObject(!repoDescriptor.isMavenRepoLayout());
                snapshotVersionDropDown.setEnabled(descriptor.isMavenRepoLayout());
                target.add(maxUniqueSnapshots);
                target.add(snapshotVersionDropDown);
                target.add(suppressPomConsistencyChecks);
            }
        });
        repoLayout.setEnabled(create);
        add(repoLayout);
        add(new SchemaHelpBubble("repoLayout.help"));
    }

    private class MaxUniqueSnapshotsTextField extends TextField<Integer> {
        public MaxUniqueSnapshotsTextField(String id) {
            super(id, Integer.class);
            add(new RangeValidator<>(0, Integer.MAX_VALUE));
            setRequired(true);
            setOutputMarkupId(true);
        }

        @Override
        public boolean isEnabled() {
            SnapshotVersionBehavior snapshotVersionBehavior = descriptor.getSnapshotVersionBehavior();
            boolean isUnique = UNIQUE.equals(snapshotVersionBehavior);
            boolean isDeployer = DEPLOYER.equals(snapshotVersionBehavior);
            boolean isMavenLayout = descriptor.isMavenRepoLayout();
            return (descriptor.isHandleSnapshots() && ((isUnique || isDeployer) || !isMavenLayout));
        }
    }

    private static class SnapshotVersionChoiceRenderer extends ChoiceRenderer<SnapshotVersionBehavior> {
        @Override
        public String getDisplayValue(SnapshotVersionBehavior object) {
            return WordUtils.capitalizeFully(object.toString());
        }
    }

    private static class ChecksumPolicyChoiceRenderer extends ChoiceRenderer<LocalRepoChecksumPolicyType> {
        @Override
        public String getDisplayValue(LocalRepoChecksumPolicyType object) {
            if (LocalRepoChecksumPolicyType.SERVER.equals(object)) {
                return "Trust server generated checksums";
            } else {
                return "Verify against client checksums";
            }
        }
    }
}
