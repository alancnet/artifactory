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

package org.artifactory.common;

/**
 * @author freds
 * @date Nov 6, 2008
 */
public enum StatusEntryLevel {
    DEBUG, INFO, WARNING, ERROR;

    public boolean isError() {
        return this == ERROR;
    }

    public boolean isDebug() {
        return this == DEBUG;
    }

    public boolean isInfo() {
        return this == INFO;
    }

    public boolean isWarning() {
        return this == WARNING;
    }
}
