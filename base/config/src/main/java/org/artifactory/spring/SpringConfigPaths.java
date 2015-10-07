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

package org.artifactory.spring;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.artifactory.addon.AddonInfo;
import org.artifactory.addon.AddonState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Yoav Landman
 */
public class SpringConfigPaths {

    private static final Logger log = LoggerFactory.getLogger(SpringConfigPaths.class);

    private final ImmutableList<String> paths;
    private final ImmutableMap<String, AddonInfo> installedAddonPaths;

    public SpringConfigPaths(List<String> paths, Map<String, AddonInfo> installedAddonPaths) {
        this.paths = ImmutableList.copyOf(paths);
        this.installedAddonPaths = ImmutableMap.copyOf(installedAddonPaths);
    }

    public ImmutableMap<String, AddonInfo> getInstalledAddonPaths() {
        return installedAddonPaths;
    }

    /**
     * @return An array of all the spring xml configuration paths. This includes the standard configuration paths and
     * the enabled addons configuration paths.
     */
    public String[] getAllPaths() {
        List<String> allPaths = new ArrayList<>();
        allPaths.addAll(paths);
        for (AddonInfo info : installedAddonPaths.values()) {
            AddonState addonState = info.getAddonState();
            if (addonState.equals(AddonState.ACTIVATED) || addonState.equals(AddonState.INACTIVATED)) {
                allPaths.add(info.getAddonPath());
            }
        }
        String[] pathsToReturn = allPaths.toArray(new String[allPaths.size()]);
        log.debug("Spring configuration paths: " + Arrays.toString(pathsToReturn));
        return pathsToReturn;
    }
}
