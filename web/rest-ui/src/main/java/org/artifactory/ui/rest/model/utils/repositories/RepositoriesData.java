package org.artifactory.ui.rest.model.utils.repositories;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class RepositoriesData extends BaseModel {

    private List<String> repoList;
    private List<RepoKeyType> repoTypesList;

    public RepositoriesData() {
    }

    public RepositoriesData(List<String> repoData) {
        this.repoList = repoData;
    }

    public List<String> getRepoList() {
        return repoList;
    }

    public void setRepoList(List<String> repoList) {
        this.repoList = repoList;
    }

    public List<RepoKeyType> getRepoTypesList() {
        return repoTypesList;
    }

    public void setRepoTypesList(List<RepoKeyType> repoTypesList) {
        this.repoTypesList = repoTypesList;
    }
}
