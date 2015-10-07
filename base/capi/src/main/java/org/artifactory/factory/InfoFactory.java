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

package org.artifactory.factory;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.fs.*;
import org.artifactory.md.MutableMetadataInfo;
import org.artifactory.md.MutablePropertiesInfo;
import org.artifactory.md.PropertiesInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.MutableRepoResourceInfo;
import org.artifactory.resource.RepoResourceInfo;
import org.artifactory.security.*;
import org.artifactory.util.Tree;

import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;

/**
 * Date: 8/1/11
 * Time: 8:46 PM
 *
 * @author Fred Simon
 */
public interface InfoFactory {
    RepoPath createRepoPathFromId(String repoPathId);

    RepoPath createRepoPath(String repoKey, String path);

    RepoPath createRepoPath(RepoPath parent, String relPath);

    MutableRepoResourceInfo copyRepoResource(RepoResourceInfo repoResourceInfo);

    MutableItemInfo copyItemInfo(ItemInfo itemInfo);

    MutableFileInfo createFileInfo(RepoPath repoPath);

    MutableFileInfo copyFileInfo(FileInfo fileInfo);

    MutableFolderInfo createFolderInfo(RepoPath repoPath);

    MutableFolderInfo copyFolderInfo(FolderInfo folderInfo);

    MutablePropertiesInfo createProperties();

    MutablePermissionTargetInfo createPermissionTarget();

    MutablePropertiesInfo copyProperties(PropertiesInfo copy);

    MutablePermissionTargetInfo copyPermissionTarget(PermissionTargetInfo copy);

    MutableUserInfo createUser();

    MutableUserInfo copyUser(UserInfo copy);

    MutableGroupInfo createGroup();

    MutableGroupInfo copyGroup(GroupInfo copy);

    MutableAclInfo createAcl();

    MutableAclInfo copyAcl(AclInfo copy);

    MutableAceInfo createAce();

    MutableAceInfo copyAce(AceInfo copy);

    UserGroupInfo createUserGroup(String groupName);

    Set<UserGroupInfo> createGroups(Set<String> names);

    UserGroupInfo createUserGroup(String groupName, String realm);

    SecurityInfo createSecurityInfo(List<UserInfo> users, List<GroupInfo> groups, List<AclInfo> acls);

    XStream getSecurityXStream();

    MutablePermissionTargetInfo createPermissionTarget(String permName, List<String> repoKeys);

    MutableAceInfo createAce(String principal, boolean group, int mask);

    MutableGroupInfo createGroup(String groupName);

    MutableUserInfo createUser(String userName);

    MutableAclInfo createAcl(PermissionTargetInfo permissionTarget);

    /**
     * Return an immutable ACL
     * @param permissionTarget
     * @param aces
     * @param updatedBy
     * @return
     */
    AclInfo createAcl(PermissionTargetInfo permissionTarget, Set<AceInfo> aces, String updatedBy);

    Tree<ZipEntryInfo> createZipEntriesTree();

    ZipEntryInfo createZipEntry(ZipEntry... zipEntry);

    ZipEntryInfo createArchiveEntry(ArchiveEntry... archiveEntries);

    ZipEntryResourceInfo createZipEntryResource(FileInfo info, ZipEntryInfo zipEntryInfo, Long first,
            ChecksumsInfo checksumsInfo);

    MutableMetadataInfo createMetadata(RepoPath repoPath);

    XStream getFileSystemXStream();

    MutableStatsInfo createStats();

    MutableStatsInfo copyStats(StatsInfo copy);

    MutableWatchersInfo createWatchers();

    MutableWatcherInfo createWatcher(String watcherUsername, long watchCreationDate);

    MetadataEntryInfo createMetadataEntry(String metadataName, String xmlContent);

    MutableWatchersInfo copyWatchers(WatchersInfo copy);

    MutableMetadataInfo createMetadata(RepoPath repoPath, String metadataName);

    RepoPath createRepoPath(String repoKey, String path, boolean folder);
}
