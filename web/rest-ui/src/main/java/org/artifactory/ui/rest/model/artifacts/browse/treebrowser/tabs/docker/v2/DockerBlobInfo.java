package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2;

/**
 * @author Shay Yaakov
 */
public class DockerBlobInfo {

    public String id;
    public String shortId;
    public String digest;
    public String size;
    public String created;
    public String command;
    public String commandText;

    public DockerBlobInfo(String id, String digest, String size, String created) {
        this.id = id;
        this.shortId = id.substring(0, 12);
        this.digest = digest;
        this.size = size;
        this.created = created;
    }
}
