/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * This class is based on: org.apache.maven.index.DefaultScanner
 */
package org.artifactory.maven.index;

import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.ScanningRequest;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;
import org.artifactory.util.Files;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A repository scanner to scan the content of a single repository.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryContentScanner extends AbstractLogEnabled implements Scanner {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryContentScanner.class);

    private ArtifactContextProducer artifactContextProducer;

    public ArtifactoryContentScanner(ArtifactoryArtifactContextProducer artifactContextProducer) {
        this.artifactContextProducer = artifactContextProducer;
    }

    @Override
    public ScanningResult scan(ScanningRequest request) {
        request.getArtifactScanningListener().scanningStarted(request.getIndexingContext());

        ScanningResult result = new ScanningResult(request);

        scanDirectory(request.getStartingDirectory(), request);

        request.getArtifactScanningListener().scanningFinished(request.getIndexingContext(), result);

        return result;
    }

    private void scanDirectory(File dir, ScanningRequest request) {
        if (dir == null) {
            return;
        }

        File[] fileArray = dir.listFiles();

        if (fileArray == null) {
            log.debug("Unexpected null file list returned from {}: {}", dir.getAbsolutePath(),
                    Files.readFailReason(dir));
            return;
        }

        Set<File> files = new TreeSet<>(new ScannerFileComparator());

        files.addAll(Arrays.asList(fileArray));

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, request);
            } else {
                processFile(file, request);
            }
        }
    }

    private void processFile(File file, ScanningRequest request) {
        try {
            if (!file.getName().startsWith(".")) {
                IndexingContext context = request.getIndexingContext();
                ArtifactContext ac = artifactContextProducer.getArtifactContext(context, file);

                if (ac != null) {
                    request.getArtifactScanningListener().artifactDiscovered(ac);
                }
            }
        } catch (Throwable t) {
            log.info("Failed to add {} to the maven index: {}", file.getAbsolutePath(), t.getMessage());
            log.debug("Failed to add file to the maven index", t);
        }
    }

    /**
     * A special comparator to overcome some very bad limitations of nexus-indexer during scanning: using this
     * comparator, we force to "discover" POMs last, before the actual artifact file. The reason for this, is to
     * guarantee that scanner will provide only "best" informations 1st about same artifact, since the POM->artifact
     * direction of discovery is not trivial at all (pom read -> packaging -> extension -> artifact file). The artifact
     * -> POM direction is trivial.
     */
    private static class ScannerFileComparator
            implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            if (o1.getName().endsWith(".pom") && !o2.getName().endsWith(".pom")) {
                // 1st is pom, 2nd is not
                return 1;
            } else if (!o1.getName().endsWith(".pom") && o2.getName().endsWith(".pom")) {
                // 2nd is pom, 1st is not
                return -1;
            } else {
                // both are "same" (pom or not pom)
                // Use reverse order so that timestamped snapshots
                // use latest - not first
                return o2.getName().compareTo(o1.getName());

            }
        }
    }
}
