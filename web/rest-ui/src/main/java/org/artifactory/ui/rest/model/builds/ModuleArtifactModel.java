package org.artifactory.ui.rest.model.builds;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.api.build.diff.BuildsDiffArtifactModel;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.RestPaging;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.BaseArtifact;
import org.artifactory.util.PathUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Chen Keinan
 */
public class ModuleArtifactModel extends BaseModel implements RestPaging {

    private String repoKey;
    private String path;
    private String name;
    private String type;
    private String repoPath;
    private String downloadLink;
    private List<BaseArtifact> actions = new ArrayList<>();
    private String status;
    private String moduleName;
    private String prevName;

    public ModuleArtifactModel(ModuleArtifact moduleArtifact, String downloadLink) {
        this.repoKey = moduleArtifact.getRepoKey();
        this.path = moduleArtifact.getPath();
        this.name = moduleArtifact.getName();
        this.type = moduleArtifact.getType();
        this.moduleName = moduleArtifact.getModule();
        this.status = moduleArtifact.getStatus();
        updateArtifactActions(downloadLink);
    }

    /**
     * update artifact actions
     *
     * @param downloadLink - artifact download link
     */
    private void updateArtifactActions(String downloadLink) {
        if (StringUtils.isNotBlank(repoKey)) {
            this.downloadLink = downloadLink;
            repoPath = repoKey + ":" + path;
            if (ContextHelper.get().getAuthorizationService().canRead(RepoPathFactory.create(repoKey, path))) {
                actions.add(new BaseArtifact("Download"));
                actions.add(new BaseArtifact("ShowInTree"));
                if (NamingUtils.isViewable(path) || "class".equals(PathUtils.getExtension(getPath()))) {
                    actions.add(new BaseArtifact("View"));
                }
            }
        } else {
            path = "No path found (externally resolved or deleted/overwritten)";
        }
    }


    public ModuleArtifactModel(BuildsDiffArtifactModel artifact) {
        if (artifact.getRepoPath() != null) {
            this.repoKey = artifact.getRepoPath().getRepoKey();
            this.path = artifact.getRepoPath().getPath();
        }
        this.name = artifact.getName();
        this.type = artifact.getType();
        this.moduleName = artifact.getModule();
        updateArtifactNames(artifact);
        this.status = artifact.getStatus().toString();
        if (artifact.getRepoPath() != null){
            this.downloadLink = artifact.getUri();
            repoPath = repoKey + ":" + path;
            if (ContextHelper.get().getAuthorizationService().canRead(RepoPathFactory.create(repoKey, path))) {
                actions.add(new BaseArtifact("Download"));
                actions.add(new BaseArtifact("ShowInTree"));
                if (NamingUtils.isViewable(path) || "class".equals(PathUtils.getExtension(getPath()))) {
                    actions.add(new BaseArtifact("View"));
                }
            }
        }
    }

    /**
     * update current and prev artifact name base on diff status
     *
     * @param moduleArtifact - module artifact as updated from db
     */
    private void updateArtifactNames(BuildsDiffArtifactModel moduleArtifact) {
        if (moduleArtifact != null) {
            String status = moduleArtifact.getStatus().toString();
            switch (status) {
                case "Removed": {
                    prevName = moduleArtifact.getName();
                    name = "";
                }
                break;
                case "Add": {
                    prevName = "";
                }
                break;
                case "Updated": {
                    String diffName = moduleArtifact.getDiffName();
                    if (diffName != null && diffName.length() > 0) {
                        prevName = diffName;
                    } else {
                        prevName = name;
                    }
                }
                break;
                case "Unchanged": {
                    prevName = name;
                }
                break;
            }
        }
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public List<BaseArtifact> getActions() {
        return actions;
    }

    public void setActions(List<BaseArtifact> actions) {
        this.actions = actions;
    }

    public void cleanData() {
        this.repoKey = null;
        this.repoPath = null;
        this.actions = null;
        this.repoPath = null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPrevName() {
        return prevName;
    }

    public void setPrevName(String prevName) {
        this.prevName = prevName;
    }
}
