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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.dependency;

/**
 * Collection of build-tool specific dependency declaration generators
 *
 * @author Noam Y. Tenne
 */
public enum DependencyDeclarationProviderType {

    MAVEN("Maven", new MavenDependencyDeclarationProvider()),
    IVY("Ivy", new IvyDependencyDeclarationProvider()),
    GRADLE("Gradle", new GradleDependencyDeclarationProvider());

    private String displayName;
    private DependencyDeclarationProvider dependencyDeclarationProvider;

    DependencyDeclarationProviderType(String displayName, DependencyDeclarationProvider dependencyDeclarationProvider) {
        this.displayName = displayName;
        this.dependencyDeclarationProvider = dependencyDeclarationProvider;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public DependencyDeclarationProvider getDeclarationProvider() {
        return dependencyDeclarationProvider;
    }
}
