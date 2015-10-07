package org.artifactory.ui.rest.model.artifacts.search.propertysearch;

import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class PropertySearch extends BaseSearch {

    private List<PropertyKeyValues> propertyKeyValues;

    public List<PropertyKeyValues> getPropertyKeyValues() {
        return propertyKeyValues;
    }

    public void setPropertyKeyValues(
            List<PropertyKeyValues> propertyKeyValues) {
        this.propertyKeyValues = propertyKeyValues;
    }
}
