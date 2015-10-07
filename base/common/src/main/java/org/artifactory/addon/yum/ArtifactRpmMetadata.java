package org.artifactory.addon.yum;

import java.io.File;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class ArtifactRpmMetadata {

    private GeneralRpmMetadata generalRpmMetadata;
    private MiscRpmMetadata miscRpmMetadata;
    private String description;
    private List<MetadataEntry> provide;
    private List<MetadataEntry> require;
    private List<MetadataEntry> conflict;
    private List<MetadataEntry> obsolete;
    private List<File> files;
    private List<MetadataChangeLog> changeLogs;


    public GeneralRpmMetadata getGeneralRpmMetadata() {
        return generalRpmMetadata;
    }

    public void setGeneralRpmMetadata(GeneralRpmMetadata generalRpmMetadata) {
        this.generalRpmMetadata = generalRpmMetadata;
    }

    public MiscRpmMetadata getMiscRpmMetadata() {
        return miscRpmMetadata;
    }

    public void setMiscRpmMetadata(MiscRpmMetadata miscRpmMetadata) {
        this.miscRpmMetadata = miscRpmMetadata;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MetadataEntry> getProvide() {
        return provide;
    }

    public void setProvide(List<MetadataEntry> provide) {
        this.provide = provide;
    }

    public List<MetadataEntry> getRequire() {
        return require;
    }

    public void setRequire(List<MetadataEntry> require) {
        this.require = require;
    }

    public List<MetadataEntry> getConflict() {
        return conflict;
    }

    public void setConflict(List<MetadataEntry> conflict) {
        this.conflict = conflict;
    }

    public List<MetadataEntry> getObsolete() {
        return obsolete;
    }

    public void setObsolete(List<MetadataEntry> obsolete) {
        this.obsolete = obsolete;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<MetadataChangeLog> getChangeLogs() {
        return changeLogs;
    }

    public void setChangeLogs(List<MetadataChangeLog> changeLogs) {
        this.changeLogs = changeLogs;
    }
}
