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

package org.artifactory.repo;

import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * A factory for creating RepoPath objects.
 * <p/>
 * Has runtime dependency on the core.
 *
 * @author Yoav Landman
 */
public class RepoPathFactory {
    private static final Logger log = LoggerFactory.getLogger(RepoPathFactory.class);

    private static Constructor<?> ctor;

    static {
        try {
            Class<?> clazz =
                    RepoPathFactory.class.getClassLoader().loadClass("org.artifactory.model.common.RepoPathImpl");
            ctor = clazz.getConstructor(String.class, String.class);
        } catch (Exception e) {
            log.error("Error creating the repoPath factory.", e);
        }
    }

    /**
     * Constructs a RepoPath from the input repo key and optional path.
     * Paths that end with slash ('/') are considered as paths pointing to folder ({@link org.artifactory.repo.RepoPath#isFolder()} will return true)
     *
     * @param repoKey The key of any repo
     * @param path    The relative path inside the repo (empty for root repo path)
     */
    public static RepoPath create(String repoKey, String path) {
        try {
            return (RepoPath) ctor.newInstance(repoKey, path);
        } catch (Exception e) {
            throw new RuntimeException("Could create repoPath.", e);
        }
    }

    /**
     * Constructs a RepoPath from a path containing both repo key and the relative path in the repo.
     * Paths that end with slash ('/') are considered as paths pointing to folder ({@link org.artifactory.repo.RepoPath#isFolder()} will return true)
     *
     * @param rpp - {repoKey}/{itemRelativePath}
     * @return Matching repo path
     */
    public static RepoPath create(String rpp) {
        if (rpp == null || rpp.length() == 0) {
            throw new IllegalArgumentException("Path cannot be empty.");
        }
        rpp = PathUtils.trimLeadingSlashes(PathUtils.formatPath(rpp));
        int idx = rpp.indexOf('/');
        String repoKey;
        String path;
        if (idx < 0) {
            //Just a repo name with no rel path
            repoKey = rpp;
            path = "";
        } else {
            repoKey = rpp.substring(0, idx);
            path = rpp.substring(idx + 1);
        }
        return create(repoKey, path);
    }
}