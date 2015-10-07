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

package org.artifactory.api.repo;

/**
 * The main listener interface for the request annotation
 *
 * @author Noam Tenne
 */
public interface RequestListener {

    /**
     * Called before the request has begun
     *
     * @param remoteAddress The address that the request was originated from
     */
    void onBeginRequest(String remoteAddress);

    /**
     * Called after the request has ended
     *
     * @param remoteAddress The address that the request was originated from
     */
    void onEndRequest(String remoteAddress);

    /**
     * Called when an exception occurrs while invoking the method
     *
     * @param remoteAddress The address that the request was originated from
     */
    void onException(String remoteAddress);
}