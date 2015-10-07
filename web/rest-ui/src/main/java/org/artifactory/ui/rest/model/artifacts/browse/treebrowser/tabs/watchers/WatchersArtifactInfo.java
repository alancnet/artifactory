package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers;

import java.util.Date;

import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author Chen Keinan
 */
public class WatchersArtifactInfo extends BaseArtifactInfo implements RestModel {

    public WatchersArtifactInfo(String name) {
        super(name);
    }

    WatchersArtifactInfo() {
    }

    private String watcherName;
    private String watchingSince;
    private String watchConfigureOn;
    private String action;

    public WatchersArtifactInfo(String username, long watchingSinceTime, RepoPath watchedPath,
            String removeWatcher) {
        this.watcherName = username;
        setFormattedDate(watchingSinceTime);
        this.watchConfigureOn = watchedPath.toString();
        this.action = removeWatcher;
    }

    /**
     * set data with pretty time format
     * @param watchingSinceTime  - date in long , milliseconds since 1970
     */
    private void setFormattedDate(long watchingSinceTime) {
        PrettyTime prettyTime = new PrettyTime();
        Date sinceDate = new Date(watchingSinceTime);
        this.watchingSince=  prettyTime.format(sinceDate) + " (" + sinceDate.toString() + ")";
    }

    public String getWatcherName() {
        return watcherName;
    }

    public void setWatcherName(String watcherName) {
        this.watcherName = watcherName;
    }

    public String getWatchingSince() {
        return watchingSince;
    }

    public void setWatchingSince(String watchingSince) {
        this.watchingSince = watchingSince;
    }

    public String getWatchConfigureOn() {
        return watchConfigureOn;
    }

    public void setWatchConfigureOn(String watchConfigureOn) {
        this.watchConfigureOn = watchConfigureOn;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
