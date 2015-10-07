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

package org.artifactory.traffic.entry;

import org.artifactory.traffic.TrafficAction;

/**
 * Main interface for the traffic entry objects
 *
 * @author Noam Tenne
 */
public interface TrafficEntry extends Comparable<TrafficEntry> {

    /**
     * Returns the entry's event date
     *
     * @return Entry event time in millis
     */
    long getTime();

    /**
     * Returns the entry's action type
     *
     * @return TrafficAction - Entry action type
     */
    TrafficAction getAction();

    /**
     * Returns the duration it took the entry's action to execute
     *
     * @return Execution duration in millis
     */
    long getDuration();
}