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

package org.artifactory.addon.p2;

import org.artifactory.addon.Addon;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Web interface of P2 addon.
 *
 * @author Yossi Shaul
 * @author Dan Feldman
 */
public interface P2Addon extends Addon {

    /**
     * Verifies all the remotes repositories and returns a list to be presented in the UI
     *
     * @return A list of remote repositories that are added/created/verified based on the virtual repo configuration.
     */
    List<P2Repo> verifyRemoteRepositories(MutableCentralConfigDescriptor currentDescriptor,
            VirtualRepoDescriptor virtualRepo, @Nullable List<P2Repo> currentList, @Nullable List<P2Repo> requestedList,
            @Nonnull Map<String, List<String>> subCompositeUrls, MutableStatusHolder statusHolder);
}
