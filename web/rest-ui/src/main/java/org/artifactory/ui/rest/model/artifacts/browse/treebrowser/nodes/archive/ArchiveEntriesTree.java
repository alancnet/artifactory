package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes.archive;

import org.apache.commons.lang.StringUtils;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.rest.common.model.RestModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Keinan
 */
public class ArchiveEntriesTree  {

    ArchiveTreeNode root;

    public ArchiveEntriesTree() {
        this.root = new ArchiveTreeNode("", true, "root", "");
    }

    Map<String,ArchiveTreeNode> treeNodeMap = new HashMap<>();

    public RestModel getRoot() {
        return root;
    }

    public void insert(ZipEntryInfo entry, String repoKey, String archivePath) {
        String[] pathElements = entry.getPath().split("/");
        ArchiveTreeNode parent = root;
        // get or create parent nodes
        StringBuilder pathBuilder = new StringBuilder();
        // iterate and create node parent folder if not exist already
        for (int i = 0; i < pathElements.length - 1; i++) {
            pathBuilder.append(pathElements[i] + "/");
            parent = addNode(parent, pathElements[i], true, pathBuilder.toString(), archivePath, repoKey, entry);
        }
        // create node for current entry
        addNode(parent, pathElements[pathElements.length - 1],
                entry.isDirectory(), entry.getPath(), archivePath, repoKey, entry);
    }

    /**
     * get parent child from map or create new child if needed
     * @param parent - node parent
     * @param pathElement - node path
     * @param directory - if true is a directory
     * @param fullPath - node full path
     * @param archivePath - archive path
     * @param repoKey - repository key
     * @param entry - entry
     * @return
     */
    private ArchiveTreeNode addNode(ArchiveTreeNode parent, String pathElement,
            boolean directory, String fullPath, String archivePath, String repoKey, ZipEntryInfo entry) {
        // get child/ parent ref in map
        ArchiveTreeNode child = treeNodeMap.get(fullPath);
        if (child == null) {
            // create new child
            String path = StringUtils.isNotBlank(parent.getTempPath()) ?
                    parent.getTempPath() + "/" + pathElement : pathElement;
            child = new ArchiveTreeNode(path, directory, pathElement, archivePath);
            child.setRepoKey(repoKey);
            child.setZipEntry(entry);
            parent.addChild(child);
            treeNodeMap.put(fullPath, child);
        }
        return child;
    }
}
