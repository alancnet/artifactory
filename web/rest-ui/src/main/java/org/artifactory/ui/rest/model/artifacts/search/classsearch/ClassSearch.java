package org.artifactory.ui.rest.model.artifacts.search.classsearch;

import org.artifactory.ui.rest.model.artifacts.search.BaseSearch;

/**
 * @author Chen Keinan
 */
public class ClassSearch extends BaseSearch {

    private String name;
    private String path;
    private boolean searchClassOnly;
    private boolean excludeInnerClasses;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSearchClassOnly() {
        return searchClassOnly;
    }

    public void setSearchClassOnly(boolean searchClassOnly) {
        this.searchClassOnly = searchClassOnly;
    }

    public boolean isExcludeInnerClasses() {
        return excludeInnerClasses;
    }

    public void setExcludeInnerClasses(boolean excludeInnerClasses) {
        this.excludeInnerClasses = excludeInnerClasses;
    }
}