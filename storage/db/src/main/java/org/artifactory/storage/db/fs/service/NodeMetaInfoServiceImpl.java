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

package org.artifactory.storage.db.fs.service;

import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.fs.dao.NodeMetaInfoDao;
import org.artifactory.storage.db.fs.entity.NodeMetaInfo;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.ItemMetaInfo;
import org.artifactory.storage.fs.service.NodeMetaInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.SQLException;

/**
 * A business service to interact with the node node meta info table.
 *
 * @author Yossi Shaul
 */
@Service
public class NodeMetaInfoServiceImpl implements NodeMetaInfoService {

    @Autowired
    private NodeMetaInfoDao nodeMetaInfoDao;

    @Autowired
    private FileService fileService;

    @Override
    @Nullable
    public ItemMetaInfo getNodeMetaInfo(RepoPath repoPath) {
        long nodeId = fileService.getNodeId(repoPath);
        try {
            NodeMetaInfo nodeMetadata = nodeMetaInfoDao.getNodeMetadata(nodeId);
            if (nodeMetadata != null) {
                return itemMetaInfoFromNodeMetaInfo(nodeMetadata);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new VfsException("Failed to load node metadata info", e);
        }
    }

    @Override
    public void createOrUpdateNodeMetaInfo(long nodeId, ItemMetaInfo metaInfo) {
        NodeMetaInfo nodeMetaInfo = nodeMetaInfoFromItemMetaInfo(nodeId, metaInfo);
        try {
            if (nodeMetaInfoDao.hasNodeMetadata(nodeId)) {
                nodeMetaInfoDao.update(nodeMetaInfo);
            } else {
                nodeMetaInfoDao.create(nodeMetaInfo);
            }
        } catch (SQLException e) {
            throw new VfsException("Failed to create or update node metadata info", e);
        }
    }

    @Override
    public boolean hasNodeMetadata(RepoPath repoPath) {
        long nodeId = fileService.getNodeId(repoPath);
        try {
            return nodeMetaInfoDao.hasNodeMetadata(nodeId);
        } catch (SQLException e) {
            throw new VfsException("Failed to load node metadata info", e);
        }
    }

    @Override
    public void deleteMetaInfo(RepoPath repoPath) {
        long nodeId = fileService.getNodeId(repoPath);
        deleteMetaInfo(nodeId);
    }

    @Override
    public void deleteMetaInfo(long nodeId) {
        try {
            nodeMetaInfoDao.deleteNodeMeta(nodeId);
        } catch (SQLException e) {
            throw new VfsException("Failed to delete node metadata info", e);
        }
    }

    private ItemMetaInfo itemMetaInfoFromNodeMetaInfo(NodeMetaInfo nodeMetadata) {
        return new ItemMetaInfo(nodeMetadata.getPropsModified(), nodeMetadata.getPropsModifiedBy());
    }

    private NodeMetaInfo nodeMetaInfoFromItemMetaInfo(long nodeId, ItemMetaInfo metaInfo) {
        return new NodeMetaInfo(nodeId, metaInfo.getPropsModified(), metaInfo.getPropsModifiedBy());
    }
}
