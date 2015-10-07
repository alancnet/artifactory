/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

/**
 * @author Gidi Shabat
 */
public class DebianNaming {

    public final static String RELEASE = "Release";
    public final static String INRELEASE = "InRelease";
    public final static String RELEASE_GPG = "Release.gpg";
    public final static String PACKAGES = "Packages";
    public final static String PACKAGES_GZ = "Packages.gz";
    public final static String PACKAGES_BZ2 = "Packages.bz2";
    public final static String CONTENTS_PREFIX = "Contents";
    public final static String DISTS_PATH = "dists";

    public final static String distribution = "deb.distribution";
    public final static String component = "deb.component";
    public final static String architecture = "deb.architecture";
    public final static String packageType = "deb.type";

    public static boolean isIndexFile(String fileName) {
        return isPackagesIndex(fileName) || isReleaseIndex(fileName) || isContentIndex(fileName);
    }

    public static boolean isSupportedIndex(String fileName) {
        return isReleaseIndex(fileName) || isPackagesIndex(fileName);
    }

    public static boolean isExpirable(String fileName) {
        return isReleaseIndex(fileName) || isSigningFile(fileName) || isPackagesIndex(fileName);
    }

    public static boolean isReleaseIndex(String fileName) {
        return fileName.equalsIgnoreCase(RELEASE) || fileName.equalsIgnoreCase(INRELEASE);
    }

    public static boolean isPackagesIndex(String fileName) {
        return fileName.equalsIgnoreCase(PACKAGES)
                || fileName.equalsIgnoreCase(PACKAGES_GZ)
                || fileName.equalsIgnoreCase(PACKAGES_BZ2);
    }

    public static boolean isSigningFile(String fileName) {
        return fileName.equalsIgnoreCase(RELEASE_GPG);
    }

    public static boolean isInRelease(String fileName) {
        return fileName.equalsIgnoreCase(INRELEASE);
    }

    //Contents or Contents-<arch>.<gz\bz\bz2>
    public static boolean isContentIndex(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.equalsIgnoreCase(CONTENTS_PREFIX) ||
                (fileName.startsWith(CONTENTS_PREFIX.toLowerCase() + "-")
                    && (fileName.endsWith(".gz") || fileName.endsWith(".bz") || fileName.endsWith(".bz2")));
    }
}
