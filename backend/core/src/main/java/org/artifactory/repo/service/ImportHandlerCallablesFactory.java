/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

import org.artifactory.api.config.ImportSettingsImpl;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.db.importexport.DbRepoImportHandler;
import org.artifactory.sapi.common.ImportSettings;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

/**
 * @author Yoav Luft
 */
public class ImportHandlerCallablesFactory {

    private final Semaphore externalSynchronization;
    private InternalRepositoryService repositoryService;
    private ImportSettings baseImportSettings;
    private String parentTaskToken;

    public ImportHandlerCallablesFactory(Semaphore importsParallelGate, String parentTaskToken) {
        repositoryService = ContextHelper.get().beanForType(InternalRepositoryService.class);
        this.externalSynchronization = importsParallelGate;
        this.parentTaskToken = parentTaskToken;
    }

    @Nonnull
    public Callable<DbRepoImportHandler> create(@Nonnull String repoKey, @Nonnull File repoRoot) {
        final ImportSettingsImpl repoSettings = new ImportSettingsImpl(repoRoot, this.baseImportSettings);
        final LocalRepo localRepo = repositoryService.getLocalRepository(InternalRepoPathFactory.repoRootPath(repoKey));
        return new Callable<DbRepoImportHandler>() {
            @Override
            public DbRepoImportHandler call() throws Exception {
                try {
                    DbRepoImportHandler importHandler = new DbRepoImportHandler(localRepo, repoSettings,
                            parentTaskToken);
                    importHandler.executeImport();
                    return importHandler;
                } finally {
                    if (externalSynchronization != null) {
                        externalSynchronization.release();
                    }
                }
            }
        };
    }

    public void setBaseImportSettings(ImportSettings baseImportSettings) {
        this.baseImportSettings = baseImportSettings;
    }
}
