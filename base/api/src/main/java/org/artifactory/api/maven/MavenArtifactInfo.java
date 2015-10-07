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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Artifact info for Maven artifacts. Holds the groupId, artifactId, version, classifier and type of the Maven artifact
 * to be deployed, if a certain attribute cannot be found it will be marked as NA. You must have at least groupId,
 * artifactId and version for a valid Maven artifact.
 *
 * @author Tomer Cohen
 */
@JsonTypeName("maven")
public class MavenArtifactInfo implements UnitInfo {
    private static final Logger log = LoggerFactory.getLogger(MavenArtifactInfo.class);
    public static final String ROOT = "artifactory-maven-artifact";
    public static final String POM = "pom";
    public static final String JAR = "jar";
    public static final String XML = "xml";
    private String artifactType = "maven";
    private String artifactId;
    private String groupId;
    private String version;
    private String classifier;
    private String type;

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public MavenArtifactInfo(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, NA, NA);
    }

    public MavenArtifactInfo(MavenArtifactInfo copy) {
        this(copy.groupId, copy.artifactId, copy.version, copy.classifier, copy.type);
    }

    public MavenArtifactInfo() {
        this(NA, NA, NA, NA, JAR);
    }

    public MavenArtifactInfo(String groupId, String artifactId, String version, String classifier, String type) {
        if (groupId == null || artifactId == null) {
            throw new IllegalArgumentException("Cannot create a maven unit with null groupId or ArtifactId");
        }
        this.groupId = groupId;
        this.artifactId = artifactId;
        if (PathUtils.hasText(version)) {
            this.version = version;
        } else {
            this.version = NA;
        }
        if (PathUtils.hasText(classifier)) {
            this.classifier = classifier;
        } else {
            this.classifier = NA;
        }
        if (PathUtils.hasText(type)) {
            this.type = type;
        } else {
            this.type = JAR;
        }
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasGroupId() {
        return StringUtils.isNotBlank(groupId) && !NA.equals(groupId);
    }

    public boolean hasArtifactId() {
        return StringUtils.isNotBlank(artifactId) && !NA.equals(artifactId);
    }

    public boolean hasVersion() {
        return StringUtils.isNotBlank(version) && !NA.equals(version);
    }

    @Override
    public boolean isMavenArtifact() {
        return true;
    }

    public String getClassifier() {
        if (!hasClassifier()) {
            return null;
        }
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public boolean hasClassifier() {
        return classifier != null && !NA.equals(classifier);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean isValid() {
        return hasGroupId() && hasArtifactId() && hasVersion();
    }

    @Override
    public void setPath(String path) {
    }

    @Override
    public String getPath() {
        return buildMavenPath();
    }

    @Override
    public String toString() {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion() +
                (classifier != null ? (":" + classifier) : "") + ":" + type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        MavenArtifactInfo that = (MavenArtifactInfo) o;

        return artifactId.equals(that.artifactId) && classifier.equals(that.classifier) &&
                groupId.equals(that.groupId) && type.equals(that.type) &&
                version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + classifier.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public static MavenArtifactInfo fromRepoPath(RepoPath repoPath) {
        String groupId, artifactId, version, type = MavenArtifactInfo.NA, classifier = MavenArtifactInfo.NA;

        String path = repoPath.getPath();
        String fileName = repoPath.getName();

        //The format of the relative path in maven is a/b/c/artifactId/baseVer/fileName where
        //groupId="a.b.c". We split the path to elements and analyze the needed fields.
        LinkedList<String> pathElements = new LinkedList<>();
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        while (tokenizer.hasMoreTokens()) {
            pathElements.add(tokenizer.nextToken());
        }
        //Sanity check, we need groupId, artifactId and version
        if (pathElements.size() < 3) {
            log.debug("Cannot build MavenArtifactInfo from '{}'. The groupId, artifactId and version are unreadable.",
                    repoPath);
            return new MavenArtifactInfo();
        }

        //Extract the version, artifactId and groupId
        int pos = pathElements.size() - 2;  // one before the last path element
        version = pathElements.get(pos--);
        artifactId = pathElements.get(pos--);
        StringBuilder groupIdBuff = new StringBuilder();
        for (; pos >= 0; pos--) {
            if (groupIdBuff.length() != 0) {
                groupIdBuff.insert(0, '.');
            }
            groupIdBuff.insert(0, pathElements.get(pos));
        }
        groupId = groupIdBuff.toString();
        //Extract the type and classifier except for metadata files
        boolean metaData = NamingUtils.isMetadata(fileName);
        if (!metaData) {
            if (MavenNaming.isUniqueSnapshotFileName(fileName)) {
                version = StringUtils.remove(version, "-" + MavenNaming.SNAPSHOT);
                version = version + "-" + MavenNaming.getUniqueSnapshotVersionTimestampAndBuildNumber(fileName);
            }

            type = StringUtils.substring(fileName, artifactId.length() + version.length() + 2);
            int versionStartIndex = StringUtils.indexOf(fileName, "-", artifactId.length()) + 1;
            int classifierStartIndex = StringUtils.indexOf(fileName, "-", versionStartIndex + version.length());
            if (classifierStartIndex >= 0) {
                Set<String> customMavenTypes = getMavenCustomTypes();
                for (String customMavenType : customMavenTypes) {
                    if (StringUtils.endsWith(fileName, customMavenType)) {
                        classifier = StringUtils.remove(type, "." + customMavenType);
                        type = customMavenType;
                        break;
                    }
                }

                if (MavenArtifactInfo.NA.equals(classifier)) {
                    int typeDotStartIndex = StringUtils.lastIndexOf(type, ".");
                    classifier = StringUtils.substring(type, 0, typeDotStartIndex);
                    type = StringUtils.substring(type, classifier.length() + 1);
                }
            }
        }
        return new MavenArtifactInfo(groupId, artifactId, version, classifier, type);
    }

    private static Set<String> getMavenCustomTypes() {
        String mvnCustomTypes = ConstantValues.mvnCustomTypes.getString();
        return Sets.newHashSet(
                Iterables.transform(Sets.newHashSet(StringUtils.split(mvnCustomTypes, ",")),
                        new Function<String, String>() {
                            @Override
                            public String apply(@Nullable String input) {
                                return StringUtils.isBlank(input) ? input : StringUtils.trim(input);
                            }
                        }
                )
        );
    }

    /**
     * Returns the maven artifact "id" in a "prettier" format.<br>
     * {@link #toString()} will not omit fields like the classifier and type when
     * they are not specified. This results in ugly artifact IDs.<br>
     * This implementation will simply omit fields which are not specified.
     *
     * @return Summarized artifact info
     */
    public String getPrettyArtifactId() {
        StringBuilder artifactIdBuilder = new StringBuilder(getGroupId()).append(":").
                append(getArtifactId()).append(":").
                append(getVersion());

        String classifier = getClassifier();
        if (StringUtils.isNotBlank(classifier) && !MavenArtifactInfo.NA.equals(classifier)) {
            artifactIdBuilder.append(":").append(classifier);
        }

        String type = getType();
        if (StringUtils.isNotBlank(type) && !MavenArtifactInfo.NA.equals(classifier)) {
            artifactIdBuilder.append(":").append(type);
        }
        return artifactIdBuilder.toString();
    }

    /**
     * Builds a maven path according to the artifact's GAVC.
     *
     * @return The maven path according the artifact's GAVC.
     */
    private String buildMavenPath() {
        StringBuilder path = new StringBuilder();
        if (isValid()) {
            addBasePath(path);
            path.append("/").append(getArtifactId()).append("-").append(getVersion());
            if (hasClassifier()) {
                path.append("-").append(classifier);
            }
            path.append(".").append(type);
        }
        return path.toString();
    }

    private void addBasePath(StringBuilder path) {
        if (isValid()) {
            path.append(groupId.replace('.', '/')).append("/").append(artifactId);
            if (hasVersion()) {
                path.append("/").append(version);
            }
        }
    }
}