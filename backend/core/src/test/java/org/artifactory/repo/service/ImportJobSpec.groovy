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
package org.artifactory.repo.service

import com.google.common.io.Files
import org.artifactory.api.config.RepositoryImportSettingsImpl
import org.artifactory.repo.db.importexport.DbRepoImportHandler
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.impl.JobDetailImpl
import org.springframework.core.task.AsyncTaskExecutor
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.Semaphore

/**
 *
 * @author Yoav Luft
 */
class ImportJobSpec extends Specification {

    def "test finalizeImport called only after import"() {
        setup:
        def asyncTaskExecutor = Mock(AsyncTaskExecutor)
        def callablesFactory = Mock(ImportHandlerCallablesFactory)
        def semaphore = new Semaphore(3)
        def repositoryService = Mock(InternalRepositoryService)

        def importJob = new ImportJob(asyncTaskExecutor, callablesFactory, semaphore, repositoryService)

        def repositoryKeys = ["repo1", "repo2", "repo3"]
        def importRootFile = createDirectories(repositoryKeys)
        importRootFile.mkdir()
        def importSettings = new RepositoryImportSettingsImpl(importRootFile)

        importSettings.repositories = repositoryKeys

        def repo1Handler = Mock(DbRepoImportHandler)
        def repo1Callable = { return repo1Handler } as Callable<DbRepoImportHandler>
        def repo2Handler = Mock(DbRepoImportHandler)
        def repo2Callable = { return repo2Handler } as Callable<DbRepoImportHandler>
        def repo3Handler = Mock(DbRepoImportHandler)
        def repo3Callable = { return repo3Handler } as Callable

        def internalDataMap = [:]
        internalDataMap.put(RepositoryImportSettingsImpl.class.getName(), importSettings)
        def jobDataMap = new JobDataMap(internalDataMap)

        def jobDetail = new JobDetailImpl()
        jobDetail.setJobDataMap(jobDataMap)

        def jobExecutionContext = Mock(JobExecutionContext)
        jobExecutionContext.getJobDetail() >> jobDetail

        when:
        importJob.onExecute(jobExecutionContext)

        then:
        1 * callablesFactory.create("repo1", _ as File) >> { repo1Callable }
        1 * callablesFactory.create("repo2", _ as File) >> { repo2Callable }
        1 * callablesFactory.create("repo3", _ as File) >> { repo3Callable }
        1 * asyncTaskExecutor.submit(repo1Callable) >> { { -> return repo1Handler } as Future }
        1 * asyncTaskExecutor.submit(repo2Callable) >> { { -> return repo2Handler } as Future }
        1 * asyncTaskExecutor.submit(repo3Callable) >> { { -> return repo3Handler } as Future }

        then:
        1 * repo1Handler.finalizeImport()
        1 * repo2Handler.finalizeImport()
        1 * repo3Handler.finalizeImport()

        cleanup:
        importRootFile.delete()
    }

    def createDirectories(dirNames) {
        def root = Files.createTempDir()
        root.setExecutable(true)
        root.setWritable(true)
        dirNames.each {
            new File(root, it).mkdir()
        }
        return root
    }
}
