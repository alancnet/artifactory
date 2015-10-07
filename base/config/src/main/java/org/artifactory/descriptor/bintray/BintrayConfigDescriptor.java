package org.artifactory.descriptor.bintray;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Bintray global(default) user descriptor
 *
 * @author Dan Feldman
 */
@XmlType(name = "BintrayConfigType", propOrder = {"userName", "apiKey", "fileUploadLimit"}, namespace = Descriptor.NS)
public class BintrayConfigDescriptor implements Descriptor {

    @XmlElement(required = true)
    private String userName;
    @XmlElement(required = true)
    private String apiKey;
    @XmlElement(required = true, defaultValue = "200")
    private int fileUploadLimit = 200;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBintrayAuth() {
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(apiKey)) {
            return userName + ":" + apiKey;
        }
        return null;
    }

    public int getFileUploadLimit() {
        return fileUploadLimit;
    }

    public void setFileUploadLimit(int fileUploadLimit) {
        this.fileUploadLimit = fileUploadLimit;
    }

    public boolean globalCredentialsExist() {
        return (StringUtils.isNotEmpty(userName)) && (StringUtils.isNotEmpty(apiKey));
    }
}
