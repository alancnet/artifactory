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

package org.artifactory.api.version;

import static org.artifactory.api.version.VersionInfoService.SERVICE_UNAVAILABLE;
import static org.artifactory.api.version.VersionInfoService.WIKI_DEFAULT;

/**
 * An object used to hold version and revision information for the VersionInfoService
 *
 * @author Noam Tenne
 */
public class VersionHolder {

    public static final VersionHolder VERSION_UNAVAILABLE = new VersionHolder(
            SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE, WIKI_DEFAULT, SERVICE_UNAVAILABLE);

    /**
     * Version number
     */
    private String version;
    /**
     * Revision number
     */
    private String revision;
    /**
     * The url of the version wiki
     */
    private String wikiUrl;
    /**
     * The url of the version download
     */
    private String downloadUrl;

    /**
     * Main constructor
     *
     * @param version     Version number
     * @param revision    Revision number
     * @param wikiUrl     Url to version wiki
     * @param downloadUrl Url to version download
     */
    public VersionHolder(String version, String revision, String wikiUrl, String downloadUrl) {
        this.version = version;
        this.revision = revision;
        this.wikiUrl = wikiUrl;
        this.downloadUrl = downloadUrl;
    }

    /**
     * Returns the version number
     *
     * @return String - Version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the revisions number
     *
     * @return String - Revision number
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Returns the url to the wiki page of the version
     *
     * @return String - Url to version wiki
     */
    public String getWikiUrl() {
        return wikiUrl;
    }

    /**
     * Returns the url of the version download
     *
     * @return String Url to version download
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }
}