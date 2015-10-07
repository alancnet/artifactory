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
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.storage.fs.service.ArchiveEntriesService;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author yoavl
 */
public class VfsMavenArchetypeArtifactInfoIndexCreator extends MavenArchetypeArtifactInfoIndexCreator {
    private static final Logger log = LoggerFactory.getLogger(VfsMavenArchetypeArtifactInfoIndexCreator.class);

    private static final String MAVEN_ARCHETYPE_PACKAGING = "maven-archetype";

    private static final String[] ARCHETYPE_XML_LOCATIONS =
            {"META-INF/maven/archetype.xml", "META-INF/archetype.xml", "META-INF/maven/archetype-metadata.xml"};

    @Override
    public void populateArtifactInfo(ArtifactContext ac) {
        JavaIOFileAdapter artifact = (JavaIOFileAdapter) ac.getArtifact();

        ArtifactInfo ai = ac.getArtifactInfo();

        // we need the file to perform these checks, and those may be only JARs
        if (artifact != null && !MAVEN_ARCHETYPE_PACKAGING.equals(ai.packaging)
                && artifact.getName().endsWith(".jar")) {
            // TODO: recheck, is the following true? "Maven plugins and Maven Archetypes can be only JARs?"

            // check for maven archetype, since Archetypes seems to not have consistent packaging,
            // and depending on the contents of the JAR, this call will override the packaging to "maven-archetype"!
            checkMavenArchetype(ai, artifact);
        }
    }

    /**
     * Archetypes that are added will have their packaging types set correctly (to maven-archetype)
     *
     * @param ai
     * @param artifact
     */
    private void checkMavenArchetype(ArtifactInfo ai, JavaIOFileAdapter artifact) {
        try {
            ArchiveEntriesService entriesService = ContextHelper.get().beanForType(ArchiveEntriesService.class);
            Set<ZipEntryInfo> archiveEntries = entriesService.getArchiveEntries(artifact.getFileInfo().getSha1());
            for (ZipEntryInfo archiveEntry : archiveEntries) {
                for (String location : ARCHETYPE_XML_LOCATIONS) {
                    if (location.equals(archiveEntry.getName())) {
                        ai.packaging = MAVEN_ARCHETYPE_PACKAGING;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Failed to parse Maven artifact " + artifact.getAbsolutePath(), e.getMessage());
            log.debug("Failed to parse Maven artifact " + artifact.getAbsolutePath(), e);
        }
    }
}