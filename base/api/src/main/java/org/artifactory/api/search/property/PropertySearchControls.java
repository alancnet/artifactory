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

package org.artifactory.api.search.property;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multiset;
import org.artifactory.api.search.SearchControlsBase;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Holds the property search parameters
 *
 * @author Noam Tenne
 */
public class PropertySearchControls extends SearchControlsBase {

    //Store property key and value
    private LinkedHashMultimap<String, String> properties;

    //Store property key and openness indication
    private LinkedHashMultimap<Boolean, String> propertyOpenIndication;

    //Openness statuses
    public static final boolean OPEN = true;
    public static final boolean CLOSED = false;

    /**
     * Default constructor
     */
    public PropertySearchControls() {
        properties = LinkedHashMultimap.create();
        propertyOpenIndication = LinkedHashMultimap.create();
    }

    /**
     * Copy constructor
     *
     * @param propertySearchControls Controls to copy
     */
    public PropertySearchControls(PropertySearchControls propertySearchControls) {
        this.properties = propertySearchControls.properties;
        this.propertyOpenIndication = propertySearchControls.propertyOpenIndication;
        this.selectedRepoForSearch = propertySearchControls.selectedRepoForSearch;
        setLimitSearchResults(propertySearchControls.isLimitSearchResults());
    }

    public LinkedHashMultimap<String, String> getProperties() {
        return properties;
    }

    public Set<String> get(String key) {
        return properties.get(key);
    }

    public boolean put(String key, String value, boolean isPropertyOpen) {
        boolean propertyAdded = properties.put(key, value);
        boolean indicationAdded = propertyOpenIndication.put(isPropertyOpen, key);
        return propertyAdded && indicationAdded;
    }

    public void removeAll(String key) {
        //Check if the openness indication contains the key with either open or closed
        if (propertyOpenIndication.containsEntry(OPEN, key)) {
            propertyOpenIndication.remove(OPEN, key);
        } else if (propertyOpenIndication.containsEntry(CLOSED, key)) {
            propertyOpenIndication.remove(CLOSED, key);
        }
        properties.removeAll(key);
    }

    public Collection<String> values() {
        return properties.values();
    }

    public Set<Map.Entry<String, String>> entries() {
        return properties.entries();
    }

    public Multiset<String> keys() {
        return properties.keys();
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public boolean isWildcardsOnly() {
        if (isEmpty()) {
            return true;
        }
        for (String key : properties.keys()) {
            if (isWildcardsOnly(key)) {
                // one key that contains wildcards is enough
                return true;
            }
        }
        return false;
    }


    public boolean containsEntry(String key, String value) {
        return properties.containsEntry(key, value);
    }

    /**
     * Creates a string that represents the search query: " PROPERTY1 = { VALUE1; VALUE2}, PROPERTY2 = {VALUE3} "
     *
     * @return Query string
     */
    public String getValue() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> keyIterator = properties.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            builder.append(key).append("=");
            builder.append("{");
            Iterator<String> valueIterator = properties.get(key).iterator();
            while (valueIterator.hasNext()) {
                String value = valueIterator.next();
                builder.append(value);
                if (valueIterator.hasNext()) {
                    builder.append(";");
                }
            }
            builder.append("}");
            if (keyIterator.hasNext()) {
                builder.append(" , ");
            }
        }

        return builder.toString();
    }

    /**
     * Returns a boolean representation of the properties map state
     *
     * @return True if the map is initialized. False if not.
     */
    public boolean isMapInitialized() {
        return properties != null;
    }

    /**
     * Returns all property keys that have the given openness specification
     *
     * @param openness Should return keys of open or closed properties
     * @return Set of property keys
     */
    public Set<String> getPropertyKeysByOpenness(boolean openness) {
        return propertyOpenIndication.get(openness);
    }
}