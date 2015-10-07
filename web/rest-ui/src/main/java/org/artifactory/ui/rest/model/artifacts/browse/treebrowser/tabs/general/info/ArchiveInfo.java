package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info;

import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive.ArchiveTreeNode;

/**
 * @author Chen Keinan
 */
public class ArchiveInfo extends BaseInfo {

    private String path;
    private String size;
    private String compressed;
    private String modificationTime;
    private String crc;
    private String comments;

    public ArchiveInfo(ZipEntryInfo zipData, ArchiveTreeNode archiveTreeNode) {
        if (isRealZipEntry(zipData, archiveTreeNode)) {
            updateInfoFromZipEntryData(zipData);
        } else {
            updateInfoFromArchiveData(archiveTreeNode);
        }
    }

    /**
     * check if entry origin from archive file or re-created during child entry processing
     *
     * @param zipData         - zip data
     * @param archiveTreeNode - archive tree node
     * @return
     */
    private boolean isRealZipEntry(ZipEntryInfo zipData, ArchiveTreeNode archiveTreeNode) {
        return (!zipData.isDirectory() && !archiveTreeNode.isDirectory())
                || (zipData.isDirectory() && archiveTreeNode.isDirectory());
    }

    /**
     * update entry from auto created data during child node process
     *
     * @param archiveTreeNode - archive node
     */
    private void updateInfoFromArchiveData(ArchiveTreeNode archiveTreeNode) {
        super.setName(archiveTreeNode.getText());
        this.size = new Long(0).toString();
        this.path = archiveTreeNode.getTempPath();
        this.compressed = new Long(0).toString();
        this.modificationTime = new Long(0).toString();
        this.crc = new Long(0).toString();
    }

    /**
     * update entry data from real archive entry
     *
     * @param zipData - real zip / archive data
     */
    private void updateInfoFromZipEntryData(ZipEntryInfo zipData) {
        super.setName(zipData.getName());
        this.size = new Long(zipData.getSize()).toString();
        this.path = zipData.getPath();
        this.compressed = new Long(zipData.getCompressedSize()).toString();
        this.modificationTime = new Long(zipData.getTime()).toString();
        this.crc = new Long(zipData.getCrc()).toString();
        this.comments = zipData.getComment();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCompressed() {
        return compressed;
    }

    public void setCompressed(String compressed) {
        this.compressed = compressed;
    }

    public String getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(String modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
