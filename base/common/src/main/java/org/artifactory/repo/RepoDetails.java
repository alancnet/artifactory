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

package org.artifactory.repo;

/**
 * An object to hold minimal details for repository provisioning
 *
 * @author Noam Y. Tenne
 */
public class RepoDetails {

    private String key;
    private String description;
    private RepoDetailsType type;
    private String url;
    private String configuration;

    /**
     * Default Constructor
     */
    public RepoDetails() {
    }

    /**
     * Local\virtual repository constructor
     *
     * @param key         Repository key
     * @param description Repository description
     * @param type        Repository type
     * @param url         URL to repository
     */
    public RepoDetails(String key, String description, RepoDetailsType type, String url) {
        this(key, description, type, url, null);
    }

    /**
     * Remote repository constructor
     *
     * @param key           Repository key
     * @param description   Repository description
     * @param type          Repository type
     * @param url           URL to repository
     * @param configuration URL to repository configuration
     */
    public RepoDetails(String key, String description, RepoDetailsType type, String url, String configuration) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.url = url;
        this.configuration = configuration;
    }

    /**
     * Returns the key of the repository
     *
     * @return Repository key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the repository
     *
     * @param key Repository key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the description of the repository
     *
     * @return Repository description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the repository
     *
     * @param description Repository description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the type of the repository
     *
     * @return Repository type
     */
    public RepoDetailsType getType() {
        return type;
    }

    /**
     * Sets the type of the repository
     *
     * @param type Repository type
     */
    public void setType(RepoDetailsType type) {
        this.type = type;
    }

    /**
     * Returns the URL of the repository
     *
     * @return Repository URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the repository
     *
     * @param url Repository URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the configuration URL of the repository
     *
     * @return Repository configuration URL
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration URL of the repository
     *
     * @param configuration Repository configuration URL
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
}