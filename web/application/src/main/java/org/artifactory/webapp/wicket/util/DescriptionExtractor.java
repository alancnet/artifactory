/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.webapp.wicket.util;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.lang.PropertyResolver;
import org.artifactory.descriptor.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * Utility class that extracts help messages from the artifactory.xsd file given a Descriptor and a property name.
 *
 * @author Yossi Shaul
 */
public class DescriptionExtractor {
    private static final Logger log = LoggerFactory.getLogger(DescriptionExtractor.class);

    protected Document doc;

    private static DescriptionExtractor instance;

    private DescriptionExtractor() {
        try {
            doc = loadArtifactoryXsd();
        } catch (Exception e) {
            throw new RuntimeException("Error reading schema", e);
        }
    }

    public static synchronized DescriptionExtractor getInstance() {
        if (instance == null) {
            instance = new DescriptionExtractor();
        }
        return instance;
    }

    /**
     * @param descriptor   The descriptor
     * @param propertyName The property name.
     * @return The description of the given property. If no description for the input property empty string will be
     *         returned.
     * @throws IllegalArgumentException if the property not found.
     */
    public String getDescription(Descriptor descriptor, String propertyName) {
        if (descriptor == null) {
            throw new IllegalArgumentException("Descriptor must not be null");
        }

        if (propertyName == null || "".equals(propertyName)) {
            throw new IllegalArgumentException("Property name must not be null or empty");
        }

        Field field = getField(descriptor, propertyName);

        String elementName = getElementName(field);
        String complexTypeName = getComplexTypeName(field.getDeclaringClass());

        String query = buildXPathQuery(complexTypeName, elementName);
        log.debug("Executing xpath query: {}", query);
        String description = executeQuery(query);
        return description;
    }

    private Field getField(Descriptor descriptor, String propertyName) {
        try {
            return PropertyResolver.getPropertyField(propertyName, descriptor);
        } catch (WicketRuntimeException e) {
            throw new IllegalArgumentException("Property field '" + propertyName + "' not found!", e);
        }

    }

    private String getElementName(Field field) {
        // default element name is the field name
        String elementName = field.getName();

        if (field.isAnnotationPresent(XmlElementWrapper.class)) {
            XmlElementWrapper wrapper = field.getAnnotation(XmlElementWrapper.class);
            // use the element name from the annotation only if it's not the default
            if (notDefaultName(wrapper.name())) {
                elementName = wrapper.name();
            }
        } else if (field.isAnnotationPresent(XmlElement.class)) {
            XmlElement annotation = field.getAnnotation(XmlElement.class);
            // use the element name from the annotation only if it's not the default
            if (notDefaultName(annotation.name())) {
                elementName = annotation.name();
            }
        }

        return elementName;
    }

    private boolean notDefaultName(String name) {
        return !"##default".equals(name);
    }

    protected String getComplexTypeName(Class<?> declaringClass) {
        XmlType xmlType = declaringClass.getAnnotation(XmlType.class);
        return xmlType.name();
    }


    private String buildXPathQuery(String xmlType, String elementName) {
        String xpath = "/xs:schema/xs:complexType[@name='" + xmlType + "']" +
                "//xs:element[@name='" + elementName + "']" +
                "/xs:annotation/xs:documentation/text()";
        return xpath;
    }

    private Document loadArtifactoryXsd() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream schemaIn = this.getClass().getResourceAsStream("/artifactory.xsd");
        try {
            return builder.parse(schemaIn);
        } finally {
            IOUtils.closeQuietly(schemaIn);
        }
    }

    private String executeQuery(String query) {
        try {
            XPathFactory xFactory = XPathFactory.newInstance();
            XPath xpath = xFactory.newXPath();
            xpath.setNamespaceContext(new SchemaNamespaceContext());
            XPathExpression expr = xpath.compile(query);
            Object description = expr.evaluate(doc, XPathConstants.STRING);
            return description.toString().trim();
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Failed to execute xpath query: " + query, e);
        }
    }

    public static class SchemaNamespaceContext implements NamespaceContext {
        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Null prefix");
            } else if ("xs".equals(prefix)) {
                return XMLConstants.W3C_XML_SCHEMA_NS_URI;
            } else if ("xml".equals(prefix)) {
                return XMLConstants.XML_NS_URI;
            }
            return XMLConstants.NULL_NS_URI;
        }

        // This method isn't necessary for XPath processing.

        @Override
        public String getPrefix(String uri) {
            throw new UnsupportedOperationException();
        }

        // This method isn't necessary for XPath processing either.

        @Override
        public Iterator getPrefixes(String uri) {
            throw new UnsupportedOperationException();
        }
    }
}
