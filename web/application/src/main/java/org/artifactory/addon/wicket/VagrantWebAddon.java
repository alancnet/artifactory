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

package org.artifactory.addon.wicket;

import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;

/**
 * Webapp Vagrant functionality interface
 *
 * @author Shay Yaakov
 */
public interface VagrantWebAddon extends Addon {

    /**
     * Assemble the repo Vagrant configuration section and add it to the given form
     *
     * @param form       Repo configuration form
     * @param descriptor Configured repo descriptor
     */
    void createAndAddRepoConfigVagrantSection(Form<LocalRepoDescriptor> form, LocalRepoDescriptor descriptor);
}
