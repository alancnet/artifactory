package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties;

import org.artifactory.descriptor.property.Property;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties(value = {"closedPredefinedValues","multipleChoice","predefinedValues","valueCount","formattedValues","propertyType"})
public class RepoProperty extends Property {

    public RepoProperty (String name){
        super.setName(name);
    }

    public RepoProperty(){}

}
