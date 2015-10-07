package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author Shay Yaakov
 */
public class DockerImageMetadata implements Serializable {

    @JsonProperty("id")
    public String id;
    @JsonProperty("parent")
    public String parent;
    @JsonProperty("created")
    public String created;
    @JsonProperty("container")
    public String container;
    @JsonProperty("docker_version")
    public String dockerVersion;
    @JsonProperty("author")
    public String author;
    @JsonProperty("container_config")
    public Config config;
    @JsonProperty("architecture")
    public String architecture;
    @JsonProperty("os")
    public String os;
    @JsonProperty("Size")
    public long size;

    public static class Config implements Serializable {
        @JsonProperty("Hostname")
        public String hostname;
        @JsonProperty("Domainname")
        public String domainname;
        @JsonProperty("User")
        public String user;
        @JsonProperty("Memory")
        public long memory;
        @JsonProperty("MemorySwap")
        public long memorySwap;
        @JsonProperty("CpuShares")
        public long cpuShares;
        @JsonProperty("CpuSet")
        public String cpuSet;
        @JsonProperty("AttachStdin")
        public boolean attachStdin;
        @JsonProperty("AttachStdout")
        public boolean attachStdout;
        @JsonProperty("AttachStderr")
        public boolean attachStderr;
        @JsonProperty("PortSpecs")
        public List<String> portSpecs;
        @JsonProperty("ExposedPorts")
        public JsonNode exposedPorts;
        @JsonProperty("Tty")
        public boolean tty;
        @JsonProperty("OpenStdin")
        public boolean openStdin;
        @JsonProperty("StdinOnce")
        public boolean stdinOnce;
        @JsonProperty("Env")
        public List<String> env;
        @JsonProperty("Cmd")
        public List<String> cmd;
        @JsonProperty("Image")
        public String image;
        @JsonProperty("Volumes")
        public JsonNode volumes;
        @JsonProperty("WorkingDir")
        public String workingDir;
        @JsonProperty("Entrypoint")
        public List<String> entrypoint;
        @JsonProperty("NetworkDisabled")
        public boolean networkDisabled;
        @JsonProperty("OnBuild")
        public List<String> onBuild;
    }
}
