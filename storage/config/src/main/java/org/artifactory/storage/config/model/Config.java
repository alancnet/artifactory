package org.artifactory.storage.config.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gidi Shabat
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config extends ModelElement {
    @XmlElement(name = "chain")
    private List<ChainMetaData> chains = new ArrayList<>();
    @XmlElement(name = "provider")
    private List<ProviderMetaData> providerMetaDatas = new ArrayList<>();
    @XmlAttribute(name = "version")
    private String version;

    public List<ChainMetaData> getChains() {
        return chains;
    }

    public String getVersion() {
        return version;
    }

    public List<ProviderMetaData> getProviderMetaDatas() {
        return providerMetaDatas;
    }
}