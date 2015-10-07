package org.artifactory.addon.npm;

/**
 * @author Chen Keinan
 */
public class NpmDependency {

    private String name;
    private String version;

    public NpmDependency(String key, String value) {
        this.name = key;
        this.version = value;
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
}
