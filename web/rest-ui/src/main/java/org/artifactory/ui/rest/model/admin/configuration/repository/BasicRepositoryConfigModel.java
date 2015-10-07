package org.artifactory.ui.rest.model.admin.configuration.repository;

import org.artifactory.rest.common.model.RestModel;

/**
 * @author Dan Feldman
 */
public interface BasicRepositoryConfigModel extends RestModel {

    void setPublicDescription(String publicDescription);

    String getPublicDescription();

    void setInternalDescription(String internalDescription);

    String getInternalDescription();

    String getIncludesPattern();

    void setIncludesPattern(String includesPattern);

    String getExcludesPattern();

    void setExcludesPattern(String excludesPattern);

    String getLayout();

    void setLayout(String layout);
}
