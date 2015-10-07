package org.artifactory.addon.yum;

/**
 * @author Chen Keinan
 */
public class MiscRpmMetadata {

    private String url;
    private String vendor;
    private String packager;
    private String buildHost;
    private String sourceRpm;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getPackager() {
        return packager;
    }

    public void setPackager(String packager) {
        this.packager = packager;
    }

    public String getBuildHost() {
        return buildHost;
    }

    public void setBuildHost(String buildHost) {
        this.buildHost = buildHost;
    }

    public String getSourceRpm() {
        return sourceRpm;
    }

    public void setSourceRpm(String sourceRpm) {
        this.sourceRpm = sourceRpm;
    }
}
