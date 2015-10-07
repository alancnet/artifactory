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

package org.artifactory.security;

import org.artifactory.common.Info;

import java.util.List;

/**
 * Date: 8/2/11
 * Time: 10:07 AM
 *
 * @author Fred Simon
 */
public interface PermissionTargetInfo extends Info {
    String ANY_PERMISSION_TARGET_NAME = "Anything";
    String ANY_REMOTE_PERMISSION_TARGET_NAME = "Any Remote";
    String ANY_PATH = "**";
    String ANY_REPO = "ANY";
    String ANY_LOCAL_REPO = "ANY LOCAL";
    String ANY_REMOTE_REPO = "ANY REMOTE";

    String getName();

    List<String> getRepoKeys();

    List<String> getIncludes();

    List<String> getExcludes();

    String getIncludesPattern();

    String getExcludesPattern();
}
