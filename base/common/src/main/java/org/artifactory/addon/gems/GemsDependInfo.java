package org.artifactory.addon.gems;

/**
 * @author Chen Keinan
 */
public class GemsDependInfo {
    private String name;
    private String requirements;

    public GemsDependInfo(String name, String requirements) {
        this.name = name;
        this.requirements = requirements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}
