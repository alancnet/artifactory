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

package org.artifactory.rest.common;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import org.artifactory.jaxb.JaxbHelper;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A jersey provider to handle Jaxb objects
 *
 * @author Noam Tenne
 */
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
@Provider
public class JaxbProvider extends AbstractMessageReaderWriterProvider<Object> {
    private static final JaxbHelper JAXB_HELPER = new JaxbHelper();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return type.getAnnotation(XmlType.class) != null;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType) {
        return type.getAnnotation(XmlType.class) != null;
    }

    @Override
    public Object readFrom(Class<Object> aClass, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> map, InputStream stream)
            throws IOException, WebApplicationException {
        return JAXB_HELPER.read(stream, aClass, null);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void writeTo(Object o, Class<?> aClass, Type type, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> map, OutputStream stream)
            throws IOException, WebApplicationException {
        JAXB_HELPER.write(stream, o);
    }
}
