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


package org.artifactory.repo.db.importexport
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import org.apache.commons.logging.Log
import org.artifactory.api.config.RepositoryImportSettingsImpl
import org.artifactory.api.context.ArtifactoryContextThreadBinder
import org.artifactory.api.maven.MavenMetadataService
import org.artifactory.api.security.AuthorizationService
import org.artifactory.common.ArtifactoryHome
import org.artifactory.descriptor.repo.LocalRepoDescriptor
import org.artifactory.mime.MimeTypes
import org.artifactory.mime.MimeTypesReader
import org.artifactory.repo.InternalRepoPathFactory
import org.artifactory.repo.LocalRepo
import org.artifactory.repo.interceptor.ImportInterceptors
import org.artifactory.repo.interceptor.StorageAggregationInterceptors
import org.artifactory.repo.service.InternalRepositoryService
import org.artifactory.sapi.fs.MutableVfsFile
import org.artifactory.sapi.fs.MutableVfsFolder
import org.artifactory.schedule.TaskService
import org.artifactory.spring.InternalArtifactoryContext
import org.artifactory.storage.BinaryInsertRetryException
import org.artifactory.storage.binstore.service.BinaryInfoImpl
import org.artifactory.test.ArtifactoryHomeStub
import org.artifactory.util.ResourceUtils
import org.joda.time.DateTimeUtils
import org.joda.time.Duration
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Paths
/**
 *
 * @author Yoav Luft
 */
class DbRepoImportHandlerSpec extends Specification {

    public static final String EMPTY_FILE_SHA1 = "da39a3ee5e6b4b0d3255bfef95601890afd80709"
    public static final String EMPTY_FILE_MD5 = 'd41d8cd98f00b204e9800998ecf8427e'
    File repoRoot
    MimeTypes mimeTypes
    private InternalRepositoryService repositoryService
    private mavenMetadataService
    private transactionManager
    private transactionManagerLog
    private transactionStatus
    private taskService
    private authorizationService
    private importInterceptors
    private aggregationInterceptors
    private artifactoryContext

    def setup() {
        repoRoot = Files.createTempDir()
        InputStream mimeTypesFile = ResourceUtils.getResource(
                "/META-INF/default/" + ArtifactoryHome.MIME_TYPES_FILE_NAME);
        mimeTypes = new MimeTypesReader().read(mimeTypesFile);
        prepareArtifactoryHomeAndContext()
    }

    void cleanup() {
        if (repoRoot != null) {
            FileUtils.deleteDirectory repoRoot
        }
    }

    def "Import single file without metadata"() {
        setup:

        def filename = "file-a.jar"
        makeFiles filename
        def importSettings = new RepositoryImportSettingsImpl(repoRoot)
        def localRepo = Mock(LocalRepo)
        def handler = new DbRepoImportHandler(localRepo, importSettings, "")

        def repoName = "test-repo"
        def rootPath = InternalRepoPathFactory.repoRootPath(repoName)
        def fileRepoPath = InternalRepoPathFactory.create(rootPath, filename)

        repositoryService.localOrCachedRepositoryByKey(repoName) >> localRepo

        localRepo.getKey() >> repoName
        localRepo.getDescriptor() >> Mock(LocalRepoDescriptor)

        def vfsFolder = Mock(MutableVfsFolder)
        def vfsFile = Mock(MutableVfsFile)

        when:

        handler.executeImport()

        then:

        1 * localRepo.createOrGetFolder(rootPath) >> vfsFolder
        _ * vfsFolder.getRepoPath() >> rootPath

        _ * vfsFile.getRepoPath() >> fileRepoPath

        1 * localRepo.createOrGetFile(fileRepoPath) >> vfsFile

        1 * localRepo.isCache() >> false
        _ * localRepo.toString() >> "Mock for local repo 'test-repo'"

        !importSettings.getStatusHolder().isError()
    }

    def "Test BinaryAlreadyExistsException is handled"() {
        setup:

        def filename = "file-a.jar"
        makeFiles filename
        def importSettings = new RepositoryImportSettingsImpl(repoRoot)

        def localRepo = Mock(LocalRepo)

        def repoName = "test-repo"
        def rootPath = InternalRepoPathFactory.repoRootPath(repoName)
        def fileRepoPath = InternalRepoPathFactory.create(rootPath, filename)

        localRepo.getKey() >> repoName
        localRepo.getDescriptor() >> Mock(LocalRepoDescriptor)
        localRepo.isCache() >> false

        repositoryService.localOrCachedRepositoryByKey(repoName) >> localRepo

        def vfsFolder = Mock(MutableVfsFolder)
        def vfsFile = Mock(MutableVfsFile)
        vfsFile.getSha1() >> EMPTY_FILE_SHA1

        def handler = new DbRepoImportHandler(localRepo, importSettings, "")

        when:

        handler.executeImport()

        then:

        1 * localRepo.createOrGetFolder(rootPath) >> vfsFolder
        _ * vfsFolder.getRepoPath() >> rootPath

        2 * localRepo.createOrGetFile(fileRepoPath) >> vfsFile

        _ * vfsFile.getRepoPath() >> fileRepoPath
        1 * vfsFile.tryUsingExistingBinary(_, _, _) >> {
            throw new BinaryInsertRetryException(new BinaryInfoImpl(EMPTY_FILE_SHA1, EMPTY_FILE_MD5, 0), null)
        }

        _ * localRepo.toString() >> "Mock for local repo 'test-repo'"

        then:

        1 * vfsFile.tryUsingExistingBinary(_, _, _) >> true

        !importSettings.getStatusHolder().isError()
    }

