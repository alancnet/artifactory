package org.artifactory.repo.vcs.git.ref;

/**
 * @author Shay Yaakov
 */
public class GitRef {
    public String name;
    public String commitId;
    public boolean isBranch;

    public GitRef(String name, String commitId, boolean isBranch) {
        this.name = name;
        this.commitId = commitId;
        this.isBranch = isBranch;
    }

    public String filename(String gitRepo, String ext) {
        String branchTag = isBranch ? this.name  + "-" + commitId : this.name;
        return gitRepo + "-" + branchTag + "." + ext;
    }
}
