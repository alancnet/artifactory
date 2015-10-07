package org.artifactory.rest.common.model;

import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class BaseModel implements RestModel {

    FeedbackMsg feedbackMsg;

    public FeedbackMsg getFeedbackMsg() {
        return feedbackMsg;
    }

    public void setFeedbackMsg(FeedbackMsg feedbackMsg) {
        this.feedbackMsg = feedbackMsg;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