    @Ignore
    def "Test file not found but has metadata"() {}

    @Ignore
    def "Test file not found"() {}

    @Ignore
    def "Test commit transaction after 1000 imported items"() {}

    def "Test commit transaction after timeout"() {
        setup:

        def fileA = "file-a.jar"
        def fileB = "file-b.jar"
        makeFiles(fileA, fileB)
        def importSettings = new RepositoryImportSettingsImpl(repoRoot)

        def localRepo = Mock(LocalRepo)

        def repoName = "test-repo"
        def rootPath = InternalRepoPathFactory.repoRootPath(repoName)
        def fileRepoPathA = InternalRepoPathFactory.create(rootPath, fileA)
        def fileRepoPathB = InternalRepoPathFactory.create(rootPath, fileB)

        localRepo.getKey() >> repoName
        localRepo.getDescriptor() >> Mock(LocalRepoDescriptor)
        localRepo.isCache() >> false
        localRepo.toString() >> "Mock for local repo 'test-repo'"

        repositoryService.localOrCachedRepositoryByKey(repoName) >> localRepo

        def vfsFolder = Mock(MutableVfsFolder)
        def vfsFileA = Mock(MutableVfsFile)
        def vfsFileB = Mock(MutableVfsFile)
        vfsFileA.getSha1() >> EMPTY_FILE_SHA1

        localRepo.createOrGetFolder(rootPath) >> vfsFolder
        vfsFolder.getRepoPath() >> rootPath
        vfsFileA.getRepoPath() >> { fileRepoPathA }
        vfsFileB.getRepoPath() >> fileRepoPathB

        def handler = new DbRepoImportHandler(localRepo, importSettings, "")

        when:

        handler.executeImport()

        then:
        // Called by every commit and every start of new transaction
        1 * artifactoryContext.getBean("artifactoryTransactionManager") >> this.transactionManager

        then:
        1 * localRepo.createOrGetFile(fileRepoPathA) >> {
            // Move the time forward!
            DateTimeUtils.setCurrentMillisOffset(Duration.standardMinutes(5).getMillis())
            vfsFileA
        }

        then:
        // Commit then start new transaction
        2 * artifactoryContext.getBean("artifactoryTransactionManager") >> this.transactionManager

        then:
        localRepo.createOrGetFile(fileRepoPathB) >> vfsFileB
        1 * artifactoryContext.getBean("artifactoryTransactionManager") >> this.transactionManager
        !importSettings.getStatusHolder().isError()
    }

    @Ignore
    def "Test termination if parent task terminates"() {}

    def prepareArtifactoryHomeAndContext() {
        artifactoryContext = Mock(InternalArtifactoryContext)
        ArtifactoryContextThreadBinder.bind(artifactoryContext)

        def artifactoryHomeStub = new ArtifactoryHomeStub()
        artifactoryHomeStub.mimeTypes = mimeTypes
        ArtifactoryHome.bind(artifactoryHomeStub)

        setupContext(artifactoryContext)
    }

    private void setupContext(InternalArtifactoryContext context) {
        repositoryService = Mock(InternalRepositoryService)
        context.beanForType(InternalRepositoryService.class) >> this.repositoryService

        mavenMetadataService = Mock(MavenMetadataService)
        context.beanForType(MavenMetadataService.class) >> this.mavenMetadataService

        transactionManager = Mock(AbstractPlatformTransactionManager)
        context.getBean("artifactoryTransactionManager") >> this.transactionManager

        transactionManagerLog = Mock(Log)
        this.transactionManager.logger = this.transactionManagerLog

        transactionStatus = Mock(DefaultTransactionStatus)
        this.transactionManager.newTransactionStatus(_, _, _, _, _, _) >> this.transactionStatus

        taskService = Mock(TaskService)
        context.getTaskService() >> this.taskService

        authorizationService = Mock(AuthorizationService)
        context.getAuthorizationService() >> this.authorizationService
        this.authorizationService.currentUsername() >> "admin"

        importInterceptors = Mock(ImportInterceptors)
        context.beanForType(ImportInterceptors.class) >> this.importInterceptors

        aggregationInterceptors = Mock(StorageAggregationInterceptors)
        context.beanForType(StorageAggregationInterceptors.class) >> this.aggregationInterceptors
    }

    def makeFiles(String... filenames) {
        filenames.each {
            def path = Paths.get(repoRoot.absolutePath, it)
            Files.touch(path.toFile())
        }
    }
}
