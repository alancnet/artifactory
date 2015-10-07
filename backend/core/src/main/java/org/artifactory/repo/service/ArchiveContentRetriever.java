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

package org.artifactory.repo.service;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.ArchiveFileContent;
import org.artifactory.common.ConstantValues;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.util.PathUtils;
import org.artifactory.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Retrieves a text content from an archive.
 *
 * @author Yossi Shaul
 */
public class ArchiveContentRetriever {
    private static final Logger log = LoggerFactory.getLogger(ArchiveContentRetriever.class);

    public ArchiveFileContent getArchiveFileContent(LocalRepo repo, RepoPath archivePath, String archiveEntryPath)
            throws IOException {
        String content = null;
        String sourceJarPath = null;
        List<String> searchList = null;
        String failureReason = null;
        ZipInputStream jis = null;
        String sourceEntryPath = null;
        try {
            if (archiveEntryPath.endsWith(".class")) {
                sourceEntryPath = NamingUtils.javaSourceNameFromClassName(archiveEntryPath);
                // locate the sources jar and find the source file in it
                String sourceJarName = PathUtils.stripExtension(archivePath.getName()) + "-sources." +
                        PathUtils.getExtension(archivePath.getName());
                String sourcesJarPath = archivePath.getParent().getPath() + "/" + sourceJarName;
                // search in the sources file first
                searchList = Lists.newArrayList(sourcesJarPath, archivePath.getPath());
            } else if (isTextFile(archiveEntryPath)) {
                // read directly from this archive
                searchList = Lists.newArrayList(archivePath.getPath());
                sourceEntryPath = archiveEntryPath;
            } else {
                failureReason = "View source for " + archiveEntryPath + " is not supported";
            }

            if (searchList != null) {
                boolean found = false;
                for (int i = 0; i < searchList.size() && !found; i++) {
                    String sourcesJarPath = searchList.get(i);
                    log.debug("Looking for {} source in {}", sourceEntryPath, sourceJarPath);
                    VfsFile sourceFile = repo.getImmutableFile(new RepoPathImpl(repo.getKey(), sourcesJarPath));
                    if (sourceFile == null) {
                        failureReason = "Source jar not found.";
                    } else if (!ContextHelper.get().getAuthorizationService()
                            .canRead(InternalRepoPathFactory.create(repo.getKey(), sourcesJarPath))) {
                        failureReason = "No read permissions for the source jar.";
                    } else {
                        List<String> alternativeExtensions = null;
                        if ("java".equalsIgnoreCase(PathUtils.getExtension(sourceEntryPath))) {
                            alternativeExtensions = Lists.newArrayList("groovy", "fx");
                        }

                        jis = new ZipInputStream(sourceFile.getStream());
                        ZipEntry zipEntry = ZipUtils.locateEntry(jis, sourceEntryPath, alternativeExtensions);
                        if (zipEntry == null) {
                            failureReason = "Source file not found.";
                        } else {
                            found = true;   // source entry was found in the jar
                            int maxAllowedSize = 1024 * 1024;
                            if (zipEntry.getSize() > maxAllowedSize) {
                                failureReason = String.format(
                                        "Source file is too big to render: file size: %s, max size: %s.",
                                        zipEntry.getSize(), maxAllowedSize);

                            } else {
                                // read the current entry (the source entry path)
                                content = IOUtils.toString(jis, "UTF-8");
                                sourceEntryPath = zipEntry.getName();
                                sourceJarPath = sourcesJarPath;
                            }
                        }
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(jis);
        }

        if (content != null) {
            return new ArchiveFileContent(content, InternalRepoPathFactory.create(repo.getKey(), sourceJarPath),
                    sourceEntryPath);
        } else {
            return ArchiveFileContent.contentNotFound(failureReason);
        }
    }

    public ArchiveFileContent getGenericArchiveFileContent(LocalRepo repo, RepoPath archivePath,
            String archiveEntryPath)
            throws IOException {
        String content = null;
        String sourceJarPath = null;
        List<String> searchList = null;
        String failureReason = null;
        ArchiveInputStream jis = null;
        String sourceEntryPath = null;
        try {
            if (archiveEntryPath.endsWith(".class")) {
                sourceEntryPath = NamingUtils.javaSourceNameFromClassName(archiveEntryPath);
                // locate the sources jar and find the source file in it
                String sourceJarName = PathUtils.stripExtension(archivePath.getName()) + "-sources." +
                        PathUtils.getExtension(archivePath.getName());
                String sourcesJarPath = archivePath.getParent().getPath() + "/" + sourceJarName;
                // search in the sources file first
                searchList = Lists.newArrayList(sourcesJarPath, archivePath.getPath());
            } else if (isTextFile(archiveEntryPath)) {
                // read directly from this archive
                searchList = Lists.newArrayList(archivePath.getPath());
                sourceEntryPath = archiveEntryPath;
            } else {
                failureReason = "View source for " + archiveEntryPath + " is not supported";
            }

            if (searchList != null) {
                boolean found = false;
                for (int i = 0; i < searchList.size() && !found; i++) {
                    String sourcesJarPath = searchList.get(i);
                    log.debug("Looking for {} source in {}", sourceEntryPath, sourceJarPath);
                    VfsFile sourceFile = repo.getImmutableFile(new RepoPathImpl(repo.getKey(), sourcesJarPath));
                    if (sourceFile == null) {
                        failureReason = "Source jar not found.";
                    } else if (!ContextHelper.get().getAuthorizationService()
                            .canRead(InternalRepoPathFactory.create(repo.getKey(), sourcesJarPath))) {
                        failureReason = "No read permissions for the source jar.";
                    } else {
                        List<String> alternativeExtensions = null;
                        if ("java".equalsIgnoreCase(PathUtils.getExtension(sourceEntryPath))) {
                            alternativeExtensions = Lists.newArrayList("groovy", "fx");
                        }

                        jis = ZipUtils.getArchiveInputStream(sourceFile);
                        ArchiveEntry zipEntry = ZipUtils.locateArchiveEntry(jis, sourceEntryPath,
                                alternativeExtensions);
                        if (zipEntry == null) {
                            failureReason = "Source file not found.";
                        } else {
                            found = true;   // source entry was found in the jar
                            int maxAllowedSize = 1024 * 1024;
                            if (zipEntry.getSize() > maxAllowedSize) {
                                failureReason = String.format(
                                        "Source file is too big to render: file size: %s, max size: %s.",
                                        zipEntry.getSize(), maxAllowedSize);

                            } else {
                                // read the current entry (the source entry path)
                                content = IOUtils.toString(jis, "UTF-8");
                                sourceEntryPath = zipEntry.getName();
                                sourceJarPath = sourcesJarPath;
                            }
                        }
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(jis);
        }

        if (content != null) {
            return new ArchiveFileContent(content, InternalRepoPathFactory.create(repo.getKey(), sourceJarPath),
                    sourceEntryPath);
        } else {
            return ArchiveFileContent.contentNotFound(failureReason);
        }
    }

    private boolean isTextFile(String fileName) {
        return NamingUtils.isViewable(fileName) || isLicenseFile(fileName);
    }

    private boolean isLicenseFile(String fileName) {
        String licenseFileNames = ConstantValues.archiveLicenseFileNames.getString();
        Set<String> possibleLicenseFileNames = Sets.newHashSet(
                Iterables.transform(Sets.newHashSet(StringUtils.split(licenseFileNames, ",")),
                        new Function<String, String>() {
                            @Override
                            public String apply(@Nullable String input) {
                                return StringUtils.isBlank(input) ? input : StringUtils.trim(input);
                            }
                        }
                )
        );
        return possibleLicenseFileNames.contains(PathUtils.getFileName(fileName));
    }
}
