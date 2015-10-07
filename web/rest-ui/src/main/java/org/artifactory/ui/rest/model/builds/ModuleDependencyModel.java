package org.artifactory.ui.rest.model.builds;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.diff.BuildsDiffDependencyModel;
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
public class ModuleDependencyModel extends BaseModel implements RestPaging {

    private String repoKey;
    private String path;
    private String name;
    private String type;
    private String scope;
    private String repoPath;
    private String downloadLink;
    private List<BaseArtifact> actions = new ArrayList<>();
    private String status;
    private String moduleName;
    private String prevName;


    public ModuleDependencyModel(ModuleDependency moduleDependency, String downloadLink) {
        this.repoKey = moduleDependency.getRepoKey();
        this.path = moduleDependency.getPath();
        this.name = moduleDependency.getName();
        this.type = moduleDependency.getType();
        this.scope = moduleDependency.getScope();
        this.status = moduleDependency.getStatus();
        this.moduleName = moduleDependency.getModule();
        //    updateArtifactNames(moduleDependency);
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

    public ModuleDependencyModel(BuildsDiffDependencyModel dependency) {
        updateRepoKeyAndPath(dependency);
        this.name = dependency.getName();
        this.type = dependency.getType();
        this.moduleName = dependency.getModule();
        updateArtifactNames(dependency);
        this.status = dependency.getStatus().toString();
        updateDependencyAction(dependency);
    }

    /**
     * update repo key and path
     *
     * @param dependency - dependency
     */
    private void updateRepoKeyAndPath(BuildsDiffDependencyModel dependency) {
        if (dependency.getRepoPath() != null) {
            this.repoKey = dependency.getRepoPath().getRepoKey();
            this.path = dependency.getRepoPath().getPath();
        }
    }

    /**
     * update dependency actions
     *
     * @param dependency
     */
    private void updateDependencyAction(BuildsDiffDependencyModel dependency) {
        if (dependency.getRepoPath() != null) {
            this.downloadLink = dependency.getUri();
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


    /**
     * update current and prev artifact name base on diff status
     *
     * @param moduleDependency - module artifact as updated from db
     */
    private void updateArtifactNames(BuildsDiffDependencyModel moduleDependency) {
        if (moduleDependency != null) {
            String status = moduleDependency.getStatus().toString();
            switch (status) {
                case "Removed": {
                    prevName = moduleDependency.getName();
                    name = "";
                }
                break;
                case "Add": {
                    prevName = "";
                }
                break;
                case "Updated": {
                    String diffName = moduleDependency.getDiffName();
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
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
