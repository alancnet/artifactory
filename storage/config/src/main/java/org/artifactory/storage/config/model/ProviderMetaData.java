package org.artifactory.storage.config.model;


import org.artifactory.storage.config.visitor.ProviderAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Gidi Shabat
 */

@XmlJavaTypeAdapter(value = ProviderAdapter.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderMetaData extends ModelElement {
    @XmlElement(name = "provider")
    private ProviderMetaData providerMetaData;
    @XmlElement(name = "sub-provider")
    private List<ProviderMetaData> subProviderMetaData = new ArrayList<>();
    @XmlAttribute(name = "type")
    private String type;
    @XmlElement(name = "param")
    private Set<Param> params = new HashSet<>();
    @XmlElement(name = "property")
    private Set<Property> properties = new HashSet<>();
    @XmlAttribute(name = "id")
    private String id;

    public ProviderMetaData(ProviderMetaData providerMetaData) {
        setId(providerMetaData.getId());
        this.type = providerMetaData.getType();
        this.params.addAll(providerMetaData.params);
        this.properties.addAll(providerMetaData.properties);
    }

    public ProviderMetaData(String id, String type) {
        setId(id);
        setType(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addParam(Param param) {
        params.add(param);
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public ProviderMetaData getProviderMetaData() {
        return providerMetaData;
    }

    public void setProviderMetaData(ProviderMetaData providerMetaData) {
        this.providerMetaData = providerMetaData;
    }

    public List<ProviderMetaData> getSubProviderMetaDataList() {
        return subProviderMetaData;
    }

    public Set<Param> getParams() {
        return params;
    }

    public String getParamValue(String name) {
        for (Param param : params) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    public Param getParam(String name) {
        for (Param param : params) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    public String getPoperty(String name) {
        for (Property property : properties) {
            if (property.getName().equals(name)) {
                return property.getValue();
            }
        }
        return null;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ProviderMetaData merge(ProviderMetaData defaultProviderMetaData) {
        //Merge properties
        HashSet<Property> tempProperties = new HashSet(defaultProviderMetaData.properties);
        tempProperties.addAll(properties);
        properties = tempProperties;
        //Merge params
        HashSet<Param> tempParams = new HashSet(defaultProviderMetaData.params);
        tempParams.addAll(params);
        params = tempParams;
        return this;
    }

    public void addSubProvider(ProviderMetaData providerMetaData) {
        subProviderMetaData.add(providerMetaData);
    }
}