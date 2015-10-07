package org.artifactory.addon.ha.message;

import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

/**
 * @author mamo
 */
public interface NuPkgHaMessage extends HaMessage {

    public class Added implements NuPkgHaMessage {
        public final RepoPath repoPath;
        public final Properties properties;

        public Added(RepoPath repoPath, Properties properties) {
            this.repoPath = repoPath;
            this.properties = properties;
        }
    }

    public class Removed implements NuPkgHaMessage {
        public final String repoKey;
        public final String packageId;
        public final String packageVersion;

        public Removed(String repoKey, String packageId, String packageVersion) {
            this.repoKey = repoKey;
            this.packageId = packageId;
            this.packageVersion = packageVersion;
        }
    }
}
