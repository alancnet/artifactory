package org.artifactory.ui.rest.model.artifacts.search.classsearch;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.archive.ArchiveSearchResult;
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
@JsonTypeName("class")
public class ClassSearchResult extends BaseSearchResult {

    private String archiveName;
    private String archivePath;

    public ClassSearchResult() {
    }

    public ClassSearchResult(ArchiveSearchResult archiveSearchResult) {
        super.setRepoKey(archiveSearchResult.getRepoKey());
        super.setName(archiveSearchResult.getEntryPath());
        super.setModifiedDate(archiveSearchResult.getLastModified());
        super.setModifiedString(archiveSearchResult.getLastModifiedString());
        archiveName = archiveSearchResult.getItemInfo().getName();
        archivePath = archiveSearchResult.getItemInfo().getRelPath();
        RepoPath repoPath = InfoFactoryHolder.get().createRepoPath(archiveSearchResult.getRepoKey(),
                archiveSearchResult.getRelativePath());
        this.repoPath = repoPath;
        updateActions();
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public String getArchivePath() {
        String path = archivePath.replaceAll(archiveName, "");
        return StringUtils.isBlank(path) ? "[root]" : path;
    }

    public void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    @Override
    protected void updateActions() {
        super.updateActions();
        getActions().remove("Delete");
    }

    @Override
    public ItemSearchResult getSearchResult() {
        RepoPath repoPath = InternalRepoPathFactory.create(getRepoKey(), archivePath);
        ItemInfo itemInfo;
        try {
            itemInfo = ContextHelper.get().getRepositoryService().getItemInfo(repoPath);
        } catch (ItemNotFoundRuntimeException e) {
            itemInfo = getItemInfo(repoPath);
        }
        return new ArtifactSearchResult(itemInfo);
    }
}
