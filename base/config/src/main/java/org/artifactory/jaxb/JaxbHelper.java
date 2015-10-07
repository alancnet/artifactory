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

package org.artifactory.jaxb;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * @author yoavl
 */
public class JaxbHelper<T> {

    public static CentralConfigDescriptorImpl readConfig(String configXmlString) {
        URL schemaUrl = CentralConfigDescriptorImpl.class.getClassLoader().getResource("artifactory.xsd");
        if (schemaUrl == null) {
            throw new RuntimeException("Cannot load artifactory.xsd schema file from classpath.\n" +
                    "Please make sure the artifactory.war contains it.");
        }
        CentralConfigDescriptorImpl descriptor =
                new JaxbHelper<CentralConfigDescriptorImpl>().read(new ByteArrayInputStream(
                        configXmlString.getBytes(Charsets.UTF_8)),
                        CentralConfigDescriptorImpl.class, schemaUrl);
        return descriptor;
    }

    public static void writeConfig(CentralConfigDescriptor descriptor, File configFile) {
        try {
            PrintStream ps = new PrintStream(configFile, Charsets.UTF_8.name());
            new JaxbHelper<CentralConfigDescriptor>().write(ps, descriptor);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File " + configFile.getAbsolutePath() + " cannot be written to!", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toXml(CentralConfigDescriptor descriptor) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new JaxbHelper<CentralConfigDescriptor>().write(bos, descriptor);
            return bos.toString("utf-8");
        } catch (IOException e) {
            throw new RuntimeException("Configuration could be converted to string!", e);
        }
    }

    public void write(OutputStream stream, T object) {
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            XmlSchema schemaAnnotation = object.getClass().getPackage().getAnnotation(XmlSchema.class);
            if (schemaAnnotation != null && schemaAnnotation.location() != null) {
                marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaAnnotation.location());
                // TODO: May just this ArtifactoryConfigVersion.getCurrent().getXsdLocation());
            }
            marshaller.marshal(object, stream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write object to stream.", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @SuppressWarnings({"unchecked"})
    public T read(InputStream stream, Class clazz, URL schemaUrl) {
        T o = null;
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (schemaUrl != null) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = sf.newSchema(schemaUrl);
                unmarshaller.setSchema(schema);
            }
            o = (T) unmarshaller.unmarshal(stream);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read object from stream.", t);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return o;
    }

    public void generateSchema(final OutputStream stream, final Class<T> clazz, final String namespace) {
        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            context.generateSchema(new SchemaOutputResolver() {
                @Override
                public Result createOutput(String namespaceUri, String suggestedFileName)
                        throws IOException {
                    StreamResult result = new StreamResult(stream);
                    result.setSystemId(namespace);
                    return result;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to write object to stream.", e);
        }
    }
}
