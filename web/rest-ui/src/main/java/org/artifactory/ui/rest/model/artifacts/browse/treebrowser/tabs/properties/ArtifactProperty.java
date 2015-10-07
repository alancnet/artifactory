package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties;

/**
 * Chen Keinan
 */
public class ArtifactProperty  {

    private String name;
    private String value;

    public ArtifactProperty() {
    }

    public ArtifactProperty(String name,String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
