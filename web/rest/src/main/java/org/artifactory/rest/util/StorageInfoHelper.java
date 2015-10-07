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

package org.artifactory.rest.util;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.api.rest.artifact.RestBaseStorageInfo;
import org.artifactory.api.rest.artifact.RestFileInfo;
import org.artifactory.api.rest.artifact.RestFolderInfo;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.rest.common.util.RestUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Helper class for returning search results (quick, gavc, property, checksum, xpath)
 * with optionally full info (same as file info and folder info REST API) and properties.
 *
 * @author Shay Yaakov
 */
public class StorageInfoHelper {

    private static final String INCLUDE_INFO_PARAM = "info";
    private static final String INCLUDE_PROPERTIES_PARAM = "properties";

    private HttpServletRequest request;
    private RepositoryService repositoryService;
    private RepositoryBrowsingService repoBrowsingService;
    private ItemInfo itemInfo;

    public StorageInfoHelper(HttpServletRequest request, RepositoryService repositoryService,
            RepositoryBrowsingService repoBrowsingService, ItemInfo itemInfo) {
        this.request = request;
        this.repositoryService = repositoryService;
        this.repoBrowsingService = repoBrowsingService;
        this.itemInfo = itemInfo;
    }

    public RestBaseStorageInfo createStorageInfo() {
        if (itemInfo instanceof FileInfo) {
            return createFileInfo();
        } else {
            return createFolderInfo();
        }
    }

    private RestFileInfo createFileInfo() {
        RestFileInfo fileInfo = new RestFileInfo();
        String uri = RestUtils.buildStorageInfoUri(request, itemInfo.getRepoKey(), itemInfo.getRelPath());
        fileInfo.slf = uri;
        addExtraFileInfo(fileInfo, uri);
        addStorageInfoProperties(fileInfo);
        return fileInfo;
    }

    private RestFolderInfo createFolderInfo() {
        RestFolderInfo folderInfo = new RestFolderInfo();
        String uri = RestUtils.buildStorageInfoUri(request, itemInfo.getRepoKey(), itemInfo.getRelPath());
        folderInfo.slf = uri;
        addExtraFolderInfo(folderInfo, uri);
        addStorageInfoProperties(folderInfo);
        return folderInfo;
    }

    private void addExtraFileInfo(RestFileInfo fileInfo, String uri) {
        if (!isIncludeExtraInfo()) {
            return;
        }

        setBaseStorageInfo(fileInfo, uri);

        fileInfo.mimeType = NamingUtils.getMimeTypeByPathAsString(itemInfo.getRelPath());
        fileInfo.downloadUri = RestUtils.buildDownloadUri(request, itemInfo.getRepoKey(), itemInfo.getRelPath());
        fileInfo.remoteUrl = buildDownloadUrl();
        fileInfo.size = String.valueOf(((FileInfo) itemInfo).getSize());
        ChecksumsInfo checksumInfo = ((FileInfo) itemInfo).getChecksumsInfo();
        ChecksumInfo sha1 = checksumInfo.getChecksumInfo(ChecksumType.sha1);
        ChecksumInfo md5 = checksumInfo.getChecksumInfo(ChecksumType.md5);
        String originalSha1 = sha1 != null ? sha1.getOriginal() : checksumInfo.getSha1();
        String originalMd5 = md5 != null ? md5.getOriginal() : checksumInfo.getMd5();
        fileInfo.checksums = new RestFileInfo.Checksums(checksumInfo.getSha1(), checksumInfo.getMd5());
        fileInfo.originalChecksums = new RestFileInfo.Checksums(originalSha1, originalMd5);
    }

    private void addExtraFolderInfo(RestFolderInfo folderInfo, String uri) {
        if (!isIncludeExtraInfo()) {
            return;
        }

        setBaseStorageInfo(folderInfo, uri);

        RepoPath folderRepoPath = InternalRepoPathFactory.create(itemInfo.getRepoKey(),
                itemInfo.getRepoPath().getPath());
        folderInfo.children = new ArrayList<>();

        //if local or cache repo
        if (isLocalRepo(itemInfo.getRepoKey())) {
            List<ItemInfo> children = repositoryService.getChildren(folderRepoPath);
            for (ItemInfo child : children) {
                folderInfo.children.add(new RestFolderInfo.DirItem("/" + child.getName(), child.isFolder()));
            }
            //for virtual repo
        } else {
            List<VirtualRepoItem> virtualRepoItems = repoBrowsingService.getVirtualRepoItems(folderRepoPath);
            for (VirtualRepoItem item : virtualRepoItems) {
                folderInfo.children.add(new RestFolderInfo.DirItem("/" + item.getName(), item.isFolder()));
            }
        }
    }

    private boolean isLocalRepo(String repoKey) {
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        return descriptor != null && !(descriptor.isCache() && !descriptor.getKey().equals(repoKey));
    }

    private void setBaseStorageInfo(RestBaseStorageInfo storageInfoRest, String uri) {
        storageInfoRest.repo = itemInfo.getRepoKey();
        storageInfoRest.path = "/" + itemInfo.getRelPath();
        storageInfoRest.created = RestUtils.toIsoDateString(itemInfo.getCreated());
        storageInfoRest.createdBy = itemInfo.getCreatedBy();
        storageInfoRest.lastModified = RestUtils.toIsoDateString(itemInfo.getLastModified());
        storageInfoRest.modifiedBy = itemInfo.getModifiedBy();
        storageInfoRest.lastUpdated = RestUtils.toIsoDateString(itemInfo.getLastUpdated());
    }

    private String buildDownloadUrl() {
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(itemInfo.getRepoKey());
        if (descriptor == null || !descriptor.isCache()) {
            return null;
        }
        RemoteRepoDescriptor remoteRepoDescriptor = ((LocalCacheRepoDescriptor) descriptor).getRemoteRepo();
        StringBuilder sb = new StringBuilder(remoteRepoDescriptor.getUrl());
        sb.append("/").append(itemInfo.getRelPath());
        return sb.toString();
    }

    private void addStorageInfoProperties(RestBaseStorageInfo storageInfo) {
        if (!isIncludeProperties()) {
            return;
        }

        // Outside the loop since we want Jackson to parse it as an empty list if there aren't any properties
        storageInfo.properties = Maps.newTreeMap();

        Properties propertiesAnnotatingItem = repositoryService.getProperties(itemInfo.getRepoPath());
        if (propertiesAnnotatingItem != null && !propertiesAnnotatingItem.isEmpty()) {
            for (String propertyName : propertiesAnnotatingItem.keySet()) {
                storageInfo.properties.put(propertyName,
                        Iterables.toArray(propertiesAnnotatingItem.get(propertyName), String.class));
            }
        }
    }

    private boolean isIncludeExtraInfo() {
        return resultDetailHeaderContainsKey(INCLUDE_INFO_PARAM);
    }

    private boolean isIncludeProperties() {
        return resultDetailHeaderContainsKey(INCLUDE_PROPERTIES_PARAM);
    }

    private boolean resultDetailHeaderContainsKey(String key) {
        String resultDetailHeader = request.getHeader(ArtifactoryRequest.RESULT_DETAIL);
        return StringUtils.contains(resultDetailHeader, key);
    }
}
