package org.artifactory.addon.ha.message;

/**
 * @author mamo
 */
public enum HaMessageTopic {
    OFFLINE_TOPIC("putOffline"),
    CONFIG_CHANGE_TOPIC("configChange"),
    ACL_CHANGE_TOPIC("aclChange"),
    LICENSES_CHANGE_TOPIC("licensesChange"),
    NUPKG_TOPIC("nuPkgChange"),
    WATCHES_TOPIC("watchesChange");

    private final String topicName;

    HaMessageTopic(String topicName) {
        this.topicName = topicName;
    }

    public String topicName() {
        return topicName;
    }
}
