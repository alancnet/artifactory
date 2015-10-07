package org.artifactory.addon.ha.message;

import org.artifactory.fs.WatcherInfo;
import org.artifactory.repo.RepoPath;

/**
 * @author mamo
 */
public interface WatchesHaMessage extends HaMessage {

    public class AddWatch implements WatchesHaMessage {
        public final long nodeId;
        public final WatcherInfo watchInfo;

        public AddWatch(long nodeId, WatcherInfo watchInfo) {
            this.nodeId = nodeId;
            this.watchInfo = watchInfo;
        }
    }

    public class DeleteAllWatches implements WatchesHaMessage {
        public final RepoPath repoPath;

        public DeleteAllWatches(RepoPath repoPath) {
            this.repoPath = repoPath;
        }
    }

    public class DeleteUserWatches implements WatchesHaMessage {
        public final RepoPath repoPath;
        public final String username;

        public DeleteUserWatches(RepoPath repoPath, String username) {
            this.repoPath = repoPath;
            this.username = username;
        }
    }

    public class DeleteAllUserWatches implements WatchesHaMessage {
        public final String username;

        public DeleteAllUserWatches(String username) {
            this.username = username;
        }
    }
}
