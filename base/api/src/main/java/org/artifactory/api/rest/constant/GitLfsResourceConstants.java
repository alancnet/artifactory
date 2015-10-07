

package org.artifactory.api.rest.constant;

/**
 * Constants for the {@link GitLfsResource}
 *
 * @author Dan Feldman
 */
public interface GitLfsResourceConstants {
    String PATH_ROOT = "lfs"; // TODO: [by dan] lfs or gitlfs?

    String OBJECTS = "objects";
    String REPO_KEY = "repoKey";
    String OID = "OID";
    //String API_V1 = "version https://git-lfs.github.com/spec/v1";
    String LFS_JSON = "application/vnd.git-lfs+json";
    //String LFS_BINARY = "application/vnd.git-lfs";
}
