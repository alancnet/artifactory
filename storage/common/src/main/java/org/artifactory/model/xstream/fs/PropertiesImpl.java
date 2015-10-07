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

package org.artifactory.model.xstream.fs;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang.StringUtils;
import org.artifactory.md.Properties;
import org.artifactory.md.PropertiesInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A map of stringified keys and values, used for storing arbitrary key-value metadata on repository items.
 *
 * @author Yoav Landman
 */
@XStreamAlias(Properties.ROOT)
public class PropertiesImpl implements Properties {
    private static final Logger log = LoggerFactory.getLogger(PropertiesImpl.class);

    private final SetMultimap<String, String> props;

    public PropertiesImpl() {
        props = LinkedHashMultimap.create();
    }

    public PropertiesImpl(PropertiesInfo m) {
        //props = LinkedHashMultimap.create( m.props);
        props = LinkedHashMultimap.create();
        Set<Map.Entry<String, String>> entries = m.entries();
        for (Map.Entry<String, String> entry : entries) {
            props.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int size() {
        return props.size();
    }

    @Override
    @Nullable
    public Set<String> get(@Nonnull String key) {
        return props.get(key);
    }

    @Override
    @Nullable
    public String getFirst(@Nonnull String key) {
        Set<String> propertyValues = props.get(key);
        if (propertyValues != null) {
            Iterator<String> iterator = propertyValues.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        return null;
    }

    @Override
    public boolean putAll(@Nonnull String key, Iterable<? extends String> values) {
        return props.putAll(key, values);
    }

    @Override
    public boolean putAll(Multimap<? extends String, ? extends String> multimap) {
        return props.putAll(multimap);
    }

    @Override
    public Set<? extends String> replaceValues(@Nonnull String key, Iterable<? extends String> values) {
        return props.replaceValues(key, values);
    }

    @Override
    public void clear() {
        props.clear();
    }

    @Override
    public Set<String> removeAll(@Nonnull Object key) {
        return props.removeAll(key);
    }

    @Override
    public boolean put(String key, String value) {
        return props.put(key, value);
    }

    @Override
    public Collection<String> values() {
        return props.values();
    }

    @Override
    public Set<Map.Entry<String, String>> entries() {
        return props.entries();
    }

    @Override
    public Multiset<String> keys() {
        return props.keys();
    }

    @Override
    public Set<String> keySet() {
        return props.keySet();
    }

    @Override
    public boolean isEmpty() {
        return props.isEmpty();
    }

    @Override
    public boolean hasMandatoryProperty() {
        for (String qPropKey : props.keySet()) {
            if (qPropKey != null && qPropKey.endsWith(MANDATORY_SUFFIX)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    @Override
    public MatchResult matchQuery(Properties queryProperties) {
        if (queryProperties == null) {
            return MatchResult.NO_MATCH;
        }
        for (String qPropKey : queryProperties.keySet()) {
            //Hack - need to model query properties together with their control flags
            boolean mandatory = false;
            String propKey = qPropKey;
            if (qPropKey != null && qPropKey.endsWith(MANDATORY_SUFFIX)) {
                mandatory = true;
                propKey = qPropKey.substring(0, qPropKey.length() - MANDATORY_SUFFIX.length());
            }

            //If the key given from the query must exist
            if (mandatory) {

                //If the current properties contain the given key
                if (containsKey(propKey)) {

                    Set<String> queryPropertyValues = clearBlankAndReturnPropertyValues(queryProperties.get(qPropKey));

                    //Only check the current property values if the request property was given with values
                    if (!queryPropertyValues.isEmpty()) {

                        //The given query properties have a value, so we should try to match
                        Set<String> currentPropertyValue = clearBlankAndReturnPropertyValues(get(propKey));
                        if (!queryPropertyValues.equals(currentPropertyValue)) {

                            //The properties don't match
                            return MatchResult.CONFLICT;
                        }
                    }
                } else {
                    //Conflict since the key given from the properties is mandatory and doesn't exist in the current properties
                    return MatchResult.CONFLICT;
                }
            } else {

                Set<String> queryPropertyValues = clearBlankAndReturnPropertyValues(queryProperties.get(qPropKey));

                if (!queryPropertyValues.isEmpty()) {
                    //If the current properties contain the given query property key
                    if (containsKey(propKey)) {

                        //The given query properties have a value, so we should try to match
                        Set<String> currentPropertyValue = clearBlankAndReturnPropertyValues(get(propKey));

                        if (!queryPropertyValues.equals(currentPropertyValue)) {

                            //The properties conflict
                            return MatchResult.CONFLICT;
                        }
                    } else {
                        //The current property doesn't have the given query property, so it does not conflict either
                        return MatchResult.NO_MATCH;
                    }
                }
            }
        }
        return MatchResult.MATCH;
    }

    @Override
    public boolean equals(@Nullable Object that) {
        if (that instanceof PropertiesImpl) {
            PropertiesImpl otherProps = (PropertiesImpl) that;
            return this.props.equals(otherProps.props);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return props.hashCode();
    }

    @Override
    public String toString() {
        return props.toString();
    }

    /**
     * Returns a copy of the given property value set after clearing any blank\null values it might contain
     *
     * @param propertyValues Property value set. Can be null
     * @return Copy of given set without the null\blank values or an Empty set if given a null set
     */
    private Set<String> clearBlankAndReturnPropertyValues(Set<String> propertyValues) {
        Set<String> clearedPropertyValues = Sets.newHashSet();
        if (propertyValues == null) {
            return clearedPropertyValues;
        }

        for (String propertyValue : propertyValues) {
            if (StringUtils.isNotBlank(propertyValue)) {
                clearedPropertyValues.add(propertyValue);
            }
        }

        return clearedPropertyValues;
    }
}