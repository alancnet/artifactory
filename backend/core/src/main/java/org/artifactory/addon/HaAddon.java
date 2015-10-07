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

package org.artifactory.addon;

import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.schedule.Task;
import org.artifactory.storage.fs.lock.FsItemsVault;
import org.artifactory.storage.fs.lock.map.LockingMap;

/**
 * @author mamo
 */
public interface HaAddon extends HaCommonAddon {

    void updateArtifactoryServerRole();

    boolean deleteArtifactoryServer(String serverId);

    void propagateTaskToPrimary(Task task);

    LockingMap getLockingMap();

    LockingMap getLockingMap(String mapName);

    void init();

    FsItemsVault getFsItemVault();
}
