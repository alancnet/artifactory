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

package org.artifactory.maven.index.locator;

import org.apache.commons.io.IOUtils;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.GavCalculator;
import org.apache.maven.index.locator.GavHelpedLocator;
import org.apache.maven.model.Model;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Locates the main artifact based on the data from the pom file.
 *
 * @author Yossi Shaul
 */
public class MainArtifactLocator implements GavHelpedLocator {
    private static final Logger log = LoggerFactory.getLogger(MainArtifactLocator.class);

    private final ArtifactPackagingMapper mapper;

    public MainArtifactLocator(ArtifactPackagingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public File locate(File source, GavCalculator gavCalculator, Gav gav) {
        // if we don't have this data, nothing we can do
        if (source == null || !source.exists() || gav == null || gav.getArtifactId() == null
                || gav.getVersion() == null) {
            return null;
        }

        InputStream pomInputStream = null;
        try {
            // need to read the pom model to get packaging
            JavaIOFileAdapter sourceAdapter = (JavaIOFileAdapter) source;
            pomInputStream = new BufferedInputStream(sourceAdapter.getStream());
            Model model = new ArtifactContext.ModelReader().readModel(pomInputStream);

            if (model == null) {
                return null;
            }

            // now generate the artifact name
            String artifactName = gav.getArtifactId() + "-" + gav.getVersion() + "."
                    + mapper.getExtensionForPackaging(model.getPackaging());

            RepoPathImpl artifactRepoPath = new RepoPathImpl(sourceAdapter.getRepoPath().getParent(), artifactName);
            ItemTree itemTree = new ItemTree(artifactRepoPath);
            ItemNode mainArtifactNode = itemTree.getRootNode();
            if (mainArtifactNode == null) {
                return null;
            } else {
                return new JavaIOFileAdapter(mainArtifactNode);
            }
        } catch (RepositoryRuntimeException e) {
            log.debug("Failed to read locate main artifact", e);
            return null;
        } finally {
            IOUtils.closeQuietly(pomInputStream);
        }
    }

}
