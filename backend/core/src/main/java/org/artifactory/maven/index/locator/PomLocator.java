/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
/*
 * Additional contributors:
 *    JFrog Ltd.
 */

package org.artifactory.maven.index.locator;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.artifact.GavCalculator;
import org.apache.maven.index.locator.GavHelpedLocator;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;

import java.io.File;

/**
 * @author yoavl
 */
public class PomLocator implements GavHelpedLocator {

    @Override
    public File locate(File source, GavCalculator gavCalculator, Gav gav) {
        // build the pom name
        String artifactName = gav.getArtifactId() + "-" + gav.getVersion() + ".pom";
        // search sibling pom
        JavaIOFileAdapter file = (JavaIOFileAdapter) source;
        return file.getSibling(artifactName);
    }
}