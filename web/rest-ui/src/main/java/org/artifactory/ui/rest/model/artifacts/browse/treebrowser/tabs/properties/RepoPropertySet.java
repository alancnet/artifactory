package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.descriptor.property.PropertySet;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties(value = {"properties","visible"})
public class RepoPropertySet extends PropertySet {

    public RepoPropertySet(String name){
        super.setName(name);
    }

    public RepoPropertySet(){}
}
