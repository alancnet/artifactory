package org.artifactory.storage.config.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Gidi Shabat
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class ChainMetaData extends ModelElement {
    @XmlElement(name = "provider")
    private ProviderMetaData providerMetaData;
    @XmlAttribute(name = "template")
    private String template;

    public ChainMetaData() {
    }

    public ChainMetaData(ChainMetaData chain) {
        setTemplate(chain.getTemplate());
    }

    public ProviderMetaData getProviderMetaData() {
        return providerMetaData;
    }

    public void setProviderMetaData(ProviderMetaData providerMetaData) {
        this.providerMetaData = providerMetaData;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
