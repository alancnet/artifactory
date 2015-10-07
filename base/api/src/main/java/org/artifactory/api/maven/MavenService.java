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

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.exception.maven.BadPomException;

import java.io.File;
import java.io.IOException;

/**
 * Provides different maven related service methods
 *
 * @author Noam Tenne
 */
public interface MavenService {

    /**
     * Generate maven settings data from a MavenSettings object
     *
     * @param mavenSettings Data to generate settings from
     * @return String - Maven settings that can be put directly into the settings.xml file
     */
    String generateSettings(MavenSettings mavenSettings) throws IOException;

    /**
     * Validates pom content before deployment
     *
     * @param pomContent                   POM content to validate
     * @param relPath                      Relative deployment path of the pom
     * @param moduleInfo                   POM module info
     * @param suppressPomConsistencyChecks If true will not throw an exception is pom consistency fails (eg, groupId
     *                                     doesn't match target relative path)
     * @throws IOException If POM is invalid
     */
    void validatePomContent(String pomContent, String relPath, ModuleInfo moduleInfo,
            boolean suppressPomConsistencyChecks) throws IOException;

    /**
     * Validates pom file's content before deployment
     *
     * @param pomFile                      POM file to validate
     * @param relPath                      Relative deployment path of the pom
     * @param moduleInfo                   POM module info
     * @param suppressPomConsistencyChecks If true will not throw an exception is pom consistency fails (eg, groupId
     *                                     doesn't match target relative path)
     * @throws BadPomException If POM is invalid
     */
    void validatePomFile(File pomFile, String relPath, ModuleInfo moduleInfo,
            boolean suppressPomConsistencyChecks) throws BadPomException;

    /**
     * Get the artifact model from a jar or pom file
     *
     * @param uploadedFile .jar or .pom file
     * @return null if no pom found
     * @throws java.io.IOException if uploaded file is unreadable
     */
    MavenArtifactInfo getMavenArtifactInfo(File uploadedFile);

    /**
     * Get the POM model that represents a file. If the file is a POM file then we simply return it. If it is a jar with
     * a POM then we check that we return its contents. Otherwise we generate a default POM according to the file's
     * GAVC.
     *
     * @param file The file to analyze.
     * @return The file's POM in string representation.
     */
    String getPomModelString(File file);
}
