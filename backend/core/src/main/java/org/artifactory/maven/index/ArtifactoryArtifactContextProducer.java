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

package org.artifactory.maven.index;

import org.apache.commons.io.IOUtils;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.locator.GavHelpedLocator;
import org.apache.maven.index.locator.Locator;
import org.apache.maven.model.Model;
import org.artifactory.maven.index.locator.MainArtifactLocator;
import org.artifactory.maven.index.locator.MetadataLocator;
import org.artifactory.maven.index.locator.PomLocator;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * @author Yossi Shaul
 */
public class ArtifactoryArtifactContextProducer implements ArtifactContextProducer {

    private final ArtifactPackagingMapper mapper = new SimpleArtifactPackagingMapper();
    private GavHelpedLocator pl = new PomLocator();
    private Locator ml = new MetadataLocator();

    /**
     * Get ArtifactContext for given pom or artifact (jar, war, etc). A file can be
     */
    @Override
    public ArtifactContext getArtifactContext(IndexingContext context, File file) {
        String repositoryPath = context.getRepository().getAbsolutePath();
        String artifactPath = file.getAbsolutePath();

        // protection from IndexOutOfBounds
        if (artifactPath.length() <= repositoryPath.length()) {
            return null; // not an artifact
        }

        if (!isIndexable(file)) {
            return null; // skipped
        }

        Gav gav = getGavFromPath(context, repositoryPath, artifactPath);

        if (gav == null) {
            return null; // not an artifact
        }

        File pom;
        File artifact;

        if (file.getName().endsWith(".pom")) {
            MainArtifactLocator al = new MainArtifactLocator(mapper);
            artifact = al.locate(file, context.getGavCalculator(), gav);

            // If we found the matching artifact, switch over to indexing that, instead of the pom
            if (artifact != null) {
                gav = getGavFromPath(context, repositoryPath, artifact.getAbsolutePath());
            }

            pom = file;
        } else {
            artifact = file;
            pom = pl.locate(file, context.getGavCalculator(), gav);
        }

        String groupId = gav.getGroupId();

        String artifactId = gav.getArtifactId();

        String version = gav.getBaseVersion();

        String classifier = gav.getClassifier();

        ArtifactInfo ai = new ArtifactInfo(context.getRepositoryId(), groupId, artifactId, version, classifier);

        // store extension if classifier is not empty
        if (!StringUtils.isEmpty(ai.classifier)) {
            ai.packaging = gav.getExtension();
        }

        // TODO: do it only for main package
        if (pom != null && pom.exists() && ai.classifier == null) {
            InputStream pomInputStream = null;
            try {
                // need to read the pom model to get packaging
                pomInputStream = new BufferedInputStream(((JavaIOFileAdapter) pom).getStream());
                Model model = new ArtifactContext.ModelReader().readModel(pomInputStream);
                if (model != null) {
                    if (model.getPackaging() != null) {
                        // only when this is not a classified artifact
                        ai.packaging = model.getPackaging();
                    }
                }
            } catch (RepositoryRuntimeException e) {
                // ignore
            } finally {
                IOUtils.closeQuietly(pomInputStream);
            }
        }

        ai.fname = file.getName();
        ai.fextension = gav.getExtension();

        File metadata = ml.locate(pom);

        return new ArtifactContext(pom, artifact, metadata, ai, gav);
    }

    private boolean isIndexable(File file) {
        if (file == null) {
            return false;
        }

        String filename = file.getName();

        if (filename.equals("maven-metadata.xml")
                // || filename.endsWith( "-javadoc.jar" )
                // || filename.endsWith( "-javadocs.jar" )
                // || filename.endsWith( "-sources.jar" )
                || filename.endsWith(".properties")
                // || filename.endsWith( ".xml" ) // NEXUS-3029
                || filename.endsWith(".asc") || filename.endsWith(".md5") || filename.endsWith(".sha1")) {
            return false;
        }

        return true;
    }

    private Gav getGavFromPath(IndexingContext context, String repositoryPath, String artifactPath) {
        String path = artifactPath.substring(repositoryPath.length() + 1).replace('\\', '/');

        return context.getGavCalculator().pathToGav(path);
    }

}
