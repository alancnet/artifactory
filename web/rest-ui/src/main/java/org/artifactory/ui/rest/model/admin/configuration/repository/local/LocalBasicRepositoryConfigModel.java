package org.artifactory.ui.rest.model.admin.configuration.repository.local;

import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;

import static org.artifactory.ui.rest.model.admin.configuration.repository.RepoConfigDefaultValues.DEFAULT_INCLUDES_PATTERN;

/**
 * @author Dan Feldman
 * @author Aviad Shikloshi
 */
public class LocalBasicRepositoryConfigModel implements BasicRepositoryConfigModel {

    protected String publicDescription;
    protected String internalDescription;
    protected String includesPattern = DEFAULT_INCLUDES_PATTERN;
    protected String excludesPattern;
    protected String layout;

    @Override
    public String getPublicDescription() {
        return publicDescription;
    }

    @Override
    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    @Override
    public String getInternalDescription() {
        return internalDescription;
    }

    @Override
    public void setInternalDescription(String internalDescription) {
        this.internalDescription = internalDescription;
    }

    @Override
    public String getIncludesPattern() {
        return includesPattern;
    }

    @Override
    public void setIncludesPattern(String includesPattern) {
        this.includesPattern = includesPattern;
    }

    @Override
    public String getExcludesPattern() {
        return excludesPattern;
    }

    @Override
    public void setExcludesPattern(String excludesPattern) {
        this.excludesPattern = excludesPattern;
    }

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
    public void setLayout(String layout) {
        this.layout = layout;
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
