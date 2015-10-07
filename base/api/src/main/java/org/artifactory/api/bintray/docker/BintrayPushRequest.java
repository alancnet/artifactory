package org.artifactory.api.bintray.docker;

import org.apache.commons.lang.StringUtils;

/**
 * @author Shay Yaakov
 */
public class BintrayPushRequest {

    public String dockerImage;
    public String dockerRepository;
    public String dockerTagName;
    public String bintraySubject;
    public String bintrayRepo;
    public boolean async;

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
        if (StringUtils.isNotBlank(dockerImage)) {
            if (StringUtils.lastIndexOf(dockerImage, ":") != -1) {
                this.dockerRepository = StringUtils.substringBeforeLast(dockerImage, ":");
                this.dockerTagName = StringUtils.substringAfterLast(dockerImage, ":");
            } else {
                this.dockerRepository = dockerImage;
                this.dockerTagName = "latest";
            }
        }
    }
}
