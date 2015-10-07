/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.webapp.wicket.panel.vcs;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.common.wicket.behavior.border.TitledBorderBehavior;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.VcsConfiguration;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;

import java.util.Arrays;

/**
 * @author Shay Yaakov
 */
public class VcsSupportConfigSection extends WebMarkupContainer {

    public VcsSupportConfigSection(RemoteRepoDescriptor descriptor) {
        super("vcsSupportSection");
        setOutputMarkupId(true);
        add(new TitledBorderBehavior("fieldset-border", "Vcs"));
        final StyledCheckbox enableVcsSupport = new StyledCheckbox("enableVcsSupport");
        add(enableVcsSupport.setTitle("Enable Vcs Support").setOutputMarkupId(true));
        add(new SchemaHelpBubble("enableVcsSupport.help"));

        VcsConfiguration vcs = descriptor.getVcs();
        if (vcs == null) {
            vcs = new VcsConfiguration();
            descriptor.setVcs(vcs);
        }

        final boolean vcsEnabled = descriptor.getType().equals(RepoType.VCS);
        final boolean isCustom = VcsGitProvider.CUSTOM.equals(vcs.getGit().getProvider());
        final WebMarkupContainer downloadUrlField = new WebMarkupContainer("downloadUrlField");
        downloadUrlField.setVisible(vcsEnabled && isCustom).setOutputMarkupPlaceholderTag(true);
        add(downloadUrlField);
        final TextField<String> downloadUrl = new TextField<>("downloadUrl",
                new PropertyModel<String>(vcs.getGit(), "downloadUrl"));
        downloadUrl.setRequired(isCustom).setVisible(vcsEnabled && isCustom).setOutputMarkupId(true);

        downloadUrlField.add(downloadUrl);
        SchemaHelpModel helpModel = new SchemaHelpModel(vcs.getGit(), "downloadUrl");
        downloadUrlField.add(new SchemaHelpBubble("downloadUrl.help", helpModel));
        final DropDownChoice providersDropDown = new DropDownChoice<>("provider",
                new PropertyModel<VcsGitProvider>(vcs.getGit(), "provider"), Arrays.asList(VcsGitProvider.values()));
        providersDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                VcsGitProvider model = (VcsGitProvider) providersDropDown.getDefaultModelObject();
                boolean isCustom = VcsGitProvider.CUSTOM.equals(model);
                downloadUrlField.setVisible(isCustom);
                downloadUrl.setRequired(isCustom).setVisible(isCustom);
                target.add(downloadUrl, downloadUrlField);
            }
        });
        providersDropDown.setRequired(vcsEnabled).setEnabled(vcsEnabled).setOutputMarkupId(true);
        add(new HelpBubble("provider.help", new ResourceModel("provider.help")));
        add(providersDropDown);

        enableVcsSupport.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                boolean enabled = enableVcsSupport.isChecked();
                providersDropDown.setEnabled(enabled);
                providersDropDown.setRequired(enabled);
                downloadUrl.setEnabled(enabled);
                ajaxRequestTarget.add(providersDropDown, downloadUrl);
            }
        });
    }
}
