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

package org.artifactory.mime;

import org.apache.commons.io.FilenameUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.PathUtils;
import org.artifactory.util.SerializablePair;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Used when deploying manually to artifactory, and classifying pom files. Only jar are queried to contain a pom file.
 *
 * @author freds
 * @author yoavl
 */
public abstract class NamingUtils {
    public static final String METADATA_PREFIX = ":";

    private NamingUtils() {
        // utility class
    }

    /**
     * @param path A file path
     * @return The content type for the path. Will return default content type if not mapped.
     */
    @Nonnull
    public static MimeType getMimeType(String path) {
        String extension = PathUtils.getExtension(path);
        return getMimeTypeByExtension(extension);
    }

    @Nonnull
    public static MimeType getMimeTypeByExtension(String extension) {
        MimeType result = null;
        if (extension != null) {
            result = ArtifactoryHome.get().getMimeTypes().getByExtension(extension);
        }

        return result != null ? result : MimeType.def;
    }

    public static String getMimeTypeByPathAsString(String path) {
        MimeType ct = getMimeType(path);
        return ct.getType();
    }

    public static boolean isChecksum(String path) {
        MimeType ct = getMimeType(path);
        return MimeType.checksum.equalsIgnoreCase(ct.getType());
    }

    public static boolean isJarVariant(String path) {
        MimeType ct = getMimeType(path);
        return MimeType.javaArchive.equalsIgnoreCase(ct.getType());
    }

    /**
     * @param path Path to a file (absolute or relative)
     * @return True if the path points to Maven pom file
     */
    public static boolean isPom(String path) {
        MimeType ct = NamingUtils.getMimeType(path);
        return "application/x-maven-pom+xml".equalsIgnoreCase(ct.getType());
    }

    public static boolean isNuPkgFile(String fileName) {
        MimeType mimeType = getMimeType(fileName);
        return "application/x-nupkg".equalsIgnoreCase(mimeType.getType());
    }

    public static boolean isRpmFile(String fileName) {
        return fileName.endsWith(".rpm");
    }

    public static boolean isGemFile(String fileName) {
        MimeType mimeType = NamingUtils.getMimeType(fileName);
        return "application/x-rubygems".equalsIgnoreCase(mimeType.getType());
    }

    public static boolean isNpmFile(String fileName) {
        return fileName.endsWith(".tgz");
    }

    /**
     * @param path Files path
     * @return True if the file syntax is xml
     */
    public static boolean isXml(String path) {
        MimeType ct = NamingUtils.getMimeType(path);
        return "xml".equalsIgnoreCase(ct.getSyntax());
    }

    public static SerializablePair<String, String> getMetadataNameAndParent(String path) {
        int mdPrefixIdx = path.lastIndexOf(METADATA_PREFIX);
        String name = null;
        String parent = null;
        if (mdPrefixIdx >= 0) {
            name = path.substring(mdPrefixIdx + METADATA_PREFIX.length());
            parent = path.substring(0, mdPrefixIdx);
        } else {
            //Fallback to checking maven metadata
            final File file = new File(path);
            if (MavenNaming.MAVEN_METADATA_NAME.equals(file.getName())) {
                name = MavenNaming.MAVEN_METADATA_NAME;
                parent = file.getParent();
            }
        }
        return new SerializablePair<>(name, parent);
    }

