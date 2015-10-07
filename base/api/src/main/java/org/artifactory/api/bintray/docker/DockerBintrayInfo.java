package org.artifactory.api.bintray.docker;

import com.google.common.collect.Maps;
import org.artifactory.fs.FileInfo;

import java.util.Map;

/**
 * @author Shay Yaakov
 */
public class DockerBintrayInfo {

    public FileInfo indexImages;
    public Map<String, DockerBintrayImageInfo> images = Maps.newLinkedHashMap();
    public String tagId;
    public String userAgent;
}
