/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.db.properties.service;

import org.artifactory.storage.db.properties.model.DbProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Gidi Shabat
 */
@Service
public class ArtifactoryCommonDbPropertiesServiceImpl implements ArtifactoryCommonDbPropertiesService {
    @Autowired
    private ArtifactoryDbPropertiesService dbPropertiesService;

    @Override
    public DbProperties getDbProperties() {
        return dbPropertiesService.getDbProperties();
    }

    @Override
    public boolean isDbPropertiesTableExists() {
        return dbPropertiesService.isDbPropertiesTableExists();
    }

    @Override
    public void updateDbProperties(DbProperties dbProperties) {
        dbPropertiesService.updateDbProperties(dbProperties);
    }
}
