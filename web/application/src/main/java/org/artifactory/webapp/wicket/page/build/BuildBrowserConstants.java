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

package org.artifactory.webapp.wicket.page.build;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Build browser path and variable constants
 *
 * @author Noam Y. Tenne
 */
public interface BuildBrowserConstants {
    String BUILDS = "builds";
    String BUILD_NAME = "buildName";
    String BUILD_NUMBER = "buildNumber";
    String BUILD_STARTED = "buildStarted";
    String MODULE_ID = "moduleName";
    String MOUNT_PATH = "/" + BUILDS + "/#{" + BUILD_NAME + "}/#{" + BUILD_NUMBER + "}/#{" + BUILD_STARTED + "}/#{" + MODULE_ID + "}";
    List<String> PATH_CONSTANTS = Lists.newArrayList(BUILD_NAME, BUILD_NUMBER, BUILD_STARTED, MODULE_ID);
}