package org.artifactory.repo.local;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.repo.StoringRepo;

/**
 * Object containing method params to use in {@link org.artifactory.repo.StoringRepo#shouldProtectPathDeletion(PathDeletionContext)}
 *
 * @author Shay Yaakov
 */
public class PathDeletionContext {

    private StoringRepo repo;
    private String path;
    private BasicStatusHolder status;
    private boolean assertOverwrite;
    private String requestSha1;
    private boolean forceExpiryCheck;

    public PathDeletionContext(StoringRepo repo, String path, BasicStatusHolder status, boolean assertOverwrite,
            String requestSha1, boolean forceExpiryCheck) {
        this.repo = repo;
        this.path = path;
        this.status = status;
        this.assertOverwrite = assertOverwrite;
        this.requestSha1 = requestSha1;
        this.forceExpiryCheck = forceExpiryCheck;
    }

    public StoringRepo getRepo() {
        return repo;
    }

    public String getPath() {
        return path;
    }

    public BasicStatusHolder getStatus() {
        return status;
    }

    public boolean isAssertOverwrite() {
        return assertOverwrite;
    }

    public String getRequestSha1() {
        return requestSha1;
    }

    public boolean isForceExpiryCheck() {
        return forceExpiryCheck;
    }

    public static class Builder {

        private StoringRepo repo;
        private String path;
        private boolean assertOverwrite;
        private String requestSha1;
        private BasicStatusHolder status;
        private boolean forceExpiryCheck;

        public Builder(StoringRepo repo, String path, BasicStatusHolder status) {
            this.repo = repo;
            this.path = path;
            this.status = status;
        }

        public Builder(StoringRepo repo, String path) {
            this.repo = repo;
            this.path = path;
            this.status = new BasicStatusHolder();
        }

        public Builder assertOverwrite(boolean assertOverwrite) {
            this.assertOverwrite = assertOverwrite;
            return this;
        }

        public Builder requestSha1(String requestSha1) {
            this.requestSha1 = requestSha1;
            return this;
        }

        public Builder forceExpiryCheck(boolean forceExpiryCheck) {
            this.forceExpiryCheck = forceExpiryCheck;
            return this;
        }

        public PathDeletionContext build() {
            return new PathDeletionContext(repo, path, status, assertOverwrite, requestSha1, forceExpiryCheck);
        }
    }
}
