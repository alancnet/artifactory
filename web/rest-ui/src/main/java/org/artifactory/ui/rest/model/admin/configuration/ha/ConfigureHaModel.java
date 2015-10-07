package org.artifactory.ui.rest.model.admin.configuration.ha;

/**
 * @author Chen keinan
 */
public class ConfigureHaModel {

    private String jfrogWikiLink = "http://www.jfrog.com/confluence/";
    private String configureLink = "http://www.jfrog.com/confluence/display/RTF/Installation+and+Setup";

    public String getJfrogWikiLink() {
        return jfrogWikiLink;
    }

    public void setJfrogWikiLink(String jfrogWikiLink) {
        this.jfrogWikiLink = jfrogWikiLink;
    }

    public String getConfigureLink() {
        return configureLink;
    }

    public void setConfigureLink(String configureLink) {
        this.configureLink = configureLink;
    }
}

