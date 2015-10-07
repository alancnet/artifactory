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

package org.artifactory.common;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * @author freds
 * @date Oct 10, 2008
 */
@SuppressWarnings({"EnumeratedConstantNamingConvention"})
public enum ConstantValues {
    test("runMode.test", FALSE), //Use and set only in specific itests - has serious performance implications
    qa("runMode.qa", FALSE),
    dev("runMode.dev", FALSE),
    devHa("runMode.devHa", FALSE),
    artifactoryVersion("version"),
    artifactoryRevision("revision"),
    artifactoryTimestamp("timestamp"),
    supportUrlSessionTracking("servlet.supportUrlSessionTracking", FALSE),
    disabledAddons("addons.disabled", ""),
    addonsInfoUrl("addons.info.url", "http://service.jfrog.org/artifactory/addons/info/%s"),
    addonsConfigureUrl("addons.info.url", "http://www.jfrog.com/confluence/display/RTF/%s"),
    springConfigDir("spring.configDir"),
    asyncCorePoolSize("async.corePoolSize", 4 * Runtime.getRuntime().availableProcessors()),
    asyncPoolTtlSecs("async.poolTtlSecs", 60),
    asyncPoolMaxQueueSize("async.poolMaxQueueSize", 10000),
    versioningQueryIntervalSecs("versioningQueryIntervalSecs", Seconds.HOUR * 2),
    logsViewRefreshRateSecs("logs.viewRefreshRateSecs", 10),
    locksTimeoutSecs("locks.timeoutSecs", 120),
    locksDebugTimeouts("locks.debugTimeouts", FALSE),
    taskCompletionLockTimeoutRetries("task.completionLockTimeoutRetries", 100),
    substituteRepoKeys("repo.key.subst."),
    repoConcurrentDownloadSyncTimeoutSecs("repo.concurrentDownloadSyncTimeoutSecs", Seconds.MINUTE * 15),
    downloadStatsEnabled("repo.downloadStatsEnabled", TRUE),
    disableGlobalRepoAccess("repo.global.disabled", FALSE),
    fsItemCacheIdleTimeSecs("fsitem.cache.idleTimeSecs", Seconds.MINUTE * 20),
    dockerTokensCacheIdleTimeSecs("docker.tokens.cache.idleTimeSecs", Seconds.MINUTE * 4),
    cacheFSSyncquietPeriodSecs("cacheFS.sync.quietPeriodSecs", Seconds.MINUTE * 15),
    searchMaxResults("search.maxResults", 500),
    searchUserQueryLimit("search.userQueryLimit", 1000),
    searchMaxFragments("search.content.maxFragments", 500),
    searchMaxFragmentsSize("search.content.maxFragmentsSize", 5000),
    searchArchiveMinQueryLength("search.archive.minQueryLength", 3),
    searchPatternTimeoutSecs("search.pattern.timeoutSecs", 30),
    gcUseIndex("gc.useIndex", FALSE),
    gcIntervalSecs("gc.intervalSecs", Seconds.DAY),
    gcDelaySecs("gc.delaySecs", Seconds.HOUR * 2),
    gcSleepBetweenNodesMillis("gc.sleepBetweenNodesMillis", 20),
    gcScanStartSleepingThresholdMillis("gc.scanStartSleepingThresholdMillis", 20000),
    gcScanSleepBetweenIterationsMillis("gc.scanSleepBetweenIterationsMillis", 200),
    gcFileScanSleepIterationMillis("gc.fileScanSleepIterationMillis", 1000),
    gcFileScanSleepMillis("gc.fileScanSleepMillis", 250),
    gcMaxCacheEntries("gc.maxCacheEntries", 10000),
    trafficCollectionActive("traffic.collectionActive", FALSE),
    securityAuthenticationCacheInitSize("security.authentication.cache.initSize", 100),
    securityAuthenticationCacheIdleTimeSecs("security.authentication.cache.idleTimeSecs", Seconds.MINUTE * 5),
    userLastAccessUpdatesResolutionSecs("security.userLastAccessUpdatesResolutionSecs", 5),
    securityAuthenticationEncryptedPasswordSurroundChars(
            "security.authentication.encryptedPassword.surroundChars", "{}"),
    securityUseBase64("security.useBase64", FALSE),
    securityMasterKeyLocation("security.master.key", "security/artifactory.key"),
    securityDisableRememberMe("security.disableRememberMe", FALSE),
    ldapForceGroupMemberAttFullDN("security.ldap.forceGroupMemberAttFullDN", FALSE),
    mvnCentralHostPattern("mvn.central.hostPattern", ".maven.org"),
    mvnCentralIndexerMaxQueryIntervalSecs("mvn.central.indexerMaxQueryIntervalSecs", Seconds.DAY),
    mvnMetadataVersionsComparator("mvn.metadataVersionsComparatorFqn"),
    mvnMetadataSnapshotComparator("mvn.metadataSnapshotComparatorFqn"),
    mvnDynamicMetadataCacheRetentionSecs("mvn.dynamicMetadata.cacheRetentionSecs", 10),
    mvnMetadataVersion3Enabled("mvn.metadata.version3.enabled", TRUE),
    mvnCustomTypes("mvn.custom.types", "tar.gz,tar.bz2"),
    requestDisableVersionTokens("request.disableVersionTokens", FALSE),
    requestSearchLatestReleaseByDateCreated("request.searchLatestReleaseByDateCreated", FALSE),
    buildMaxFoldersToScanForDeletionWarnings("build.maxFoldersToScanForDeletionWarnings", 2),
    missingBuildChecksumCacheIdeTimeSecs("build.checksum.cache.idleTimeSecs", Seconds.MINUTE * 5),
    artifactoryUpdatesRefreshIntervalSecs("updates.refreshIntervalSecs", Seconds.HOUR * 4),
    artifactoryUpdatesUrl("updates.url", "http://service.jfrog.org/artifactory/updates"),
    artifactoryRequestsToGlobalCanRetrieveRemoteArtifacts(
            "artifactoryRequestsToGlobalCanRetrieveRemoteArtifacts", FALSE),
    uiSyntaxColoringMaxTextSizeBytes("ui.syntaxColoringMaxTextSizeBytes", 512000),
    pluginScriptsRefreshIntervalSecs("plugin.scripts.refreshIntervalSecs", 0),
    aolPluginSupport("plugin.aol.support", FALSE),
    aolDedicatedServer("aol.dedicated.server", FALSE),
    aolDisplayAccountManagementLink("aol.displayAccountManagementLink", TRUE),
    uiChroot("ui.chroot"),
    artifactoryLicenseDir("licenseDir"),
    fileRollerMaxFilesToRetain("file.roller.maxFileToRetain", 10),
    backupFileExportSleepIterationMillis("backup.fileExportSleepIterationMillis", 2000),
    backupFileExportSleepMillis("backup.fileExportSleepMillis", 250),
    s3backupBucket("backup.s3.bucket"),
    s3backupFolder("backup.s3.folder"),
    s3backupAccountId("backup.s3.accountId"),
    s3backupAccountSecretKey("backup.s3.accountSecretKey"),
    httpAcceptEncodingGzip("http.acceptEncoding.gzip", true),
    httpUseExpectContinue("http.useExpectContinue", false),
    httpForceForbiddenResponse("http.forceForbiddenResponse", FALSE),
    enableCookieManagement("http.enableCookieManagement", false),
    filteringResourceSizeKb("filtering.resourceSizeKb", 64),
    searchForExistingResourceOnRemoteRequest("repo.remote.checkForExistingResourceOnRequest", TRUE),
    versionQueryEnabled("version.query.enabled", true),
    hostId("host.id"),
    responseDisableContentDispositionFilename("response.disableContentDispositionFilename", FALSE),
    yumCalculationRequestAggregationTimeWindowSecs("yum.calculationRequest.aggregationTimeWindowSecs", 60),
    debianCalculationRequestAggregationTimeWindowSecs("debian.calculationRequest.aggregationTimeWindowSecs", 60),
    yumCalculationRequestAggregationCycleSecs("yum.calculationRequest.aggregationCycleSecs", 60),
    debianCalculationRequestAggregationCycleSecs("debian.calculationRequest.aggregationCycleSecs", 60),
    globalExcludes("repo.includeExclude.globalExcludes"),
    archiveLicenseFileNames("archive.licenseFile.names", "license,LICENSE,license.txt,LICENSE.txt,LICENSE.TXT"),
    uiSearchMaxRowsPerPage("ui.search.maxRowsPerPage", 20),
    replicationChecksumDeployMinSizeKb("replication.checksumDeploy.minSizeKb", 10),
    replicationConsumerQueueSize("replication.consumer.queueSize", 1),
    replicationLocalIterationSleepThresholdMillis("replication.local.iteration.sleepThresholdMillis", 1000),
    replicationLocalIterationSleepMillis("replication.local.iteration.sleepMillis", 100),
    replicationEventQueueSize("replication.event.queue.size", 50000),
    requestExplodedArchiveExtensions("request.explodedArchiveExtensions", "zip,tar,tar.gz,tgz"),
    jCenterUrl("bintray.jcenter.url", "http://jcenter.bintray.com"),
    bintrayUrl("bintray.url", "https://bintray.com"),
    bintrayApiUrl("bintray.api.url", "https://api.bintray.com"),
    bintrayUIHideUploads("bintray.ui.hideUploads", FALSE),
    bintrayUIHideInfo("bintray.ui.hideInfo", FALSE),
    bintrayUIHideRemoteSearch("bintray.ui.hideRemoteSearch", FALSE),
    bintraySystemUser("bintray.system.user"),
    bintraySystemUserApiKey("bintray.system.api.key"),
    bintrayClientThreadPoolSize("bintray.client.threadPool.size", 5),
    bintrayClientRequestTimeout("bintray.client.requestTimeoutMS",150000),
    bintrayClientSignRequestTimeout("bintray.client.signRequestTimeoutMS",90000),
    useUserNameAutoCompleteOnLogin("useUserNameAutoCompleteOnLogin", "on"),
    uiHideEncryptedPassword("ui.hideEncryptedPassword", FALSE),
    statsFlushIntervalSecs("stats.flushIntervalSecs", 30),
    statsRemoteFlushIntervalSecs("stats.remote.flushIntervalSecs", 35),
    statsFlushTimeoutSecs("stats.flushTimeoutSecs", 120),
    integrationCleanupIntervalSecs("integrationCleanup.intervalSecs", 300),
    integrationCleanupQuietPeriodSecs("integrationCleanup.quietPeriodSecs", 60),
    folderPruningIntervalSecs("folderPruning.intervalSecs", 300),
    folderPruningQuietPeriodSecs("folderPruning.quietPeriodSecs", 60),
    virtualCleanupMaxAgeHours("repo.virtualCacheCleanup.maxAgeHours", 168),
    virtualCleanupNamePattern("repo.virtualCacheCleanup.pattern", "*.pom"),
    defaultSaltValue("security.authentication.password.salt", "CAFEBABEEBABEFAC"),
    dbIdGeneratorFetchAmount("db.idGenerator.fetch.amount", 2000),
    dbIdGeneratorMaxUpdateRetries("db.idGenerator.max.update.retries", 50),
    gemsLocalIndexTaskIntervalSecs("gems.localIndexTaskIntervalSecs", 30),
    gemsVirtualIndexTaskIntervalSecs("gems.virtualIndexTaskIntervalSecs", 300),
    gemsIndexTaskQueueLimit("gems.gemsIndexTaskQueueLimit", 20000),
    gemsAfterRepoInitHack("gems.gemsAfterRepoInitHack", true),
    securityCrowdGroupStartIndex("security.authentication.crowd.group.startIndex", 0),
    securityCrowdMaxGroupResults("security.authentication.crowd.group.maxResults", 9999),
    uiHideChecksums("ui.hideChecksums", FALSE),
    archiveIndexerTaskIntervalSecs("archive.indexer.intervalSecs", 60),
    inMemoryNuGetRemoteCaches("nuget.inMemoryRemoteCaches", TRUE),
    nuGetRequireAuthentication("nuget.forceAuthentication", FALSE),
    nuGetAllowRootGetWithAnon("nuget.allowRootGetWithAnon", FALSE),
    dockerForceAuthentication("docker.forceAuthentication", FALSE),
    haHeartbeatIntervalSecs("ha.heartbeat.intervalSecs", 5),
    haHeartbeatStaleIntervalSecs("ha.heartbeat.staleSecs", 30),
    haMembersIntroductionIntervalSecs("ha.membersIntroduction.intervalSecs", 30),
    haMembersIntroductionStaleIntervalSecs("ha.membersIntroduction.staleSecs", 30),
    npmIndexQuietPeriodSecs("npm.index.quietPeriodSecs", 60),
    npmIndexCycleSecs("npm.index.cycleSecs", 60),
    importMaxParallelRepos("import.max.parallelRepos", Runtime.getRuntime().availableProcessors() - 1),
    debianDistributionPath("debian.distribution.path", "dists"),
    debianIndexQuietPeriodSecs("debian.index.quietPeriodSecs", 60),
    debianIndexCycleSecs("debian.index.cycleSecs", 2),
    debianDefaultArchitectures("debian.default.architectures", "i386,amd64"),
    pypiIndexQuietPeriodSecs("pypi.index.quietPeriodSecs", 60),
    pypiIndexSleepSecs("pypi.index.sleepMilliSecs", 60),
    dockerCleanupMaxAgeMillis("docker.cleanup.maxAgeMillis", Seconds.DAY * 1000),
    httpRangeSupport("http.range.support", true);
    public static final String SYS_PROP_PREFIX = "artifactory.";

    private final String propertyName;
    private final String defValue;

    ConstantValues(String propertyName) {
        this(propertyName, null);
    }

    ConstantValues(String propertyName, Object defValue) {
        this.propertyName = SYS_PROP_PREFIX + propertyName;
        this.defValue = defValue == null ? null : defValue.toString();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getDefValue() {
        return defValue;
    }

    public String getString() {
        return ArtifactoryHome.get().getArtifactoryProperties().getProperty(this);
    }

    public int getInt() {
        return (int) getLong();
    }

    public long getLong() {
        return ArtifactoryHome.get().getArtifactoryProperties().getLongProperty(this);
    }

    public boolean getBoolean() {
        return ArtifactoryHome.get().getArtifactoryProperties().getBooleanProperty(this);
    }

    public boolean isSet() {
        return ArtifactoryHome.get().getArtifactoryProperties().hasProperty(this);
    }

    private static class Seconds {
        private static final int MINUTE = 60;
        private static final int HOUR = MINUTE * 60;
        private static final int DAY = HOUR * 24;
    }
}
