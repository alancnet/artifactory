package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadArtifact;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.IAction;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.IArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.GeneralArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.ArchiveInfo;
import org.artifactory.util.PathUtils;
import org.artifactory.util.TreeNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Chen Keinan
 */
public class ArchiveTreeNode implements Serializable, Comparable<ArchiveTreeNode>, RestModel {
    private String tempPath;
    private boolean directory;
    private ZipEntryInfo zipEntry;
    private Set<RestModel> children;
    private String name;
    private String text;
    private String repoKey;
    private String archivePath;
    private String downloadPath;
    private String path;
    private String mimeType;

    @JsonIgnore
    public boolean isDirectory() {
        return directory;
    }

    public void setZipEntry(ZipEntryInfo zipEntry) {
        this.zipEntry = zipEntry;
    }

    public ArchiveTreeNode(String entryPath, boolean directory, String text, String archive) {
        this.tempPath = entryPath;
        this.archivePath = archive;
        this.directory=directory;
        name = PathUtils.getFileName(entryPath);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getPath() {
        if ((!zipEntry.isDirectory() && !isDirectory())
                || (zipEntry.isDirectory() && isDirectory())) {
            return archivePath + "/" + zipEntry.getPath();
        } else {
            return archivePath + "/" + tempPath;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDownloadPath() {
        if (!hasChildren()) {
            return archivePath + "!" + "/" + zipEntry.getPath();
        } else {
            return null;
        }
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setText(String text) {
        this.text = text;
    }

     @JsonIgnore
    public ZipEntryInfo getData() {
        return zipEntry;    }

    public List<IArtifactInfo> getTabs(){
        GeneralArtifactInfo artifactInfo = new GeneralArtifactInfo("General");
        ArchiveInfo archiveInfo = new ArchiveInfo(zipEntry, this);
        artifactInfo.setInfo(archiveInfo);
        List<IArtifactInfo> archiveInfoList =  new ArrayList<>();
        archiveInfoList.add(artifactInfo);
        if (NamingUtils.isViewable(getTempPath()) || "class".equals(PathUtils.getExtension(getTempPath()))) {
            archiveInfoList.add(new BaseArtifactInfo("ViewSource"));
        }
        return archiveInfoList;
    }

    public String getType() {
        return isDirectory() ? "folder" : "file";
    }

    public List<IAction> getActions(){
        List<IAction> actions = null;
        if (!isDirectory()) {
            actions = new ArrayList<>();
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            if (!(addonsManager instanceof OssAddonsManager)) {
                actions.add(new DownloadArtifact("Download"));
            }
        }
        return actions;
    }

    public Set<RestModel> getChildren() {
        return children;
    }

    public void setChildren(
            Set<RestModel> children) {
        this.children = children;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    public boolean hasChildren() {
       return children != null && !children.isEmpty();
    }

     @JsonProperty("folder")
    public boolean isLeaf() {
        return hasChildren();
    }

     public TreeNode<ZipEntryInfo> getChild(ZipEntryInfo data) {
        return null;
    }

    @JsonIgnore
    public String getTempPath() {
        return tempPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArchiveTreeNode that = (ArchiveTreeNode) o;

        if (!zipEntry.equals(that.zipEntry)) {
            return false;
        }
        return true;
    }

    public String getMimeType() {
        if (NamingUtils.isViewable(getTempPath()) || "class".equals(PathUtils.getExtension(getTempPath()))) {
            return NamingUtils.getMimeType(getTempPath()).getType();
        } else {
            return null;
        }
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    RestModel getChild(String relativePath) {
        if (children != null) {
            for (RestModel child : children) {
                if (((ArchiveTreeNode)child).getName().equals(relativePath)) {
                    return child;
                }
            }
        }
        return null;
    }

    public void addChild(RestModel child) {
        if (!directory) {
            throw new IllegalStateException("Cannot add children to a leaf node");
        }
        if (children == null) {
            children = new TreeSet<>();
        }
        children.add(child);
    }
    private String getName(){
        return name;
    }
    @Override
    public int hashCode() {
        return zipEntry.hashCode();
    }

    @Override
    public int compareTo(ArchiveTreeNode o) {
        if (o.isDirectory() && !isDirectory()) {
            return 1;
        }
        if (!o.isDirectory() && isDirectory()) {
            return -1;
        }
        return getName().compareTo(o.getName());
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }
    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
