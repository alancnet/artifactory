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

package org.artifactory.repo.snapshot;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.api.module.regex.NamedPattern;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.StoringRepo;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemNodeFilter;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.util.RepoLayoutUtils;

import java.util.Calendar;

/**
 * Base class for collecting versions items under a given root node.
 *
 * @author Shay Yaakov
 */
public abstract class VersionsRetriever {

    protected TreeMultimap<Calendar, ItemInfo> versionsItems;

    public VersionsRetriever(boolean reverseOrderResults) {
        if (reverseOrderResults) {
            versionsItems = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural().reverse());
        } else {
            versionsItems = TreeMultimap.create(Ordering.natural(), Ordering.natural());
        }
    }

    /**
     * Collects versions items under the given node for the given repo
     *
     * @param repo                 The repo to search in
     * @param baseRevisionModule   Base module info to search under, we try both artifact and desriptor path if it's distinctive
     * @param pathHasVersionTokens If we should search with version tokens, this applies for release artifacts as the user
     *                             may provide release/integration tokens to search for latest version
     */
    public TreeMultimap<Calendar, ItemInfo> collectVersionsItems(StoringRepo repo, ModuleInfo baseRevisionModule,
            boolean pathHasVersionTokens) {
        RepoLayout repoLayout = repo.getDescriptor().getRepoLayout();
        String baseArtifactPath = ModuleInfoUtils.constructArtifactPath(baseRevisionModule, repoLayout, false);
        ItemNode artifactSearchNode = getTreeNode(repo, repoLayout, baseArtifactPath, pathHasVersionTokens);
        if (artifactSearchNode != null) {
            internalCollectVersionsItems(repo, artifactSearchNode);
        }

        if (repoLayout.isDistinctiveDescriptorPathPattern()) {
            String baseDescriptorPath = ModuleInfoUtils.constructDescriptorPath(baseRevisionModule, repoLayout, false);
            if (!baseDescriptorPath.equals(baseArtifactPath)) {
                ItemNode descriptorSearchNode = getTreeNode(repo, repoLayout, baseDescriptorPath, pathHasVersionTokens);
                if (descriptorSearchNode != null) {
                    internalCollectVersionsItems(repo, descriptorSearchNode);
                }
            }
        }

        return versionsItems;
    }

    private ItemNode getTreeNode(StoringRepo repo, RepoLayout repoLayout, String itemPath,
            boolean pathHasVersionTokens) {
        RepoPath searchBasePath = getBaseRepoPathFromPartialItemPath(repo.getKey(), itemPath);
        String regEx = RepoLayoutUtils.generateRegExpFromPattern(repoLayout, itemPath, false, pathHasVersionTokens);
        NamedPattern pattern = NamedPattern.compile(regEx);
        ItemNodeFilter fileFilter = getFileFilter(repo, pattern);

        ItemTree itemTree = new ItemTree(searchBasePath, fileFilter);
        return itemTree.buildTree();
    }

    private RepoPath getBaseRepoPathFromPartialItemPath(String repoKey, String itemPath) {
        StringBuilder searchBasePathBuilder = new StringBuilder();
        String[] pathTokens = itemPath.split("/");
        for (String pathToken : pathTokens) {
            if (!pathToken.contains("[") && !pathToken.contains("(") && !pathToken.contains("{")) {
                searchBasePathBuilder.append(pathToken).append("/");
            } else {
                break;
            }
        }
        return InternalRepoPathFactory.create(repoKey, searchBasePathBuilder.toString());
    }

    protected abstract void internalCollectVersionsItems(StoringRepo repo, ItemNode node);

    protected abstract ItemNodeFilter getFileFilter(StoringRepo repo, NamedPattern pattern);
}
