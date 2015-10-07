package org.artifactory.rest.common.model;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties({"hasMessages"})
public class FeedbackMsg extends BaseModel {

    String error;
    String warn;
    String info;
    String url;
    List<String> errors;
    boolean hasMessages;

    public boolean hasMessages() {
        return hasMessages;
    }

    public void setHasMessages(boolean hasMessages) {
        this.hasMessages = hasMessages;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        if (StringUtils.isNotBlank(error)) {
            this.hasMessages = true;
            this.error = error;
        }
    }

    @JsonIgnore
    public boolean hasError() {
        return StringUtils.isNotBlank(error);
    }

    public String getWarn() {
        return warn;
    }

    public void setWarn(String warn) {
        if (StringUtils.isNotBlank(warn)) {
            this.hasMessages = true;
            this.warn = warn;
        }
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        if (StringUtils.isNotBlank(info)) {
            this.hasMessages = true;
            this.info = info;
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.hasMessages = true;
        this.errors = errors;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.hasMessages = true;
        this.url = url;
    }
}
