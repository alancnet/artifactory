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
import com.google.common.collect.Collections2;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.artifactory.api.config.RepositoryImportSettingsImpl;
import org.artifactory.backup.BackupJob;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.maven.index.MavenIndexerJob;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.cleanup.ArtifactCleanupJob;
import org.artifactory.repo.cleanup.IntegrationCleanupJob;
import org.artifactory.repo.db.importexport.DbRepoImportHandler;
import org.artifactory.repo.replication.LocalReplicationJob;
import org.artifactory.repo.replication.RemoteReplicationJob;
import org.artifactory.schedule.CachedThreadPoolTaskExecutor;
import org.artifactory.schedule.JobCommand;
import org.artifactory.schedule.StopCommand;
import org.artifactory.schedule.StopStrategy;
import org.artifactory.schedule.Task;
import org.artifactory.schedule.TaskCallback;
import org.artifactory.schedule.TaskUser;
import org.artifactory.schedule.quartz.QuartzCommand;
import org.artifactory.search.archive.ArchiveIndexerImpl;
import org.artifactory.spring.InternalContextHelper;
import org.artifactory.storage.binstore.service.BinaryStoreGarbageCollectorJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static org.artifactory.schedule.StopStrategy.STOP;

/**
 * This job triggers the actual import process of either single or multiple repositories.
 *
 * @author freds
 * @author Yoav Luft
 */
@JobCommand(manualUser = TaskUser.CURRENT,
        keyAttributes = {Task.REPO_KEY},
        commandsToStop = {
                @StopCommand(command = BinaryStoreGarbageCollectorJob.class, strategy = StopStrategy.IMPOSSIBLE),
                @StopCommand(command = ExportJob.class, strategy = StopStrategy.IMPOSSIBLE),
                @StopCommand(command = BackupJob.class, strategy = StopStrategy.IMPOSSIBLE),
                @StopCommand(command = MavenIndexerJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = ArtifactCleanupJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = IntegrationCleanupJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = LocalReplicationJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = RemoteReplicationJob.class, strategy = StopStrategy.STOP),
                @StopCommand(command = ArchiveIndexerImpl.ArchiveIndexJob.class, strategy = STOP)},
        runOnlyOnPrimary = false)
public class ImportJob extends QuartzCommand {
    private static final Logger log = LoggerFactory.getLogger(ImportJob.class);

    private RepositoryImportSettingsImpl importSettings;
    private MutableStatusHolder statusHolder;
    private InternalRepositoryService repositoryService;
    private ImportHandlerCallablesFactory callablesFactory;
    private final Semaphore parallelImportsGate;
    private AsyncTaskExecutor taskExecutor;

    public ImportJob() {
        int maxParallelImports = Math.max(ConstantValues.importMaxParallelRepos.getInt(), 1);
        parallelImportsGate = new Semaphore(maxParallelImports);
        callablesFactory = new ImportHandlerCallablesFactory(parallelImportsGate, TaskCallback.currentTaskToken());
        taskExecutor = new CachedThreadPoolTaskExecutor();
        repositoryService = InternalContextHelper.get().beanForType(InternalRepositoryService.class);
    }

    /**
     * Constructor for use by tests
     */
    ImportJob(AsyncTaskExecutor executor, ImportHandlerCallablesFactory callablesFactory, Semaphore gate,
            InternalRepositoryService repositoryService) {
        parallelImportsGate = gate;
        this.callablesFactory = callablesFactory;
        this.taskExecutor = executor;
        this.repositoryService = repositoryService;
    }

    @Override
    protected void onExecute(JobExecutionContext callbackContext) {
        try {
            initializeFromContext(callbackContext);
            deleteRepositories();
            List<Callable<DbRepoImportHandler>> importers = prepareImportHandlerCallables();
            List<Future<DbRepoImportHandler>> handlerFutures = new ArrayList<>(importers.size());
            for (Callable<DbRepoImportHandler> importer : importers) {
                // we acquire an import permit here and each importer is responsible to release it when it ends
                parallelImportsGate.acquire();
                handlerFutures.add(taskExecutor.submit(importer));
            }
            log.debug("All import threads were submitted");
            for (Future<DbRepoImportHandler> handlerFuture : handlerFutures) {
                // serially call finalize for each repo which will trigger another retry phase if required
                // this has to be serial to avoid further concurrent import conflicts
                finalizeImport(handlerFuture);
            }
            log.info("Import of {} repositories completed", importSettings.getRepositories().size());
            checkForUnusedSubdirectories();
        } catch (Exception e) {
            if (statusHolder != null) {
                statusHolder.error("Error occurred during import: " + e.getMessage(), e, log);
            } else {
                log.error("Error occurred during import", e);
            }
        }
    }

