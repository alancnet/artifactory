package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeleteWatchersModel implements RestModel {
    private List<DeleteWatcher> watches = Lists.newArrayList();

    public List<DeleteWatcher> getWatches() {
        return watches;
    }

    public void addUser(DeleteWatcher watcher) {
        watches.add(watcher);
    }
}
