package org.artifactory.ui.rest.model.admin.importexport;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class ImportExportSettings extends BaseModel {

    private String path;
    private boolean excludeMetadata;
    private boolean verbose;
    private boolean excludeContent;
    private String repository;
    private boolean excludeBuilds;
    private boolean m2;
    private boolean createArchive;
    private boolean zip;

    ImportExportSettings() {
    }

    public ImportExportSettings(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean isExcludeMetadata() {
        return excludeMetadata;
    }

    public void setExcludeMetadata(Boolean excludeMetadata) {
        this.excludeMetadata = excludeMetadata;
    }

    public Boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    public Boolean isExcludeContent() {
        return excludeContent;
    }

    public void setExcludeContent(Boolean excludeContent) {
        this.excludeContent = excludeContent;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Boolean isExcluudeBuillds() {
        return excludeBuilds;
    }

    public void setExcludeBuilds(Boolean excludeBuilds) {
        this.excludeBuilds = excludeBuilds;
    }

    public Boolean isCreateM2CompatibleExport() {
        return m2;
    }

    public void setM2(Boolean m2) {
        this.m2 = m2;
    }

    public Boolean isCreateZipArchive() {
        return createArchive;
    }

    public void setCreateArchive(Boolean createArchive) {
        this.createArchive = createArchive;
    }

    public boolean isZip() {
        return zip;
    }

    public void setZip(boolean zip) {
        this.zip = zip;
    }
}
