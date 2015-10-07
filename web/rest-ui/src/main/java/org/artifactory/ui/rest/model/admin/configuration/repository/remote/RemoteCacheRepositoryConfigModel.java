package org.artifactory.ui.rest.model.admin.configuration.repository.remote;

import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class RemoteCacheRepositoryConfigModel implements RestModel {

    protected Integer keepUnusedArtifactsHours = DEFAULT_KEEP_UNUSED_ARTIFACTS;
    protected Long retrievalCachePeriodSecs = DEFAULT_RETRIEVAL_CACHE_PERIOD;
    protected Long assumedOfflineLimitSecs = DEFAULT_ASSUMED_OFFLINE;
    protected Long missedRetrievalCachePeriodSecs = DEFAULT_MISSED_RETRIEVAL_PERIOD;

    public Integer getKeepUnusedArtifactsHours() {
        return keepUnusedArtifactsHours;
    }

    public void setKeepUnusedArtifactsHours(Integer keepUnusedArtifactsHours) {
        this.keepUnusedArtifactsHours = keepUnusedArtifactsHours;
    }

    public Long getRetrievalCachePeriodSecs() {
        return retrievalCachePeriodSecs;
    }

    public void setRetrievalCachePeriodSecs(Long retrievalCachePeriodSecs) {
        this.retrievalCachePeriodSecs = retrievalCachePeriodSecs;
    }

    public Long getAssumedOfflineLimitSecs() {
        return assumedOfflineLimitSecs;
    }

    public void setAssumedOfflineLimitSecs(Long assumedOfflineLimitSecs) {
        this.assumedOfflineLimitSecs = assumedOfflineLimitSecs;
    }

    public Long getMissedRetrievalCachePeriodSecs() {
        return missedRetrievalCachePeriodSecs;
    }

    public void setMissedRetrievalCachePeriodSecs(Long missedRetrievalCachePeriodSecs) {
        this.missedRetrievalCachePeriodSecs = missedRetrievalCachePeriodSecs;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
