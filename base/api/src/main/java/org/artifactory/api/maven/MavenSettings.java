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

package org.artifactory.api.maven;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains different maven settings data. Used mainly by the maven settings generator
 *
 * @author Noam Tenne
 */
public class MavenSettings {
    private String url;
    private List<MavenSettingsRepository> releaseRepositories;
    private List<MavenSettingsRepository> pluginRepositories;
    private List<MavenSettingsMirror> mirrorRepositories;
    private List<MavenSettingsServer> servers;

    /**
     * Default constructor
     *
     * @param url Context URL
     */
    public MavenSettings(String url) {
        this(url, new ArrayList<MavenSettingsRepository>(), new ArrayList<MavenSettingsRepository>(),
                new ArrayList<MavenSettingsMirror>(), new ArrayList<MavenSettingsServer>());
    }

    /**
     * Secondary constructor
     *
     * @param url                Context URL
     * @param repositories       Release repository list
     * @param pluginRepositories Plugin repository list
     * @param mirrorRepositories Mirror repository list
     * @param servers            Server list
     */
    public MavenSettings(String url, List<MavenSettingsRepository> repositories,
            List<MavenSettingsRepository> pluginRepositories, List<MavenSettingsMirror> mirrorRepositories,
            List<MavenSettingsServer> servers) {
        //Make sure URL ends with slash
        if (!url.endsWith("/")) {
            url += "/";
        }
        this.url = url;
        this.releaseRepositories = repositories;
        this.pluginRepositories = pluginRepositories;
        this.mirrorRepositories = mirrorRepositories;
        this.servers = servers;
    }

    /**
     * Returns the context URL of Artifactory
     *
     * @return String - Context URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns the list of release repositories
     *
     * @return List<MavenSettingsRepository> - Release repositories
     */
    public List<MavenSettingsRepository> getReleaseRepositories() {
        return releaseRepositories;
    }

    /**
     * Returns the list of plugin repositories
     *
     * @return List<MavenSettingsRepository> - Plugin repositories
     */
    public List<MavenSettingsRepository> getPluginRepositories() {
        return pluginRepositories;
    }

    /**
     * Returns the list of mirror repositories
     *
     * @return List<MavenSettingsMirror> - Mirror repositories
     */
    public List<MavenSettingsMirror> getMirrorRepositories() {
        return mirrorRepositories;
    }

    /**
     * Returns the list of servers
     *
     * @return List<MavenSettingsServer> - Servers
     */
    public List<MavenSettingsServer> getServers() {
        return servers;
    }

    /**
     * Adds a repository to the release repository list
     *
     * @param mavenSettingsRepository Release repository
     */
    public void addReleaseRepository(MavenSettingsRepository mavenSettingsRepository) {
        releaseRepositories.add(mavenSettingsRepository);
    }

    /**
     * Adds a repository to the plugin repository list
     *
     * @param mavenSettingsRepository Plugin repository
     */
    public void addPluginRepository(MavenSettingsRepository mavenSettingsRepository) {
        pluginRepositories.add(mavenSettingsRepository);
    }

    /**
     * Adds a repository to the mirror repository list
     *
     * @param mavenSettingsMirror Mirror repository
     */
    public void addMirrorRepository(MavenSettingsMirror mavenSettingsMirror) {
        mirrorRepositories.add(mavenSettingsMirror);
    }

    /**
     * Adds a server to the server list
     *
     * @param mavenSettingsServer Server
     */
    public void addServer(MavenSettingsServer mavenSettingsServer) {
        servers.add(mavenSettingsServer);
    }
}
