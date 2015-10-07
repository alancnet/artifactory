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

package org.artifactory.maven;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.mime.MavenNaming;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author Noam Y. Tenne
 */
public class PomTargetPathValidator {
    private static final Logger log = LoggerFactory.getLogger(PomTargetPathValidator.class);

    private final String relPath;
    private final ModuleInfo moduleInfo;
    private Model model;

    public PomTargetPathValidator(String relPath, ModuleInfo moduleInfo) {
        this.relPath = relPath;
        this.moduleInfo = moduleInfo;
    }

    public void validate(InputStream in, boolean suppressPomConsistencyChecks) throws IOException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            model = reader.read(new InputStreamReader(in, MavenModelUtils.UTF8));

            String groupId = getGroupId(model);

            if (StringUtils.isNotBlank(groupId)) {
                //Do not verify if the pom's groupid does not exist
                String modelVersion = getModelVersion(model);
                if (StringUtils.isBlank(modelVersion)) {
                    String msg = String.format(
                            "The Pom version of '%s' does not exists. Please verify your POM content for correctness",
                            relPath);
                    if (suppressPomConsistencyChecks) {
                        log.error("{} POM consistency checks are suppressed. Broken artifacts might have been " +
                                "stored in the repository - please resolve this manually.", msg);
                        return;
                    } else {
                        throw new BadPomException(msg);
                    }
                }

                //For snapshots with unique snapshot version, do not include the model version in the path
                boolean snapshot = moduleInfo.isIntegration();
                boolean versionSnapshot = MavenNaming.isNonUniqueSnapshotVersion(modelVersion);

                String pathPrefix = null;
                if (snapshot && !versionSnapshot) {
                    pathPrefix = groupId.replace('.', '/') + "/" + model.getArtifactId() + "/";
                } else if (StringUtils.isNotBlank(modelVersion)) {
                    pathPrefix = groupId.replace('.', '/') + "/" + model.getArtifactId() + "/" +
                            modelVersion;
                }

                //Do not validate paths that contain property references
                if (pathPrefix != null && !pathPrefix.contains("${")
                        && !StringUtils.startsWithIgnoreCase(relPath, pathPrefix)) {
                    String msg = String.format(
                            "The target deployment path '%s' does not match the POM's expected path " +
                                    "prefix '%s'. Please verify your POM content for correctness and make sure the source path " +
                                    "is a valid Maven repository root path.", relPath, pathPrefix);
                    if (suppressPomConsistencyChecks) {
                        log.warn("{} POM consistency checks are suppressed. Broken artifacts might have been " +
                                "stored in the repository - please resolve this manually.", msg);
                    } else {
                        throw new BadPomException(msg);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            if (log.isDebugEnabled()) {
                try {
                    in.reset();
                    InputStreamReader isr = new InputStreamReader(in, MavenModelUtils.UTF8);
                    String s = readString(isr);
                    log.debug("Could not parse bad POM for '{}'. Bad POM content:\n{}\n", relPath, s);
                } catch (Exception ex) {
                    log.trace("Could not extract bad POM content for '{}': {}.", relPath, e.getMessage());
                }
            }
            String message = "Failed to read POM for '" + relPath + "': " + e.getMessage() + ".";
            if (suppressPomConsistencyChecks) {
                log.error(message + " POM consistency checks are suppressed. Broken artifacts might have been " +
                        "stored in the repository - please resolve this manually.");
            } else {
                throw new BadPomException(message);
            }
        }
    }

    private String getGroupId(Model model) {
        String groupId = model.getGroupId();
        if (StringUtils.isBlank(groupId)) {
            Parent parent = model.getParent();
            if (parent != null) {
                groupId = parent.getGroupId();
            }
        }
        return groupId;
    }

    private String getModelVersion(Model model) {
        String modelVersion = model.getVersion();
        //Version may come from the parent
        if (StringUtils.isBlank(modelVersion)) {
            Parent parent = model.getParent();
            if (parent != null) {
                modelVersion = parent.getVersion();
            }
        }
        return modelVersion;
    }

    private String readString(Reader reader) throws IOException {
        StringBuilder buffer = new StringBuilder(2048);
        int value;
        while ((value = reader.read()) != -1) {
            buffer.append((char) value);
        }
        return buffer.toString();
    }

    /**
     * @return True if the processed pom represents a maven plugin.
     */
    public boolean isMavenPlugin() {
        return model != null && "maven-plugin".equals(model.getPackaging());
    }
}
