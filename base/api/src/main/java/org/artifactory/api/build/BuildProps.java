package org.artifactory.api.build;

/**
 * @author Chen keinan
 */
public class BuildProps {
    private String key;
    private String value;
    private String status;
    private String prevValue;

    public BuildProps(String key, String value, String status) {
        this.key = key;
        this.value = value;
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrevValue() {
        return prevValue;
    }

    public void setPrevValue(String prevValue) {
        this.prevValue = prevValue;
    }
}
