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

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.*;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;

/**
 * Displays the local repository configuration's different packaging features (RPM, NuGet)
 *
 * @author Noam Y. Tenne
 */
public class LocalRepoPackagesPanel extends Panel {

    @SpringBean
    private AddonsManager addonsManager;

    public LocalRepoPackagesPanel(String id, LocalRepoDescriptor descriptor, boolean isCreate) {
        super(id);

        Form<LocalRepoDescriptor> form = new SecureForm<>("form", new CompoundPropertyModel<>(descriptor));
        add(form);

        addonsManager.addonByType(YumWebAddon.class).createAndAddLocalRepoYumSection(form, descriptor,
                isCreate);
        addonsManager.addonByType(NuGetWebAddon.class).createAndAddRepoConfigNuGetSection(form, descriptor, isCreate);

        form.add(addonsManager.addonByType(GemsWebAddon.class).
                buildPackagesConfigSection("gemsSupportSection", descriptor, form));

        addonsManager.addonByType(NpmWebAddon.class).createAndAddRepoConfigNpmSection(form, descriptor, isCreate);

        addonsManager.addonByType(BowerWebAddon.class).createAndAddRepoConfigBowerSection(form, descriptor, isCreate);

        addonsManager.addonByType(DebianWebAddon.class).createAndAddLocalRepoDebianSection(form, descriptor);

        addonsManager.addonByType(PypiWebAddon.class).createAndAddPypiConfigSection(form, descriptor, isCreate);
        
        addonsManager.addonByType(DockerWebAddon.class).createAndAddRepoConfigDockerSection(form, descriptor, isCreate);

        addonsManager.addonByType(VagrantWebAddon.class).createAndAddRepoConfigVagrantSection(form, descriptor);

        addonsManager.addonByType(GitLfsWebAddon.class).createAndAddRepoConfigGitLfsSection(form, descriptor);
    }

}
