package org.artifactory.ui.rest.model.artifacts.search.propertysearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.api.search.property.PropertySearchResult;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("property")
public class PropertyResult extends BaseSearchResult {

    private String relativePath;
    private String relativeDirPath;
    private String resultType;

    public PropertyResult() {
    }

    public PropertyResult(PropertySearchResult propertyResult) {
        super.setRepoKey(propertyResult.getRepoKey());
        this.relativePath = propertyResult.getRelativePath();
        String relDirPath = propertyResult.getRelDirPath();
        if (StringUtils.isBlank(relDirPath)) {
            relDirPath = StringUtils.equals(propertyResult.getName(), ".") ? "[repo]" : "[root]";
        }
        ItemInfo itemInfo = propertyResult.getItemInfo();
        if (!itemInfo.isFolder()) {
            this.resultType = "File"; //file
        } else if (".".equals(itemInfo.getRepoPath().getPath())) {
            this.resultType = "Repository"; //repository
        } else {
            this.resultType = "Directory"; //directory
        }
        this.relativeDirPath = relDirPath;
        super.setName(StringUtils.equals(propertyResult.getName(), ".") ? propertyResult.getRepoKey() : propertyResult.getName());
        super.setModifiedDate(itemInfo.getLastModified());
        super.setModifiedDate(itemInfo.getLastModified());
        super.setModifiedString(ContextHelper.get().getCentralConfig().format(getModifiedDate()));
        this.repoPath = InfoFactoryHolder.get().createRepoPath(propertyResult.getRepoKey(),
                propertyResult.getRelativePath(), itemInfo instanceof FolderInfo);
        updateActions();
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

    @Override
    protected void updateActions() {
        super.updateActions();
        if (resultType.equals("Directory") || resultType.equals("Repository")) {
            getActions().remove("Download");
            if (resultType.equals("Repository")) {
                getActions().remove("Delete");
            }
        }
    }

    @Override
    public ItemSearchResult getSearchResult() {
        ItemInfo itemInfo;
        RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), getRelativePath());
        try {
            itemInfo = ContextHelper.get().getRepositoryService().getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            itemInfo = getItemInfo(repoPath);
        }
        return new ArtifactSearchResult(itemInfo);
    }
}
