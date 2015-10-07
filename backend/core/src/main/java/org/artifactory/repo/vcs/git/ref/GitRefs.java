package org.artifactory.repo.vcs.git.ref;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Set;

/**
 * Represents a git repository tags and branches.
 * Follows the get command 'curl https://github.com/twbs/bootstrap.git/info/refs?service=git-upload-pack'
 *
 * @author Shay Yaakov
 */
public class GitRefs {

    public static final String REFS_FILENAME = "gitrefs";
    public Set<GitRef> tags = Sets.newLinkedHashSet();
    public Set<GitRef> branches = Sets.newLinkedHashSet();

    public boolean isEmpty() {
        return tags.isEmpty() && branches.isEmpty();
    }

    public boolean contains(String tagOrBrnach) {
        for (GitRef tagRef : tags) {
            if (StringUtils.equalsIgnoreCase(tagRef.name, tagOrBrnach)) {
                return true;
            }
        }
        for (GitRef branchRef : branches) {
            if (StringUtils.equalsIgnoreCase(branchRef.name, tagOrBrnach)) {
                return true;
            }
        }
        return false;
    }

    public void merge(GitRefs other) {
        if (other == null) {
            return;
        }

        tags.addAll(other.tags);
        branches.addAll(other.branches);
    }

    public InputStream constructOriginalRefsStream() {
        final StringBuilder sb = new StringBuilder();
        for (GitRef tag : tags) {
            sb.append("0000").append(tag.commitId).append(" refs/tags/").append(tag).append("\n");
        }
        for (GitRef branch : branches) {
            sb.append("0000").append(branch.commitId).append(" refs/heads/").append(branch).append("\n");
        }

        return IOUtils.toInputStream(sb.toString());
    }
}
