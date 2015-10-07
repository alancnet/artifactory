package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.fs.FileInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("archive")
public class ZipFileNode extends FileNode {
    private String type;

    public ZipFileNode() {
    }

    public ZipFileNode(FileInfo fileInfo,String text) {
        super(fileInfo,text);
        setRepoType("local");
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return "archive";
    }

    @Override
    public void updateNodeData() {
        if (isLocal()) {
            super.updateNodeData();
            super.updateHasChild(true);
        }
    }
}
