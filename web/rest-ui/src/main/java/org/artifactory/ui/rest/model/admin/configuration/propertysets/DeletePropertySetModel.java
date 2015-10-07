package org.artifactory.ui.rest.model.admin.configuration.propertysets;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeletePropertySetModel implements RestModel {
    private List<String> propertySetNames = Lists.newArrayList();

    public List<String> getPropertySetNames(){
        return propertySetNames;
    }
    public void addPropertySet(String propertyName) {
        propertySetNames.add(propertyName);
    }
}