package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.viewsource;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;

/**
 * @author Chen Keinan
 */
public class ViewArtifactSource extends BaseArtifactInfo {

    public ViewArtifactSource(String name) {
        super(name);
    }

    public ViewArtifactSource() {
    }

    private String source;
    private String archivePath;
    private String sourcePath;
    private String repoKey;

    public String getArchivePath() {
        return archivePath;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
