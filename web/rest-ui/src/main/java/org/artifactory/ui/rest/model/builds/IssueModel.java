package org.artifactory.ui.rest.model.builds;

import org.artifactory.rest.common.model.BaseModel;
import org.jfrog.build.api.Issue;

/**
 * @author Chen Keinan
 */
public class IssueModel extends BaseModel {

    private String key;
    private String url;
    private String summary;
    private boolean aggregated;

    public IssueModel(Issue issue) {
        setAggregated(issue.isAggregated());
        setKey(issue.getKey());
        setSummary(issue.getSummary());
        setUrl(issue.getUrl());
    }

    public IssueModel(String key, String url, String summary) {
        this.key = key;
        this.url = url;
        this.summary = summary;
        this.aggregated = false;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isAggregated() {
        return aggregated;
    }

    public void setAggregated(boolean aggregated) {
        this.aggregated = aggregated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IssueModel)) {
            return false;
        }

        IssueModel that = (IssueModel) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }

        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }

        if (summary != null ? !summary.equals(that.summary) : that.summary != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (key != null ? key.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        return result;
    }

}