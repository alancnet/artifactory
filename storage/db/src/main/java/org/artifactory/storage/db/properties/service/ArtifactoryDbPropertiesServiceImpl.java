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

import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.properties.dao.DbPropertiesDao;
import org.artifactory.storage.db.properties.model.DbProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

/**
 * @author Gidi Shabat
 */
@Service
public class ArtifactoryDbPropertiesServiceImpl implements ArtifactoryDbPropertiesService {
    @Autowired
    private DbPropertiesDao dbPropertiesDao;

    @Override
    public void updateDbProperties(DbProperties dbProperties) {
        try {
            dbPropertiesDao.createProperties(dbProperties);
        } catch (SQLException e) {
            throw new StorageException("Failed to load db properties from database", e);
        }
    }

    @Override
    public DbProperties getDbProperties() {
        try {
            return dbPropertiesDao.getLatestProperties();
        } catch (SQLException e) {
            throw new StorageException("Failed to load db properties from database", e);
        }
    }

    @Override
    public boolean isDbPropertiesTableExists() {
        try {
            return dbPropertiesDao.isDbPropertiesTableExists();
        } catch (SQLException e) {
            throw new StorageException("Failed to check if the  db_properties table exists in the database", e);
        }
    }
}
