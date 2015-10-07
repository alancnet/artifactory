package org.artifactory.storage.fs.tree.file;

import org.artifactory.storage.fs.tree.ItemNodeFilter;

/**
 * @author Chen Keinan
 */
public class NodeItemFilterHolder {

    private ItemNodeFilter itemNodeFilter;
    private boolean accepted = true;

    public NodeItemFilterHolder(boolean accepted){
        this.accepted= accepted;
    }

    public NodeItemFilterHolder(boolean accepted, ItemNodeFilter itemNodeFilter){
        this.itemNodeFilter = itemNodeFilter;
        this.accepted= accepted;
    }

    public ItemNodeFilter getItemNodeFilter() {
        return itemNodeFilter;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
