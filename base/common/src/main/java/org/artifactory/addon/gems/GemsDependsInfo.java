package org.artifactory.addon.gems;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class GemsDependsInfo {
    private List<GemsDependInfo> development;
    private List<GemsDependInfo> runtime;

    public GemsDependsInfo() {
    }

    public GemsDependsInfo(List<GemsDependInfo> devDependsInfos, List<GemsDependInfo> runDependsInfos) {
        this.development = devDependsInfos;
        this.runtime = runDependsInfos;
    }

    public List<GemsDependInfo> getDevelopment() {
        return development;
    }

    public void setDevelopment(List<GemsDependInfo> development) {
        this.development = development;
    }

    public List<GemsDependInfo> getRuntime() {
        return runtime;
    }

    public void setRuntime(List<GemsDependInfo> runtime) {
        this.runtime = runtime;
    }
}
