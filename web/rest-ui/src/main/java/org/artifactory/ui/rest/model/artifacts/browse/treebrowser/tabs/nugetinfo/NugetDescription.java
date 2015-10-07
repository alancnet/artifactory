package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.nugetinfo;

import org.artifactory.nuget.NuMetaData;

/**
 * @author Chen Keinan
 */
public class NugetDescription {

    private String description;

    public NugetDescription(NuMetaData nuMetaData) {
        this.description = nuMetaData.getDescription();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
