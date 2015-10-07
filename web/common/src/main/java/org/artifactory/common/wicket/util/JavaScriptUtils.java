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

package org.artifactory.common.wicket.util;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.Application;
import org.apache.wicket.javascript.IJavaScriptCompressor;
import org.artifactory.api.jackson.JacksonFactory;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Yoav Aharoni
 */
public abstract class JavaScriptUtils {
    private JavaScriptUtils() {
        // utility class
    }

    public static String jsFunctionCall(String functionName, Object... parameters) {
        return functionName + "(" + jsParams(parameters) + ")";
    }

    public static String jsParam(Object parameter) {
        Writer writer = new StringWriter();
        try {
            JsonGenerator generator = JacksonFactory.createJsonGenerator(writer);
            generator.writeObject(parameter);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not write js param '" + parameter + "'.", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public static String jsParams(Object... parameters) {
        StringBuilder result = new StringBuilder();
        for (Object parameter : parameters) {
            result.append(',');
            String jsParameter = jsParam(parameter);
            result.append(jsParameter);
        }
        return result.substring(1);
    }

    public static String compress(String javascript) {
        IJavaScriptCompressor compressor = Application.get().getResourceSettings().getJavaScriptCompressor();
        return compressor == null ? javascript : compressor.compress(javascript);
    }
}
