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

package org.artifactory.md;

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableWatchersInfo;
import org.artifactory.fs.WatchersInfo;

/**
 * @author freds
 */
public class WatchersPersistenceHandler
        extends AbstractXmlContentPersistenceHandler<WatchersInfo, MutableWatchersInfo> {

    public WatchersPersistenceHandler(WatchersXmlProvider xmlProvider) {
        super(xmlProvider);
    }

    @Override
    public MutableWatchersInfo copy(WatchersInfo original) {
        return InfoFactoryHolder.get().copyWatchers(original);
    }
}