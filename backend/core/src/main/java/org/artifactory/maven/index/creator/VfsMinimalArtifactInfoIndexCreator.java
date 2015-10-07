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

import org.apache.maven.index.ArtifactAvailablility;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.artifact.Gav;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.locator.Locator;
import org.apache.maven.model.Model;
import org.artifactory.maven.index.locator.ExtensionBasedLocator;
import org.artifactory.schedule.TaskService;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VfsMinimalArtifactInfoIndexCreator extends MinimalArtifactInfoIndexCreator {
    private static final Logger log = LoggerFactory.getLogger(VfsMinimalArtifactInfoIndexCreator.class);

    private final Locator jl;
    private final Locator sl;
    private final Locator sigl;

    public VfsMinimalArtifactInfoIndexCreator() {
        this.jl = new ExtensionBasedLocator("-javadoc.jar");
        this.sl = new ExtensionBasedLocator("-sources.jar");
        this.sigl = new ExtensionBasedLocator(".jar.asc");
    }

    @SuppressWarnings({"OverlyComplexMethod"})
    @Override
    public void populateArtifactInfo(ArtifactContext ac) {
        //Test whether the indexer needs to be stopped or paused
        //Check if we need to stop/suspend
        TaskService taskService = InternalContextHelper.get().getTaskService();
        boolean stop = taskService.pauseOrBreak();
        if (stop) {
            return;
        }
        //Populating the artifact info
        File artifact = ac.getArtifact();
        File pom = ac.getPom();
        ArtifactInfo ai = ac.getArtifactInfo();

        if (pom != null) {
            ai.lastModified = pom.lastModified();

            ai.fextension = "pom";
        }

        // TODO handle artifacts without poms
        if (pom != null) {
            if (ai.classifier != null) {
                ai.sourcesExists = ArtifactAvailablility.NOT_AVAILABLE;

                ai.javadocExists = ArtifactAvailablility.NOT_AVAILABLE;
            } else {
                File sources = sl.locate(pom);
                if (!sources.exists()) {
                    ai.sourcesExists = ArtifactAvailablility.NOT_PRESENT;
                } else {
                    ai.sourcesExists = ArtifactAvailablility.PRESENT;
                }

                File javadoc = jl.locate(pom);
                if (!javadoc.exists()) {
                    ai.javadocExists = ArtifactAvailablility.NOT_PRESENT;
                } else {
                    ai.javadocExists = ArtifactAvailablility.PRESENT;
                }
            }
        }

        if (artifact != null) {
            File signature = sigl.locate(artifact);

            ai.signatureExists = signature.exists() ? ArtifactAvailablility.PRESENT : ArtifactAvailablility.NOT_PRESENT;

            try {
                String sha1 = ((JavaIOFileAdapter) artifact).getFileInfo().getSha1();
                if (sha1 != null) {
                    ai.sha1 = StringUtils.chomp(sha1).trim().split(" ")[0];
                }
            } catch (Exception e) {
                ac.addError(e);
                if (log.isDebugEnabled()) {
                    log.debug("Could not retrieve artifact checksum.", e);
                } else {
                    String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    log.warn("Could not retrieve artifact checksum" + (msg != null ? ": " + msg : "") + ".");
                }
            }

            ai.lastModified = artifact.lastModified();

            ai.size = artifact.length();

            ai.fextension = getExtension(artifact, ac.getGav());

            if (ai.packaging == null) {
                ai.packaging = ai.fextension;
            }
        }

        Model model = ac.getPomModel();

        if (model != null) {
            ai.name = model.getName();

            ai.description = model.getDescription();

            if (model.getPackaging() != null && ai.classifier == null) {
                // only when this is not a classified artifact
                ai.packaging = model.getPackaging();
            }
        }
    }

    private String getExtension(File artifact, Gav gav) {
        if (gav != null && StringUtils.isNotBlank(gav.getExtension())) {
            return gav.getExtension();
        }

        // last resort, the extension of the file
        String artifactFileName = artifact.getName().toLowerCase();

        // tar.gz? and other "special" combinations
        if (artifactFileName.endsWith("tar.gz")) {
            return "tar.gz";
        } else if (artifactFileName.equals("tar.bz2")) {
            return "tar.bz2";
        }

        // get the part after the last dot
        return FileUtils.getExtension(artifactFileName);
    }

}