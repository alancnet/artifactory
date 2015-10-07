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

package org.artifactory.maven.index;

import org.apache.lucene.store.FSDirectory;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactScanningListener;
import org.apache.maven.index.DefaultIndexer;
import org.apache.maven.index.DefaultIndexerEngine;
import org.apache.maven.index.DefaultQueryCreator;
import org.apache.maven.index.DefaultScannerListener;
import org.apache.maven.index.DefaultSearchEngine;
import org.apache.maven.index.ScanningRequest;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.incremental.DefaultIncrementalHandler;
import org.apache.maven.index.incremental.IncrementalHandler;
import org.apache.maven.index.packer.DefaultIndexPacker;
import org.apache.maven.index.packer.IndexPacker;
import org.apache.maven.index.packer.IndexPackingRequest;
import org.apache.maven.index.updater.DefaultIndexUpdater;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.TempFileStreamHandle;
import org.artifactory.maven.index.creator.VfsJarFileContentsIndexCreator;
import org.artifactory.maven.index.creator.VfsMavenArchetypeArtifactInfoIndexCreator;
import org.artifactory.maven.index.creator.VfsMavenPluginArtifactInfoIndexCreator;
import org.artifactory.maven.index.creator.VfsMinimalArtifactInfoIndexCreator;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.StoringRepo;
import org.artifactory.request.NullRequestContext;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.schedule.TaskInterruptedException;
import org.artifactory.schedule.TaskUtils;
import org.artifactory.storage.fs.tree.ItemTree;
import org.artifactory.storage.fs.tree.file.JavaIOFileAdapter;
import org.artifactory.util.Files;
import org.artifactory.util.Pair;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.util.FieldUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author yoavl
 * @author yossis
 */
public class RepoIndexer extends DefaultIndexer implements ArtifactScanningListener {
    private static final Logger log = LoggerFactory.getLogger(RepoIndexer.class);

    private final StoringRepo repo;
    private IndexingContext context;
    private IndexPacker packer;
    private final DefaultIndexerEngine defaultIndexerEngine;
    private final ArtifactoryContentScanner scanner;

