package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeletePropertyModel extends BaseModel {
    private List<PropertyWithPath> properties= Lists.newArrayList();

    public List<PropertyWithPath> getProperties() {
        return properties;
    }
}
