package org.artifactory.ui.rest.model.artifacts.search.checksumsearch;

import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

/**
 * @author Chen Keinan
 */
public class ChecksumSearch extends BaseSearch {

    private String checksum;

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
