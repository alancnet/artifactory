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

package org.artifactory.rest.common.list;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.property.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class uses a <a href="http://www.regular-expressions.info/lookaround.html">Negative Lookbehind</a> regexp.
 * <p>All the regexp are used instead of regular String split since we want to allow
 * pipe, comma or equal as regular characters inside the key/value (by escaping them with a backslash).<br/>
 * For example: "a\=a=1\,2" allows for key: a=a and value: 1,2 (one value instead of multiple values as comma usually means).
 * <p/>
 * <p><b>NOTE!</b> Since Jackrabbit doesn't allow pipes as the key/value name
 * we use the pipe regexp pattern only for handling values, in case a key with a pipe is given the user will get 500.
 *
 * @author Tomer Cohen
 */
public class KeyValueList extends ArrayList<String> {
    private static final Logger log = LoggerFactory.getLogger(KeyValueList.class);

    private static final Pattern BACKSLASH_PIPE_PATTERN = Pattern.compile("(?<!\\\\)\\|");
    private static final Pattern BACKSLASH_COMMA_PATTERN = Pattern.compile("(?<!\\\\)\\,");
    private static final Pattern BACKSLASH_EQUAL_PATTERN = Pattern.compile("(?<!\\\\)=");

    public KeyValueList(String s) {
        super();
        String[] splittedValues = BACKSLASH_PIPE_PATTERN.split(s);
        for (String v : splittedValues) {
            try {
                if (!StringUtils.isWhitespace(v)) {
                    add(v.trim());
                }
            } catch (Exception ex) {
                log.error("Error while parsing list parameter '{}': {}.", s, ex.getMessage());
                throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
            }
        }
    }

    public Map<String, List<String>> toStringMap() {
        Map<String, List<String>> map = Maps.newHashMap();
        for (String keyVal : this) {
            String[] split = BACKSLASH_EQUAL_PATTERN.split(keyVal);
            if (split.length == 2) {
                String[] valueSplit = BACKSLASH_COMMA_PATTERN.split(split[1]);
                List<String> values = Lists.newArrayList();
                for (String s : valueSplit) {
                    values.add(replaceBackslashes(s));
                }
                map.put(replaceBackslashes(split[0]), values);
            }
        }
        return map;
    }

    public Map<Property, List<String>> toPropertyMap() {
        Map<Property, List<String>> map = Maps.newHashMap();
        for (String keyVal : this) {
            String[] split = BACKSLASH_EQUAL_PATTERN.split(keyVal);
            String key = replaceBackslashes(split[0]);
            if (split.length == 2) {
                Property propertyDescriptor = new Property();
                propertyDescriptor.setName(key);
                String[] valueSplit = BACKSLASH_COMMA_PATTERN.split(split[1]);
                List<String> values = Lists.newArrayList();
                for (String s : valueSplit) {
                    values.add(replaceBackslashes(s));
                }
                map.put(propertyDescriptor, values);
            } else if (split.length == 1) {
                //Empty value
                Property propertyDescriptor = new Property();
                propertyDescriptor.setName(key);
                map.put(propertyDescriptor, Lists.newArrayList(""));
            }
        }
        return map;
    }

    private String replaceBackslashes(String s) {
        return StringUtils.replaceEach(s, new String[]{"\\,", "\\=", "\\|", "\\"}, new String[]{",", "=", "|", "\\"});
    }
}
