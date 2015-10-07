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
import org.artifactory.fs.MutableStatsInfo;
import org.artifactory.fs.StatsInfo;

/**
 * @author freds
 */
public class StatsInfoPersistenceHandler extends AbstractMetadataPersistenceHandler<StatsInfo, MutableStatsInfo> {

    public StatsInfoPersistenceHandler(XmlMetadataProvider<StatsInfo, MutableStatsInfo> xmlProvider) {
        super(xmlProvider);
    }

    @Override
    public MutableStatsInfo copy(StatsInfo original) {
        return InfoFactoryHolder.get().copyStats(original);
    }
}
