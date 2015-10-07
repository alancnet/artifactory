package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.ChecksumPolicyType;
import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.LocalRepoChecksumPolicyType;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsType;

/**
 * @author Dan Feldman
 */
public interface RepoConfigDefaultValues {

    //local basic
    String DEFAULT_INCLUDES_PATTERN = "**/*";
    String DEFAULT_REPO_LAYOUT = "simple-default";

    //local advanced
    boolean DEFAULT_BLACKED_OUT = false;
    boolean DEFAULT_ALLOW_CONTENT_BROWSING = false;

    //remote basic
    boolean DEFAULT_OFFLINE = false;
    ContentSynchronisation DEFAULT_DELEGATION_CONTEXT = null;

    //remote advanced
    boolean DEFAULT_HARD_FAIL = false;
    boolean DEFAULT_STORE_ARTIFACTS_LOCALLY = true;
    boolean DEFAULT_SYNC_PROPERTIES = false;
    boolean DEFAULT_SHARE_CONFIG = false;

    boolean DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE = true;
    boolean DEFAULT_LIST_REMOTE_ITEMS_UNSUPPORTED_TYPE = false;

    //network
    int DEFAULT_SOCKET_TIMEOUT = 15000;
    boolean DEFAULT_LENIENENT_HOST_AUTH = false;
    boolean DEFAULT_COOKIE_MANAGEMENT = false;

    //cache
    int DEFAULT_KEEP_UNUSED_ARTIFACTS = 0;
    long DEFAULT_RETRIEVAL_CACHE_PERIOD = 600;
    long DEFAULT_ASSUMED_OFFLINE = 300;
    long DEFAULT_MISSED_RETRIEVAL_PERIOD = 1800;

    //replication
    boolean DEFAULT_LOCAL_REPLICATION_ENABLED = true;
    boolean DEFAULT_REMOTE_REPLICATION_ENABLED = false;
    boolean DEFAULT_EVENT_REPLICATION = false;
    boolean DEFAULT_REPLICATION_SYNC_DELETES = false;

    //virtual
    boolean DEFAULT_VIRTUAL_CAN_RETRIEVE_FROM_REMOTE = false;

    //bower
    String DEFAULT_BOWER_REGISTRY = "https://bower.herokuapp.com";

    //debian
    boolean DEFAULT_DEB_TRIVIAL_LAYOUT = false;

    //docker
    DockerApiVersion DEFAULT_DOCKER_API_VER = DockerApiVersion.V2;
    boolean DEFAULT_TOKEN_AUTH = true;
    boolean DEFAULT_FORCE_DOCKER_AUTH = false;

    //maven / gradle / ivy / sbt
    int DEFAULT_MAX_UNIQUE_SNAPSHOTS = 0;
    boolean DEFAULT_HANDLE_RELEASES = true;
    boolean DEFAULT_HANDLE_SNAPSHOTS = true;
    boolean DEFAULT_SUPPRESS_POM_CHECKS = true;
    boolean DEFAULT_SUPPRESS_POM_CHECKS_MAVEN = false;
    SnapshotVersionBehavior DEFAULT_SNAPSHOT_BEHAVIOR = SnapshotVersionBehavior.UNIQUE;
    LocalRepoChecksumPolicyType DEFAULT_CHECKSUM_POLICY = LocalRepoChecksumPolicyType.CLIENT;
    boolean DEFAULT_EAGERLY_FETCH_JARS = false;
    boolean DEFAULT_EAGERLY_FETCH_SOURCES = false;
    ChecksumPolicyType DEFAULT_REMOTE_CHECKSUM_POLICY = ChecksumPolicyType.GEN_IF_ABSENT;
    boolean DEFAULT_REJECT_INVALID_JARS = false;
    PomCleanupPolicy DEFAULT_POM_CLEANUP_POLICY = PomCleanupPolicy.discard_active_reference;

    //nuget
    String DEFAULT_NUGET_FEED_PATH = "api/v2";
    String DEFAULT_NUGET_DOWNLOAD_PATH = "api/v2/package";
    boolean DEFAULT_FORCE_NUGET_AUTH = false;

    //vcs
    VcsType DEFAULT_VCS_TYPE = VcsType.GIT;
    VcsGitProvider DEFAULT_GIT_PROVIDER = VcsGitProvider.GITHUB;

    //yum
    int DEFAULT_YUM_METADATA_DEPTH = 0;
    String DEFAULT_YUM_GROUPFILE_NAME = "groups.xml";
    boolean DEFAULT_YUM_AUTO_CALCULATE = true;

    // default remote registry urls
    String NUGET_URL = "https://www.nuget.org/";
    String RUBYGEMS_URL = "https://rubygems.org/";
    String MAVEN_GROUP_URL = "https://jcenter.bintray.com";
    String NPM_URL = "https://registry.npmjs.org";
    String PYPI_URL = "https://pypi.python.org";
    String DOCKER_URL = "https://registry-1.docker.io/";
    String VCS_URL = "https://github.com/";
}