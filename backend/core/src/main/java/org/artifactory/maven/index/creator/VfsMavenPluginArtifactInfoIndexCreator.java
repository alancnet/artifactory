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

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author yoavl
 */
public class VfsMavenPluginArtifactInfoIndexCreator extends MavenPluginArtifactInfoIndexCreator {
    private static final Logger log = LoggerFactory.getLogger(VfsMavenPluginArtifactInfoIndexCreator.class);

    private static final String MAVEN_PLUGIN_PACKAGING = "maven-plugin";

    @Override
    public void populateArtifactInfo(ArtifactContext ac) {
        File artifact = ac.getArtifact();

        ArtifactInfo ai = ac.getArtifactInfo();

        // we need the file to perform these checks, and those may be only JARs
        if (artifact != null && MAVEN_PLUGIN_PACKAGING.equals(ai.packaging) && artifact.getName().endsWith(".jar")) {
            // TODO: recheck, is the following true? "Maven plugins and Maven Archetypes can be only JARs?"

            // 1st, check for maven plugin
            checkMavenPlugin(ai, (JavaIOFileAdapter) artifact);
        }
    }

    private void checkMavenPlugin(ArtifactInfo ai, JavaIOFileAdapter artifact) {
        ZipInputStream zis = null;
        InputStream is = null;
        try {
            is = artifact.getStream();
            zis = new ZipInputStream(is);
            ZipEntry currentEntry;
            while ((currentEntry = zis.getNextEntry()) != null) {
                if (currentEntry.getName().equals("META-INF/maven/plugin.xml")) {
                    parsePluginDetails(ai, zis);
                    break;
                }
            }
        } catch (Exception e) {
            log.info("Failed to parsing Maven plugin " + artifact.getAbsolutePath(), e.getMessage());
            log.debug("Failed to parsing Maven plugin " + artifact.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(zis);
        }
    }

    private void parsePluginDetails(ArtifactInfo ai, ZipInputStream zis)
            throws XmlPullParserException, IOException, PlexusConfigurationException {
        PlexusConfiguration plexusConfig =
                new XmlPlexusConfiguration(Xpp3DomBuilder.build(new InputStreamReader(zis, Charsets.UTF_8)));

        ai.prefix = plexusConfig.getChild("goalPrefix").getValue();
        ai.goals = new ArrayList<>();
        PlexusConfiguration[] mojoConfigs = plexusConfig.getChild("mojos").getChildren("mojo");
        for (PlexusConfiguration mojoConfig : mojoConfigs) {
            ai.goals.add(mojoConfig.getChild("goal").getValue());
        }
    }
}