package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public class DockerV2Info {

    public DockerTagInfo tagInfo = new DockerTagInfo();
    public List<DockerBlobInfo> blobsInfo = Lists.newArrayList();
}
