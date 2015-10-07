package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker;

/**
 * @author Chen Keinan
 */
public class DockerInfo {

    private String imageId;
    private String imageIdPath;
    private String parent;
    private String parentIdPath;
    private String created;
    private String container;
    private String dockerVersion;
    private String author;
    private String architecture;
    private String os;
    private String size;

    public String getImageId() {

        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public void setDockerVersion(String dockerVersion) {
        this.dockerVersion = dockerVersion;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImageIdPath() {
        return imageIdPath;
    }

    public void setImageIdPath(String imageIdPath) {
        this.imageIdPath = imageIdPath;
    }

    public String getParentIdPath() {
        return parentIdPath;
    }

    public void setParentIdPath(String parentIdPath) {
        this.parentIdPath = parentIdPath;
    }
}
