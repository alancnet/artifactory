package org.artifactory.storage.config.visitor;


import org.artifactory.storage.config.model.Param;
import org.artifactory.storage.config.model.Property;
import org.artifactory.storage.config.model.ProviderMetaData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ProviderAdapter extends XmlAdapter {

    @Override
    public Object marshal(Object object) throws Exception {
        return object;
    }

    @Override
    public Object unmarshal(Object obj) throws Exception {
        Element element = (Element) obj;
        NodeList childNodes = element.getChildNodes();
        String id = element.getAttribute("id");
        String type = element.getAttribute("type");
        ProviderMetaData providerMetaData = new ProviderMetaData(id, type);
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            String itemName = item.getLocalName() != null ? item.getLocalName().trim() : "";
            String content = item.getTextContent().trim();
            if ("provider".equals(itemName)) {
                ProviderMetaData newProviderMetaData = (ProviderMetaData) unmarshal(item);
                providerMetaData.setProviderMetaData(newProviderMetaData);
            } else if ("sub-provider".equals(itemName)) {
                ProviderMetaData newProviderMetaData = (ProviderMetaData) unmarshal(item);
                providerMetaData.addSubProvider(newProviderMetaData);
            } else if ("property".equals(itemName)) {
                String name = ((Element) item).getAttribute("name");
                String value = ((Element) item).getAttribute("value");
                providerMetaData.addProperty(new Property(name, value));
            } else if (!content.isEmpty()) {
                providerMetaData.addParam(new Param(item.getNodeName(), item.getTextContent()));
            }
        }
        return providerMetaData;
    }
}