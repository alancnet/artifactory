package org.artifactory.rest.common.model;

/**
 * @author Chen Keinan
 */
public interface RestSpecialFields {

    /**
     * conditional ignore special fields
     * @IgnoreSpecialFields will be ignores during json serialization
     * when overriding toString with the following:
     * JsonUtil.jsonToStringIgnoreSpecialFields()
     */
    boolean ignoreSpecialFields();
}
