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
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.IOUtils;
import org.artifactory.factory.xstream.XStreamFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * User: freds Date: Aug 12, 2008 Time: 6:46:36 PM
 */
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.WILDCARD})
@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.WILDCARD})
@Provider
public class XStreamAliasProvider extends AbstractMessageReaderWriterProvider<Object> {
    private static final Set<Class> processed = new HashSet<>();
    private static final XStream xstream = XStreamFactory.create();
    private static final String DEFAULT_ENCODING = "utf-8";

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getAnnotation(XStreamAlias.class) != null || isXtreamable(genericType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.getAnnotation(XStreamAlias.class) != null || isXtreamable(genericType);
    }

    @Override
    public Object readFrom(Class<Object> aClass, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, String> map, InputStream stream)
            throws IOException, WebApplicationException {
        String encoding = getCharsetAsString(mediaType);
        XStream xStream = getXStream(aClass);
        InputStreamReader xmlIs = new InputStreamReader(stream, encoding);
        try {
            return xStream.fromXML(xmlIs);
        } finally {
            IOUtils.closeQuietly(xmlIs);
        }
    }

    @Override
    public void writeTo(Object o, Class<?> aClass, Type type, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> map, OutputStream stream)
            throws IOException, WebApplicationException {
        String encoding = getCharsetAsString(mediaType);
        XStream xStream = getXStream(o.getClass());
        xStream.toXML(o, new OutputStreamWriter(stream, encoding));
    }

    protected static String getCharsetAsString(MediaType m) {
        if (m == null) {
            return DEFAULT_ENCODING;
        }
        String result = m.getParameters().get("charset");
        return (result == null) ? DEFAULT_ENCODING : result;
    }

    protected XStream getXStream(Class type) {
        synchronized (processed) {
            if (!processed.contains(type)) {
                xstream.processAnnotations(type);
                processed.add(type);
            }
        }
        return xstream;
    }

    @SuppressWarnings({"unchecked"})
    private boolean isXtreamable(Type genericType) {
        return (genericType instanceof Class && ((Class) genericType).getAnnotation(XStreamAlias.class) != null);
    }
}
