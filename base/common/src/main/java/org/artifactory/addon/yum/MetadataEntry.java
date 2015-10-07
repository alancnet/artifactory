package org.artifactory.addon.yum;

/**
 * @author Chen Keinan
 */
public class MetadataEntry {
    public String name;
    public String flags;
    public String epoch;
    public String version;
    public String release;
    public String pre;

    public MetadataEntry(String epoch, String flags, String name, String pre, String release,
            String version) {
        this.epoch = epoch;
        this.flags = flags;
        this.name = name;
        this.pre = pre;
        this.release = release;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epoch) {
        this.epoch = epoch;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getPre() {
        return pre;
    }

    public void setPre(String pre) {
        this.pre = pre;
    }
}