    public RepoIndexer(StoringRepo repo) {
        if (repo == null) {
            throw new IllegalArgumentException("Repo cannot be null");
        }
        this.repo = repo;
        //Unplexus
        defaultIndexerEngine = new DefaultIndexerEngine();
        FieldUtils.setProtectedFieldValue("indexerEngine", this, defaultIndexerEngine);
        DefaultQueryCreator queryCreator = new DefaultQueryCreator();
        FieldUtils.setProtectedFieldValue("logger", queryCreator,
                new ConsoleLogger(org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console"));
        FieldUtils.setProtectedFieldValue("queryCreator", this, queryCreator);
        FieldUtils.setProtectedFieldValue("searcher", this, new DefaultSearchEngine());
        //packer
        IncrementalHandler incrementalHandler = new DefaultIncrementalHandler();
        FieldUtils.setProtectedFieldValue("logger", incrementalHandler,
                new ConsoleLogger(org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console"));
        packer = new DefaultIndexPacker();
        FieldUtils.setProtectedFieldValue("incrementalHandler", packer, incrementalHandler);
        FieldUtils.setProtectedFieldValue("logger", packer,
                new ConsoleLogger(org.codehaus.plexus.logging.Logger.LEVEL_WARN, "console"));

        ArtifactoryArtifactContextProducer artifactContextProducer = new ArtifactoryArtifactContextProducer();
        scanner = new ArtifactoryContentScanner(artifactContextProducer);
    }

    @Override
    public void scanningStarted(IndexingContext ctx) {
    }

    @Override
    public void scanningFinished(IndexingContext ctx, ScanningResult result) {
    }

    @Override
    public void artifactError(ArtifactContext ac, Exception e) {
    }

    @Override
    public void artifactDiscovered(ArtifactContext ac) {
        if (log.isTraceEnabled()) {
            log.trace("Artifact discovered: '{}'", ac.getArtifactInfo().getUinfo());
        }
        if (TaskUtils.pauseOrBreak()) {
            throw new TaskInterruptedException();
        }
        //Be nice with other threads
        Thread.yield();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    Pair<TempFileStreamHandle, TempFileStreamHandle> index(Date fireTime) throws Exception {
        //Use a file based dir with a temp file to conserve memory
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        // TODO: Should use the temp file of the repo
        File dir = Files.createRandomDir(artifactoryHome.getTempWorkDir(), "artifactory.index." + repo.getKey());
        try {
            createContext(dir);
            return createIndex(dir, true);
        } catch (Exception e) {
            throw new RuntimeException("Indexing failed.", e);
        } finally {
            //Remove the temp index dir and files
            removeTempIndexFiles(dir);
        }
    }

    Pair<TempFileStreamHandle, TempFileStreamHandle> createIndex(File indexDir, boolean scan) throws IOException {
        try {
            context.updateTimestamp();
            if (scan) {
                //Update the dir content by scanning the repo
                scanner.scan(new ScanningRequest(context,
                        new DefaultScannerListener(context, defaultIndexerEngine, true, this), null));
            }

            ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
            File outputFolder = Files.createRandomDir(artifactoryHome.getTempWorkDir(),
                    "artifactory.index." + repo.getKey());
            outputFolder.deleteOnExit();
            IndexPackingRequest request = newIndexPackingRequest(outputFolder);
            //Pack - will create the index files inside the folder
            packer.packIndex(request);
            //Return the handle to the zip file (will be remove when the handle is closed)
            File tmpGz = new File(outputFolder, MavenNaming.NEXUS_INDEX_GZ);
            if (!tmpGz.exists()) {
                throw new RuntimeException("Temp index file '" + tmpGz.getAbsolutePath() + "' does not exist.");
            }
            File propertiesFile = new File(outputFolder, MavenNaming.NEXUS_INDEX_PROPERTIES);
            if (!propertiesFile.exists()) {
                throw new RuntimeException("Temp properties file '" + tmpGz.getAbsolutePath() + "' does not exist.");
            }
            TempFileStreamHandle zipIndexHandle = new TempFileStreamHandle(tmpGz);
            TempFileStreamHandle propertiesHandle = new TempFileStreamHandle(propertiesFile);
            return new Pair<>(zipIndexHandle, propertiesHandle);
        } catch (Exception e) {
            throw new RuntimeException("Index creation failed.", e);
        }
    }

    void mergeInto(StoringRepo localRepo, Map<StoringRepo, FSDirectory> extractedRepoIndexes) throws Exception {
        FSDirectory repoToMergeIndexDir = getIndexDir(localRepo, extractedRepoIndexes);
        if (repoToMergeIndexDir == null) {
            //No local index exists
            return;
        }
        //Merge the provided index into the repo-specific temp index dir
        try {
            log.debug("Merging local index of {} into {}.", localRepo, repo);
            context.merge(repoToMergeIndexDir);
            /*IndexWriter indexWriter = context.getIndexWriter();
            indexWriter.addIndexes(new Directory[]{indexDir});
            indexWriter.close();*/
        } catch (FileNotFoundException e) {
            //Merged-into directory is new - just copy instead of merging
            log.debug("Target index directory is new: merging is skipped.");
        }
    }

    StoringRepo getRepo() {
        return repo;
    }

    void removeTempIndexFiles(File dir) {
        if (dir != null) {
            /**
             * Remove indexing context and delete the created files in a proper manner
             */
            try {
                closeIndexingContext(context, true);
            } catch (Exception e) {
                log.warn("Could not remove temporary index context '{}'.", context);
            }
            /**
             * We have to delete the index dir ourselves because the nexus removal
             *  tool deletes the files, but leaves the dir.
             */
            org.apache.commons.io.FileUtils.deleteQuietly(dir);
        }
    }

    void createContext(File indexDir) throws IOException {
        String repoKey = repo.getKey();
        List<IndexCreator> indexCreators = new ArrayList<>(4);
        indexCreators.add(new VfsMinimalArtifactInfoIndexCreator());
        indexCreators.add(new VfsJarFileContentsIndexCreator());
        indexCreators.add(new VfsMavenPluginArtifactInfoIndexCreator());
        indexCreators.add(new VfsMavenArchetypeArtifactInfoIndexCreator());
        ItemTree itemTree = new ItemTree(repo.getRepoPath(""));
        JavaIOFileAdapter rootFile = new JavaIOFileAdapter(itemTree.getRootNode());
        context = createIndexingContext(repoKey, repoKey, rootFile, indexDir, null, null, true, true, indexCreators);
    }

    private IndexPackingRequest newIndexPackingRequest(File outputFolder) {
        IndexPackingRequest request = new IndexPackingRequest(context, outputFolder);
        request.setCreateChecksumFiles(false);
        request.setCreateIncrementalChunks(false);
        //create new index format
        request.setFormats(Arrays.asList(/*IndexPackingRequest.IndexFormat.FORMAT_LEGACY,*/
                IndexPackingRequest.IndexFormat.FORMAT_V1));
        return request;
    }

    private FSDirectory getIndexDir(StoringRepo repo, Map<StoringRepo, FSDirectory> extractedRepoIndexes)
            throws Exception {
        //Check if need to extract the local index first
        FSDirectory indexDir = extractedRepoIndexes.get(repo);
        if (indexDir == null) {
            //Extraction required
            NullRequestContext requestContext =
                    new NullRequestContext(repo.getRepoPath(MavenNaming.NEXUS_INDEX_GZ_PATH));
            RepoResource indexRes = repo.getInfo(requestContext);
            if (!indexRes.isFound()) {
                log.debug("Cannot find index resource for repository {}", repo);
                return null;
            }
            //Copy the index file
            try (ResourceStreamHandle handle = repo.getResourceStreamHandle(requestContext, indexRes)) {
                ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
                File indexUnzippedDir = Files.createRandomDir(artifactoryHome.getTempWorkDir(),
                        "artifactory.merged-index." + repo.getKey());
                indexUnzippedDir.deleteOnExit();
                indexDir = FSDirectory.open(indexUnzippedDir);
                //Get the extracted lucene dir
                DefaultIndexUpdater.unpackIndexData(handle.getInputStream(), indexDir, context);
            }
            //Remember the extracted index
            extractedRepoIndexes.put(repo, indexDir);
        }
        return indexDir;
    }
}