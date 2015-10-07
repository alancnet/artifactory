package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.util.JsonUtil;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.*;

/**
 * @author Dan Feldman
 */
public class YumTypeSpecificConfigModel implements TypeSpecificConfigModel {

    //local
    protected Integer metadataFolderDepth = DEFAULT_YUM_METADATA_DEPTH;
    protected String groupFileNames = DEFAULT_YUM_GROUPFILE_NAME;
    protected Boolean autoCalculateYumMetadata = DEFAULT_YUM_AUTO_CALCULATE;
    protected Boolean listRemoteFolderItems = DEFAULT_LIST_REMOTE_ITEMS_SUPPORTED_TYPE;

    public Integer getMetadataFolderDepth() {
        return metadataFolderDepth;
    }

    public void setMetadataFolderDepth(Integer metadataFolderDepth) {
        this.metadataFolderDepth = metadataFolderDepth;
    }

    public String getGroupFileNames() {
        return groupFileNames;
    }

    public void setGroupFileNames(String groupFileNames) {
        this.groupFileNames = groupFileNames;
    }

    public Boolean isAutoCalculateYumMetadata() {
        return autoCalculateYumMetadata;
    }

    public void setAutoCalculateYumMetadata(Boolean autoCalculateYumMetadata) {
        this.autoCalculateYumMetadata = autoCalculateYumMetadata;
    }

    public Boolean isListRemoteFolderItems() {
        return listRemoteFolderItems;
    }

    public void setListRemoteFolderItems(Boolean listRemoteFolderItems) {
        this.listRemoteFolderItems = listRemoteFolderItems;
    }

    @Override
    public RepoType getRepoType() {
        return RepoType.YUM;
    }

    @Override
    public String getUrl() {
        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
