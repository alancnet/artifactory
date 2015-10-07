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

package org.artifactory.traffic;

/**
 * @author yoavl
 */
public enum TrafficCollectorResolution {
    SECOND(1),
    MINUTE(SECOND.secs * 60),
    HOUR(MINUTE.secs * 60),
    DAY(HOUR.secs * 24),
    WEEK(DAY.secs * 7),
    MONTH(WEEK.secs * 4);

    private final int secs;
    private final String name;

    TrafficCollectorResolution(int secs) {
        this.secs = secs;
        this.name = name().toLowerCase();
    }

    public int getSecs() {
        return secs;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}