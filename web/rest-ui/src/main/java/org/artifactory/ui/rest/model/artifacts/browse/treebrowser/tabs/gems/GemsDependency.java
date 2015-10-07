package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.gems;

/**
 * @author Chen Keinan
 */
public class GemsDependency {
    private String name;
    private String version;
    private String type;

    public GemsDependency(String name, String requirements, String type) {
        this.name = name;
        this.version = requirements;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
