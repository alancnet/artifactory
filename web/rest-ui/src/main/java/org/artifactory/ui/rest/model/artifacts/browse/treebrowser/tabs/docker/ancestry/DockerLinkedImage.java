package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.ancestry;

/**
 * @author Chen Keinan
 */
public class DockerLinkedImage {
    private String id;
    private String size;
    private DockerLinkedImage child;
    private String path;

    public DockerLinkedImage(String id, String size, String path) {
        this.id = id;
        this.size = size;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public DockerLinkedImage getChild() {
        return child;
    }

    public void setChild(
            DockerLinkedImage child) {
        this.child = child;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void addChild(DockerLinkedImage dockerLinkedImage) {
        child = dockerLinkedImage;
    }
}
