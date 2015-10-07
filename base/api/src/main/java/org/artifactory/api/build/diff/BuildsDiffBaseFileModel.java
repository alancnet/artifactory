/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.api.build.diff;

import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Builds diff base file model object for artifacts or dependencies model
 * Holds file related fields such as sha1, md5 etc.
 *
 * @author Shay Yaakov
 */
public abstract class BuildsDiffBaseFileModel implements Serializable {

    protected String name;
    protected String diffName;
    private String module;
    private String type;
    private String sha1;
    private String md5;
    private String uri;

    @JsonIgnore
    private RepoPath repoPath;

    @JsonIgnore
    private BuildsDiffStatus status;

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getDiffName();

    public abstract void setDiffName(String diffName);

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(RepoPath repoPath) {
        this.repoPath = repoPath;
    }

    public BuildsDiffStatus getStatus() {
        return status;
    }

    public void setStatus(BuildsDiffStatus status) {
        this.status = status;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
