package org.artifactory.ui.rest.model.artifacts.search;

import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.mime.NamingUtils;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("stash")
public class StashResult extends BaseSearchResult {

    private String relativePath;
    private String relativeDirPath;
    private String resultType;
    private String mimeType;

    public StashResult(PropertySearchResult propertyResult) {
        super.setRepoKey(propertyResult.getRepoKey());

    }

    public StashResult(String name, String relativePath, String repoKey) {
        super.setRepoKey(repoKey);
        setName(name);
        this.relativePath = relativePath;
        this.setRepoKey(repoKey);
        this.mimeType = NamingUtils.getMimeType(relativePath).getType();
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRelativeDirPath() {
        return relativeDirPath;
    }

    public void setRelativeDirPath(String relativeDirPath) {
        this.relativeDirPath = relativeDirPath;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public ItemSearchResult getSearchResult() {
        return null;
    }
}
