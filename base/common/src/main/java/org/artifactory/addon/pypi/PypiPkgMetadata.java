package org.artifactory.addon.pypi;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class PypiPkgMetadata {

    private PypiPkgInfo pypiPkgInfo;
    @Nullable
    public List<String> categories;
    @Nullable
    public List<String> requires;
    @Nullable
    public List<String> provides;
    @Nullable
    public List<String> obsoletes;

    public PypiPkgInfo getPypiPkgInfo() {
        return pypiPkgInfo;
    }

    public void setPypiPkgInfo(PypiPkgInfo pypiPkgInfo) {
        this.pypiPkgInfo = pypiPkgInfo;
    }

    @Nullable
    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(@Nullable List<String> categories) {
        this.categories = categories;
    }

    @Nullable
    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(@Nullable List<String> requires) {
        this.requires = requires;
    }

    @Nullable
    public List<String> getProvides() {
        return provides;
    }

    public void setProvides(@Nullable List<String> provides) {
        this.provides = provides;
    }

    @Nullable
    public List<String> getObsoletes() {
        return obsoletes;
    }

    public void setObsoletes(@Nullable List<String> obsoletes) {
        this.obsoletes = obsoletes;
    }
}
