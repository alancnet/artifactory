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

package org.artifactory.common.wicket.component.file.path;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.file.Folder;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.springframework.util.StringUtils.startsWithIgnoreCase;

/**
 * @author yoava
 */
public class PathHelper implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(PathHelper.class);
    private static final Pattern ROOT_PATTERN = Pattern.compile(getRootPattern());
    private static final Pattern ABSOLUTE_PATTERN = Pattern.compile(getAbsolutePattern());

    private String workingDirectoryPath;

    public PathHelper() {
        setDefaultWorkingDirectory();
    }

    public void setDefaultWorkingDirectory() {
        final String defaultRoot = ConstantValues.uiChroot.getString();
        if (StringUtils.isBlank(defaultRoot)) {
            setWorkingDirectoryPath(null);
        } else {
            setWorkingDirectoryPath(defaultRoot);
        }
    }

    public PathHelper(String chRoot) {
        setWorkingDirectoryPath(chRoot);
    }

    public String getWorkingDirectoryPath() {
        return workingDirectoryPath;
    }

    public void setWorkingDirectoryPath(String workingDirectoryPath) {
        if (workingDirectoryPath == null) {
            this.workingDirectoryPath = null;
            return;
        }

        this.workingDirectoryPath = new Folder(workingDirectoryPath).getAbsolutePath().replace('\\', '/');
        if (!this.workingDirectoryPath.endsWith("/")) {
            this.workingDirectoryPath += "/";
        }
    }

    public String getPathFolder(String path) {
        int slashIndex = path.lastIndexOf('/');
        if (slashIndex >= 0) {
            return path.substring(0, slashIndex + 1);
        }
        return path;
    }

    public String getAbsolutePath(String input) {
        String inputPath = input.trim().replace('\\', '/');
        if (workingDirectoryPath == null) {
            // path is absolute
            if (!isAbsolutePath(inputPath)) {
                inputPath = "/" + inputPath;
            }

            String absPath = new File(inputPath).getAbsolutePath().replace('\\', '/');
            if (!isRootPath(absPath) && inputPath.endsWith("/")) {
                absPath += '/';
            }
            return absPath;
        }

        if (inputPath.startsWith("/")) {
            inputPath = inputPath.substring(1);
        }

        inputPath = workingDirectoryPath + inputPath;
        return inputPath;
    }

    private boolean isAbsolutePath(CharSequence path) {
        return ABSOLUTE_PATTERN.matcher(path).matches();
    }

    private boolean isRootPath(CharSequence path) {
        return ROOT_PATTERN.matcher(path).matches();
    }

    public List<File> getFiles(String path, PathMask mask) {
        String absolutePath = getAbsolutePath(path);
        Folder folder = new Folder(getPathFolder(absolutePath));

        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        if (!isParentOf(workingDirectoryPath, folder)) {
            return Collections.emptyList();
        }
        Folder[] folders = folder.getFolders();
        File[] files = folder.getFiles();
        List<File> filesList = new ArrayList<>(folders.length + files.length);

        if (mask.includeFolders()) {
            for (Folder file : folders) {
                String fileAbsPath = file.getAbsolutePath().replace('\\', '/');
                if (startsWithIgnoreCase(fileAbsPath, absolutePath)) {
                    filesList.add(file);
                }
            }
        }

        if (mask.includeFiles()) {
            for (File file : files) {
                String fileAbsPath = file.getPath().replace('\\', '/');
                if (startsWithIgnoreCase(fileAbsPath, absolutePath)) {
                    filesList.add(file);
                }
            }
        }
        return filesList;
    }

    public static boolean isParentOf(String parent, Folder child) {
        if (parent == null) {
            return true;
        }
        // check if child folder matches canonicalParent/*
        final String canonicalParent = getCanonicalPath(new File(parent));
        return getCanonicalPath(child).startsWith(canonicalParent);
    }

    public static String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            log.error(String.format("Could not get canonical path for \"%s\", using absolute path instead: %s",
                    file.getAbsolutePath(), e.getMessage()), e);
        }
        return file.getAbsolutePath();
    }

    public String getRelativePath(File file) {
        if (workingDirectoryPath == null) {
            return file.getAbsolutePath();
        }
        return file.getAbsolutePath().substring(workingDirectoryPath.length() - 1);
    }

    public File getAbsoluteFile(String relativePath) {
        if (relativePath == null) {
            return null;
        }

        return new File(getAbsolutePath(relativePath));
    }

    private static String getRootPattern() {
        StringBuilder pattern = new StringBuilder();
        pattern.append("/");
        for (File file : File.listRoots()) {
            String drive = file.getAbsolutePath().replace('\\', '/');
            pattern.append("|");
            pattern.append(drive);
            pattern.append("|");
            pattern.append(drive.toLowerCase());
        }
        return pattern.toString();
    }

    private static String getAbsolutePattern() {
        return "^" + getRootPattern().replace("|", "|^");
    }
}
