package org.artifactory.ui.rest.model.artifacts.search.quicksearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.ui.rest.model.artifacts.search.BaseSearchResult;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("quick")
public class QuickSearchResult extends BaseSearchResult {

    private String relativePath;
    private String relativeDirPath;

    public QuickSearchResult() {
        // for jackson
    }

    public QuickSearchResult(ArtifactSearchResult artifactSearchResult) {
        super.setModifiedDate(artifactSearchResult.getLastModified());
        super.setModifiedString(artifactSearchResult.getLastModifiedString());
        this.relativePath = artifactSearchResult.getRelativePath();
        String relDirPath = artifactSearchResult.getRelDirPath();
        if (StringUtils.isBlank(relDirPath)) {
            relDirPath = "[root]";
        }
        this.relativeDirPath = relDirPath;
        super.setRepoKey(artifactSearchResult.getRepoKey());
        super.setName(artifactSearchResult.getName());
        RepoPath repoPath = InfoFactoryHolder.get().createRepoPath(artifactSearchResult.getRepoKey(),
                artifactSearchResult.getRelativePath());
        this.repoPath = repoPath;
        super.updateActions();
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
