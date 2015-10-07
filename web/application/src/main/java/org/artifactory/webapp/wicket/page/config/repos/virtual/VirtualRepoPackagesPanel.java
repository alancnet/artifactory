/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.webapp.wicket.page.config.repos.virtual;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.BowerWebAddon;
import org.artifactory.addon.wicket.GemsWebAddon;
import org.artifactory.addon.wicket.NpmWebAddon;
import org.artifactory.addon.wicket.NuGetWebAddon;
import org.artifactory.addon.wicket.PypiWebAddon;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

/**
 * Displays the local repository configuration's different packaging features (RPM, NuGet)
 *
 * @author Noam Y. Tenne
 */
public class VirtualRepoPackagesPanel extends Panel {

    @SpringBean
    private AddonsManager addonsManager;

    public VirtualRepoPackagesPanel(String id, final VirtualRepoDescriptor descriptor, boolean isCreate) {
        super(id);

        Form<VirtualRepoDescriptor> form = new SecureForm<>("form", new CompoundPropertyModel<>(descriptor));
        add(form);

        form.add(addonsManager.addonByType(NuGetWebAddon.class).
                getVirtualRepoConfigurationSection("NuGet", descriptor, form, isCreate));

        form.add(addonsManager.addonByType(GemsWebAddon.class).
                buildPackagesConfigSection("gemsSupportSection", descriptor, form));

        addonsManager.addonByType(NpmWebAddon.class).createAndAddRepoConfigNpmSection(form, descriptor, isCreate);

        addonsManager.addonByType(BowerWebAddon.class).createAndAddRepoConfigBowerSection(form, descriptor, isCreate);

        addonsManager.addonByType(PypiWebAddon.class).createAndAddPypiConfigSection(form, descriptor, isCreate);
    }
}
