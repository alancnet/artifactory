package org.artifactory.ui.rest.model.admin.configuration.layouts;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Lior Hasson
 */
public class RegExResolverModel extends BaseModel {
    private String artifactRegEx;
    private String descriptorRegEx;

    public RegExResolverModel(String artifactRegEx, String descriptorRegEx) {
        this.artifactRegEx = artifactRegEx;
        this.descriptorRegEx = descriptorRegEx;
    }

    public String getArtifactRegEx() {
        return artifactRegEx;
    }

    public void setArtifactRegEx(String artifactRegEx) {
        this.artifactRegEx = artifactRegEx;
    }

    public String getDescriptorRegEx() {
        return descriptorRegEx;
    }

    public void setDescriptorRegEx(String descriptorRegEx) {
        this.descriptorRegEx = descriptorRegEx;
    }
}
