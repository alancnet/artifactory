package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Shay Yaakov
 */
@XmlType(name = "BowerConfigurationType", propOrder = {"bowerRegistryUrl"}, namespace = Descriptor.NS)
public class BowerConfiguration implements Descriptor {

    @XmlElement(defaultValue = "https://bower.herokuapp.com", required = false)
    private String bowerRegistryUrl = "https://bower.herokuapp.com";

    public String getBowerRegistryUrl() {
        return bowerRegistryUrl;
    }

    public void setBowerRegistryUrl(String bowerRegistryUrl) {
        this.bowerRegistryUrl = bowerRegistryUrl;
    }
}
