package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Shay Yaakov
 */
public class DockerTagInfo {

    public String title;
    public String digest;
    public String totalSize;
    public Set<String> ports = Sets.newHashSet();
    public Set<String> volumes = Sets.newHashSet();
}