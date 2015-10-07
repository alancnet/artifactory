package org.artifactory.api.repo;

/**
 * @author Chen Keinan
 */
public class RootNodesFilterResult {
    private boolean allItemNodesCanRead = true;

    public boolean isAllItemNodesCanRead() {
        return allItemNodesCanRead;
    }

    public void setAllItemNodesCanRead(boolean isBrowsableItemsAcceptCanRead) {
        this.allItemNodesCanRead = isBrowsableItemsAcceptCanRead;
    }
}
