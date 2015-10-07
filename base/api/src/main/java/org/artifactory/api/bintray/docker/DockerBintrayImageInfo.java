package org.artifactory.api.bintray.docker;

import org.artifactory.fs.FileInfo;

/**
 * @author Shay Yaakov
 */
public class DockerBintrayImageInfo {

    public FileInfo json;
    public FileInfo layer;

    public DockerBintrayImageInfo(FileInfo json, FileInfo layer) {
        this.json = json;
        this.layer = layer;
    }
}