    public static boolean isMetadata(String path) {
        String fileName = PathUtils.getFileName(path);
        if (fileName == null || fileName.length() == 0) {
            return false;
        }
        //First check for the metadata pattern of x/y/z/resourceName:metadataName
        if (fileName.contains(METADATA_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the path points to the deprecated properties path (i.e., ends with :properties)
     *
     * @param path The path to check
     * @return True if this is a properties path
     */
    public static boolean isProperties(String path) {
        String fileName = PathUtils.getFileName(path);
        if (fileName == null || fileName.length() == 0) {
            return false;
        }
        if (fileName.endsWith(METADATA_PREFIX + "properties")) {
            return true;
        }
        return false;
    }

    /**
     * @param path The path to check
     * @return True if the path represents a checksum for metadata (ie metadata path and ends with checksum file
     * extension)
     */
    public static boolean isMetadataChecksum(String path) {
        if (isChecksum(path)) {
            String checksumTargetFile = PathUtils.stripExtension(path);
            return isMetadata(checksumTargetFile);
        } else {
            return false;
        }
    }

    /**
     * @return True if the path points to a system file (e.g., maven index)
     */
    public static boolean isSystem(String path) {
        return MavenNaming.isIndex(path) || path.endsWith(".index");
    }

    /**
     * Return the name of the requested metadata. Should be called on a path after determining that it is indeed a
     * metadata path.
     * <pre>
     * getMetadataName("x/y/z/resourceName#file-info") = "file-info"
     * getMetadataName("x/y/z/resourceName/maven-metadata.xml") = "maven-metadata.xmlo"
     * </pre>
     *
     * @param path A metadata path in the pattern of x/y/z/resourceName#metadataName or a path that ends with
     *             maven-metadata.xml.
     * @return The metadata name from the path. Null if not valid.
     */
    public static String getMetadataName(String path) {
        //First check for the metadata pattern of x/y/z/resourceName#metadataName
        int mdPrefixIdx = path.lastIndexOf(METADATA_PREFIX);
        String name = null;
        if (mdPrefixIdx >= 0) {
            name = path.substring(mdPrefixIdx + METADATA_PREFIX.length());
        } else {
            //TODO: [by YS] remove maven metadata from here
            //Fallback to checking maven metadata
            String fileName = PathUtils.getFileName(path);
            if (MavenNaming.isMavenMetadataFileName(fileName)) {
                name = MavenNaming.MAVEN_METADATA_NAME;
            }
        }
        return name;
    }

    public static String stripMetadataFromPath(String path) {
        int metadataPrefixIdx = path.lastIndexOf(NamingUtils.METADATA_PREFIX);
        if (metadataPrefixIdx >= 0) {
            path = path.substring(0, metadataPrefixIdx);
        }
        return path;
    }

    /**
     * Get the path of the metadata container. Assumes we already verified that this is a metadataPath.
     *
     * @param path
     * @return
     */
    public static String getMetadataParentPath(String path) {
        String metadataName = getMetadataName(path);
        // the root repository may have metadata checksums (i.e. repo1), and it will not have a parent.
        // if it doesn't check the relative path without stripping.
        int index = path.lastIndexOf(metadataName) - 1;
        if (index < 0) {
            return metadataName;
        }
        return path.substring(0, index);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static String getParameter(String path, String paramName) {
        String fileName = PathUtils.getFileName(path);
        String paramQueryPrefix = paramName + "=";
        int mdStart = fileName.lastIndexOf(paramQueryPrefix);
        if (mdStart > 0) {
            int mdEnd = fileName.indexOf('&', mdStart);
            String paramValue = fileName.substring(mdStart + paramQueryPrefix.length(),
                    mdEnd > 0 ? mdEnd : fileName.length());
            return paramValue;
        }
        return null;
    }

    /**
     * Recieves a metadata container path (/a/b/c.pom) and a metadata name (maven-metadata.xml) and returns the whole
     * path - "/a/b/c.pom:maven-metadata.xml".
     *
     * @param containerPath Path of metadata container
     * @param metadataName  Name of metadata item
     * @return String - complete path to metadata
     */
    @Deprecated
    public static String getMetadataPath(String containerPath, String metadataName) {
        if ((containerPath == null) || (metadataName == null)) {
            throw new IllegalArgumentException("Container path and metadata name cannot be null.");
        }
        String metadataPath = containerPath + METADATA_PREFIX + metadataName;
        return metadataPath;
    }

    /**
     * @param classFilePath Path to a java class file (ends with .class)
     * @return Path of the matching java source path (.java).
     */
    public static String javaSourceNameFromClassName(String classFilePath) {
        String classFileName = FilenameUtils.getName(classFilePath);
        if (!"class".equals(FilenameUtils.getExtension(classFileName))) {
            return classFilePath;
        }

        String javaFileName;
        if (classFileName.indexOf('$') > 0) {
            // it's a subclass, take the first part (the main class name)
            javaFileName = classFileName.substring(0, classFileName.indexOf('$')) + ".java";
        } else {
            javaFileName = classFileName.replace(".class", ".java");
        }

        String javaFilePath = FilenameUtils.getFullPath(classFilePath) + javaFileName;
        return javaFilePath;
    }

    /**
     * @param fileName The file name
     * @return True if the filename represents a viewable file (ie, text based)
     */
    public static boolean isViewable(String fileName) {
        MimeType contentType = NamingUtils.getMimeType(fileName);
        return contentType.isViewable();
    }

    public static RepoPath getLockingTargetRepoPath(RepoPath repoPath) {
        String path = repoPath.getPath();
        if (isMetadata(path)) {
            String fsItemPath = getMetadataParentPath(path);
            return InternalRepoPathFactory.create(repoPath.getRepoKey(), fsItemPath);
        } else {
            return repoPath;
        }
    }
}