    private void finalizeImport(Future<DbRepoImportHandler> handlerFuture) {
        try {
            DbRepoImportHandler importHandler = handlerFuture.get();
            importHandler.finalizeImport();
        } catch (InterruptedException e) {
            log.info("Import was interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            log.error("Import had failed due to {}: {}", cause.getClass().getName(), cause.getMessage());
            log.debug("Import had failed due to {}: {}",
                    cause.getClass().getName(), cause.getMessage(), cause);
            if (statusHolder != null) {
                statusHolder.error("Error occurred during import: " + cause.getMessage(), cause, log);
            }
        } catch (RuntimeException e) {
            // Extract cause
            if (statusHolder != null) {
                statusHolder.error("Error occurred during import: " + e.getMessage(), e, log);
            } else {
                log.error("Error occurred during import", e);
            }
        }
    }

    private List<Callable<DbRepoImportHandler>> prepareImportHandlerCallables() {
        List<String> repoKeysToImport = importSettings.getRepositories();
        List<Callable<DbRepoImportHandler>> importers = new ArrayList<>(repoKeysToImport.size());
        for (String repoKey : repoKeysToImport) {
            try {
                Callable<DbRepoImportHandler> handlerCallable = createHandlerCallable(repoKey);
                importers.add(handlerCallable);
            } catch (FileNotFoundException e) {
                if (importSettings.isFailIfEmpty()) {
                    throw new RuntimeException(e);
                } else {
                    statusHolder.warn(e.getMessage(), log);
                }
            }
        }
        return importers;
    }

    private Callable<DbRepoImportHandler> createHandlerCallable(String repoKey) throws FileNotFoundException {
        File repoRoot;
        if (importSettings.isSingleRepoImport()) {
            repoRoot = importSettings.getBaseDir();
        } else {
            // base dir is the root repositories dir (e.g., the specific repo is under 'repositories/repoKey')
            repoRoot = getRepoRootDir(repoKey, importSettings.getBaseDir());
        }
        return callablesFactory.create(repoKey, repoRoot);
    }

    private void deleteRepositories() {
        for (String repoKey : importSettings.getRepositoriesToDelete()) {
            deleteExistingRepository(repoKey);
        }
    }

    private void deleteExistingRepository(String repoKey) {
        statusHolder.status("Fully removing repository '" + repoKey + "'.", log);
        RepoPath deleteRepoPath = InternalRepoPathFactory.repoRootPath(repoKey);
        try {
            repositoryService.undeploy(deleteRepoPath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        statusHolder.status("Repository '" + repoKey + "' fully deleted.", log);
    }

    private void initializeFromContext(JobExecutionContext callbackContext) {
        JobDataMap jobDataMap = callbackContext.getJobDetail().getJobDataMap();
        importSettings = (RepositoryImportSettingsImpl) jobDataMap.get(RepositoryImportSettingsImpl.class.getName());
        callablesFactory.setBaseImportSettings(importSettings);
        statusHolder = importSettings.getStatusHolder();
    }

    private void checkForUnusedSubdirectories() {
        if (importSettings.isSingleRepoImport()) {
            return;
        }
        Collection<String> directoryNames = collectSubdirectoriesNames();
        if (importSettings.isFailIfEmpty()
                && Collections.disjoint(importSettings.getRepositories(), directoryNames)) {
            statusHolder.error("The selected directory did not contain any repositories.", log);
        } else {
            for (String subDir : directoryNames) {
                boolean isMetadata = subDir.contains("metadata");
                boolean isIndex = subDir.contains("index");
                if (!isMetadata && !isIndex && !importSettings.getRepositories().contains(subDir)) {
                    statusHolder.warn("The directory " + subDir + " does not match any repository key.", log);
                }
            }
        }
    }

    private Collection<String> collectSubdirectoriesNames() {
        File[] subDirectories = importSettings.getBaseDir().listFiles(
                (java.io.FileFilter) DirectoryFileFilter.DIRECTORY);
        return Collections2.transform(Arrays.asList(subDirectories), new Function<File, String>() {
            @Nullable
            @Override
            public String apply(File file) {
                return file.getName();
            }
        });
    }

    private File getRepoRootDir(String repoKey, File baseDir) throws FileNotFoundException {
        File repoBaseDir = new File(baseDir, repoKey);
        if (repoBaseDir.canRead() && repoBaseDir.isDirectory()) {
            return repoBaseDir;
        }
        throw new FileNotFoundException("No directory for repository " + repoKey + " found at " + baseDir);
    }

}