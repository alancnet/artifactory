/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
/*
 * Additional contributors:
 *    JFrog Ltd.
 */

package org.artifactory.maven.index.creator;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.storage.fs.service.ArchiveEntriesService;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;

import java.io.IOException;
import java.util.Set;

/**
 * @author yoavl
 */
public class VfsJarFileContentsIndexCreator extends JarFileContentsIndexCreator {

    @Override
    public void populateArtifactInfo(ArtifactContext artifactContext) throws IOException {
        ArtifactInfo ai = artifactContext.getArtifactInfo();
        JavaIOFileAdapter artifactFile = (JavaIOFileAdapter) artifactContext.getArtifact();

        if (artifactFile != null && artifactFile.getName().endsWith(".jar")) {
            updateArtifactInfo(ai, artifactFile);
        }
    }

    private void updateArtifactInfo(ArtifactInfo ai, JavaIOFileAdapter file)
            throws IOException {
        ArchiveEntriesService entriesService = ContextHelper.get().beanForType(ArchiveEntriesService.class);
        Set<ZipEntryInfo> archiveEntries = entriesService.getArchiveEntries(file.getFileInfo().getSha1());
        StringBuilder sb = new StringBuilder();
        for (ZipEntryInfo e : archiveEntries) {
            String name = e.getName();
            if (name.endsWith(".class")) {
                // TODO verify if class is public or protected
                // TODO skip all inner classes for now

                int i = name.indexOf('$');

                if (i == -1) {
                    if (name.charAt(0) != '/') {
                        sb.append('/');
                    }

                    // class name without ".class"
                    sb.append(name.substring(0, name.length() - 6)).append('\n');
                }
            }
        }

        if (sb.toString().trim().length() != 0) {
            ai.classNames = sb.toString();
        } else {
            ai.classNames = null;
        }
    }
}
