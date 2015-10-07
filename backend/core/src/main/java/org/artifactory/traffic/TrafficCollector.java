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

import org.artifactory.sapi.common.ArtifactorySession;

import java.util.Calendar;

/**
 * A resolution based reaper of traffic events
 *
 * @author yoavl
 */
public interface TrafficCollector {

    String getName();

    TrafficCollectorResolution getResolution();

    void addListener(TrafficCollectorListener listener);

    void removeListener(TrafficCollectorListener listener);

    /**
     * Called when traffic entries collection occurs by the traffic service. A traffic collector is expected to: (1)
     * Collect back the data from the traffic service, either from the last collected until now or by some other time
     * window based on the last collected value, and notify its listeners with the colleted data. and/or - (2) Use the
     * provided collected entries segment.
     *
     * @param lastCollected
     * @param service
     * @param session
     */
    void collect(Calendar lastCollected, InternalTrafficService service, ArtifactorySession session);
}