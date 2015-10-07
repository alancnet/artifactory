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

package org.artifactory.factory.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.artifactory.md.Properties;
import org.artifactory.model.xstream.fs.PropertiesImpl;

import java.util.Map;

/**
 * Converts a map of key-vals to the format:
 * <pre>
 * &lt;properties&gt;
 *     &lt;key1&gt;val1&lt;/key1&gt;
 *     &lt;key2&gt;val2&lt;/key2&gt;
 * &lt;/properties&gt;
 * </pre >
 *
 * @author Yoav Landman
 */
public class PropertiesConverter implements Converter {
    @Override
    public boolean canConvert(Class type) {
        return type.equals(PropertiesImpl.class);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Properties map = (Properties) source;
        for (Map.Entry<String, String> entry : map.entries()) {
            //Start a node with the key name
            writer.startNode(entry.getKey());
            //Write the value in
            String val = entry.getValue();
            if (val != null) {
                writer.setValue(val);
            } else {
                writer.startNode("null");
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Properties map = new PropertiesImpl();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String key = reader.getNodeName();
            String val;
            if (reader.hasMoreChildren()) {
                //Handle nulls
                val = null;
            } else {
                val = reader.getValue();
            }
            map.put(key, val);
            reader.moveUp();
        }
        return map;
    }
}