package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class DockerConfig {

    private String hostname;
    private String domainName;
    private String user;
    private String memory;
    private String memorySwap;
    private String cpuShares;
    private String cpuSet;
    private String attachStdin;
    private boolean attachStdout;
    private String attachStderr;
    private String portSpecs;
    private String exposedPorts;
    private String tty;
    private String openStdin;
    private String stdinOnce;
    private String env;
    private String cmd;
    private String image;
    private String volumes;
    private String workingDir;
    private List<String> entryPoint;
    private String networkDisabled;
    private String onBuild;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getMemorySwap() {
        return memorySwap;
    }

    public void setMemorySwap(String memorySwap) {
        this.memorySwap = memorySwap;
    }

    public String getVolumes() {
        return volumes;
    }

    public String getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(String cpuShares) {
        this.cpuShares = cpuShares;
    }

    public String getCpuSet() {
        return cpuSet;
    }

    public void setCpuSet(String cpuSet) {
        this.cpuSet = cpuSet;
    }

    public String getAttachStdin() {
        return attachStdin;
    }

    public void setAttachStdin(String attachStdin) {
        this.attachStdin = attachStdin;
    }

    public String getAttachStderr() {
        return attachStderr;
    }

    public void setAttachStderr(String attachStderr) {
        this.attachStderr = attachStderr;
    }

    public boolean isAttachStdout() {
        return attachStdout;
    }

    public void setAttachStdout(boolean attachStdout) {
        this.attachStdout = attachStdout;
    }

    public String getPortSpecs() {
        return portSpecs;
    }

    public void setPortSpecs(String portSpecs) {
        this.portSpecs = portSpecs;
    }

    public String getExposedPorts() {
        return exposedPorts;
    }

    public void setExposedPorts(String exposedPorts) {
        this.exposedPorts = exposedPorts;
    }

    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }

    public String getTty() {
        return tty;
    }

    public void setTty(String tty) {
        this.tty = tty;
    }

    public String getOpenStdin() {
        return openStdin;
    }

    public void setOpenStdin(String openStdin) {
        this.openStdin = openStdin;
    }

    public String getStdinOnce() {
        return stdinOnce;
    }

    public void setStdinOnce(String stdinOnce) {
        this.stdinOnce = stdinOnce;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public List<String> getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(List<String> entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getNetworkDisabled() {
        return networkDisabled;
    }

    public void setNetworkDisabled(String networkDisabled) {
        this.networkDisabled = networkDisabled;
    }

    public String getOnBuild() {
        return onBuild;
    }

    public void setOnBuild(String onBuild) {
        this.onBuild = onBuild;
    }
}
