package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.vcs.VcsType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yoav Luft
 */
@XmlType(name = "VcsType", propOrder = {"type", "git"})
public class VcsConfiguration implements Descriptor {

    @XmlElement(name = "type", defaultValue = "git")
    private VcsType type = VcsType.GIT;

    @XmlElement(name = "git")
    private VcsGitConfiguration git = new VcsGitConfiguration();

    public VcsConfiguration() {
    }

    public VcsType getType() {
        return type;
    }

    public void setType(VcsType type) {
        this.type = type;
    }

    public VcsGitConfiguration getGit() {
        return git;
    }

    public void setGit(VcsGitConfiguration git) {
        this.git = git;
    }
}
