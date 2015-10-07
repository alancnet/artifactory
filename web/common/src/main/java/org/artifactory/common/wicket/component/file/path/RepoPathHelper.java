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

package org.artifactory.common.wicket.component.file.path;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generates paths for repository that partially match input.
 * @author Yoav Luft
 */
public class RepoPathHelper extends PathHelper implements Serializable {

    private RepositoryService repositoryService;
    private String repoKey;

    public RepoPathHelper(RepositoryService repositoryService, String repoKey) {
        this.repositoryService = repositoryService;
        this.repoKey = repoKey;
    }

    public Collection<String> getPaths(String repoPath, PathMask mask) {
        if (repoKey == null || repoPath == null) return Collections.emptyList();
        String parentPath = getFilePath(repoPath);
        final String element = getFileName(repoPath);
        RepoPath parent = RepoPathFactory.create(repoKey, parentPath);
        List<ItemInfo> children = repositoryService.getChildren(parent);
        Collection<ItemInfo> filtered = Collections2.filter(children, new Predicate<ItemInfo>() {
            @Override
            public boolean apply(@Nullable ItemInfo item) {
                return element.isEmpty() || (item != null && item.getName().startsWith(element));
            }
        });
        return Collections2.transform(filtered, new Function<ItemInfo, String>() {
            @Nullable
            @Override
            public String apply(@Nullable ItemInfo input) {
                if (input != null) {
                    return input.isFolder() ? input.getRelPath() + "/" : input.getRelPath();
                } else {
                    return "";
                }
            }
        });

    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath(String value) {
        return value;
    }

    // [yluft] PathUtils clean up trailing slashes, etc, we need something simpler
    private String getFileName(String path) {
        int lastIndex = path.lastIndexOf("/");
        return lastIndex > -1 ? path.substring(lastIndex + 1) : path;
    }

    private String getFilePath(String path) {
        int lastIndex = path.lastIndexOf("/");
        return lastIndex > -1 ? path.substring(0, lastIndex) : "/";
    }
}
