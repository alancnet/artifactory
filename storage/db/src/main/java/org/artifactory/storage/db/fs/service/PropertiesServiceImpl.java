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

package org.artifactory.storage.db.fs.service;

import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.dao.PropertiesDao;
import org.artifactory.storage.db.fs.entity.NodeProperty;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.PropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author Yossi Shaul
 */
@Service
public class PropertiesServiceImpl implements PropertiesService {

    @Autowired
    private DbService dbService;

    @Autowired
    private PropertiesDao propertiesDao;

    @Autowired
    private FileService fileService;

    //TODO: [by YS] check how to pass an id instead of finding the id for each repo path passed

    @Override
    public Properties getProperties(RepoPath repoPath) {
        long nodeId = fileService.getNodeId(repoPath);
        if (nodeId > 0) {
            return loadProperties(nodeId);
        } else {
            return new PropertiesImpl();
        }
    }

    @Override
    @Nonnull
    public Properties loadProperties(long nodeId) {
        try {
            List<NodeProperty> nodeProperties = propertiesDao.getNodeProperties(nodeId);
            PropertiesImpl properties = new PropertiesImpl();
            for (NodeProperty nodeProperty : nodeProperties) {
                properties.put(nodeProperty.getPropKey(), nodeProperty.getPropValue());
            }
            return properties;
        } catch (SQLException e) {
            throw new StorageException("Failed to load properties for " + nodeId, e);
        }
    }

    @Override
    public boolean hasProperties(RepoPath repoPath) {
        long nodeId = fileService.getNodeId(repoPath);
        try {
            if (nodeId > 0) {
                return propertiesDao.hasNodeProperties(nodeId);
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to check properties for " + repoPath, e);
        }
    }

    /**
     * Sets properties
     *
     * @param nodeId     Id of the node to set properties on
     * @param properties The properties to set
     */
    @Override
    public void setProperties(long nodeId, Properties properties) {
        try {
            // first delete existing properties is exist
            deleteProperties(nodeId);

            // create record for each node property. one record for each key/value combination
            for (Map.Entry<String, String> propEntry : properties.entries()) {
                NodeProperty property = new NodeProperty(dbService.nextId(), nodeId, propEntry.getKey(),
                        propEntry.getValue());
                propertiesDao.create(property);
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to set properties on node: " + nodeId, e);
        }
    }

    @Override
    public int deleteProperties(long nodeId) {
        try {
            return propertiesDao.deleteNodeProperties(nodeId);
        } catch (SQLException e) {
            throw new StorageException("Failed to delete properties for node: " + nodeId, e);
        }
    }
}
