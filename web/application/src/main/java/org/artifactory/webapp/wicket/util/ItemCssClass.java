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

package org.artifactory.webapp.wicket.util;

import com.google.common.collect.ImmutableMap;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;

import javax.annotation.Nonnull;

/**
 * Enum of the files, folders and repositories CSS classes. Use cssClass() for the name of the css class.
 *
 * @author Yossi Shaul
 */
public enum ItemCssClass {
    doc,
    folder, folderCompact("folder-compact"),
    jar, pom, xml, jnlp, parent, nupkg, apk, rpm, gem, deb, box, lfs,
    repository, repositoryCache("repository-cache"),
    repositoryVirtual("repository-virtual"), root, treeSearch("tree-search");

    private static final ImmutableMap<String, ItemCssClass> cssClassByName;

    static {
        ImmutableMap.Builder<String, ItemCssClass> builder = ImmutableMap.builder();
        for (ItemCssClass cssClass : ItemCssClass.values()) {
            builder.put(cssClass.cssClass, cssClass);
        }
        cssClassByName = builder.build();
    }


    private String cssClass;

    /**
     * By default the css class name is the enum name.
     */
    ItemCssClass() {
        this.cssClass = name();
    }

    ItemCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    /**
     * @return String representing the css class for this enum
     */
    public String getCssClass() {
        return cssClass;
    }

    public static String getRepoCssClass(Object repo) {
        if (repo instanceof String) {
            return getRepoKeyCssClass(repo);
        }

        return getRepoDescriptorCssClass(repo);
    }

    public static String getRepoKeyCssClass(Object repo) {
        String repoKey = (String) repo;
        RepositoryService repositoryService = ArtifactoryApplication.get().getRepositoryService();
        RepoDescriptor descriptor = repositoryService.virtualRepoDescriptorByKey(repoKey);
        if (descriptor != null) {
            return ItemCssClass.repositoryVirtual.getCssClass();
        }
        descriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        if (descriptor != null) {
            return getRepoDescriptorCssClass(descriptor);
        }
        return ItemCssClass.repository.getCssClass();
    }

    public static String getRepoDescriptorCssClass(Object descriptor) {
        if (descriptor instanceof VirtualRepoDescriptor) {
            return ItemCssClass.repositoryVirtual.getCssClass();
        }

        if (descriptor instanceof RemoteRepoDescriptor) {
            return ItemCssClass.repositoryCache.getCssClass();
        }

        if (descriptor instanceof RealRepoDescriptor && ((RealRepoDescriptor) descriptor).isCache()) {
            return ItemCssClass.repositoryCache.getCssClass();
        }

        return ItemCssClass.repository.getCssClass();
    }

    /**
     * @param path The file path
     * @return The matching css class for the give file path. If there is no special css class for the given path, the
     *         generic 'doc' class will be returned.
     */
    @Nonnull
    public static ItemCssClass getFileCssClass(String path) {
        ItemCssClass cssClass;
        if (path.endsWith(BaseBrowsableItem.UP)) {
            cssClass = parent;
        } else {
            MimeType ct = NamingUtils.getMimeType(path);
            cssClass = cssClassByName.get(ct.getCss());
        }

        if (cssClass == null) {
            cssClass = doc;
        }
        return cssClass;
    }
}
