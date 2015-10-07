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

package org.artifactory.util;

import com.google.common.collect.Lists;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * User: freds Date: Jun 25, 2008 Time: 12:11:46 AM
 */
public abstract class Files {
    private static final Logger log = LoggerFactory.getLogger(Files.class);
    private static final Random intGenerator = new Random(System.currentTimeMillis());

    private Files() {
        // utility class
    }

    public static String getDecodedFileUrl(File file) {
        return "file:" + file.toURI().getPath();
    }

    /**
     * Rename the oldFile to oldFileName.original.XX.extension, where XX is a rolling number for files with identical
     * names in the oldFile.getParent() directory. Then Rename the newFile to oldFile.
     *
     * @param oldFile
     * @param newdFile
     */
    public static void switchFiles(File oldFile, File newdFile) {
        String exceptionMsg = "Cannot switch files '" + oldFile.getAbsolutePath() + "' to '" +
                newdFile.getAbsolutePath() + "'. '";
        if (!oldFile.isFile()) {
            throw new IllegalArgumentException(exceptionMsg + oldFile.getAbsolutePath() + "' is not a file.");
        }
        if (!newdFile.exists()) {
            // Just rename the new file to the old name
            try {
                org.apache.commons.io.FileUtils.moveFile(oldFile, newdFile);
            } catch (IOException e) {
                log.warn("Cannot rename {} to {}: {}", oldFile, newdFile, e.getMessage());
            }
            return;
        }
        if (!newdFile.isFile()) {
            //renameTo() of open files does not work on windows (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6213298)
            throw new IllegalArgumentException(exceptionMsg + newdFile.getAbsolutePath() + "' is not a file.");
        }
        //Else is
        File dir = newdFile.getParentFile();
        String fileName = newdFile.getName();
        int lastDot = fileName.lastIndexOf('.');
        final String extension;
        final String fileNameNoExt;
        if (lastDot <= 0) {
            extension = "";
            fileNameNoExt = fileName;
        } else {
            extension = fileName.substring(lastDot + 1);
            fileNameNoExt = fileName.substring(0, lastDot);
        }

        final String fileNameNoNumber = fileNameNoExt + ".";
        File backupOrigFileName = new File(dir, fileNameNoNumber + extension);
        if (backupOrigFileName.exists()) {
            // Rolling files
            String[] allOrigFiles = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fileNameNoExt) && name.endsWith(extension) &&
                            name.length() > fileNameNoNumber.length() + extension.length();
                }
            });
            int maxNb = 1;
            for (String origFile : allOrigFiles) {
                try {
                    String middle = origFile.substring(fileNameNoNumber.length(),
                            origFile.length() - extension.length() - 1);
                    int value = Integer.parseInt(middle);
                    if (value >= maxNb) {
                        maxNb = value + 1;
                    }
                } catch (Exception e) {
                    log.warn("Minor issue in file name '" + origFile + "':" + e.getMessage());
                }
            }
            backupOrigFileName = new File(dir, fileNameNoNumber + maxNb + ".xml");
        }
        File destFile = new File(dir, fileName);
        try {
            org.apache.commons.io.FileUtils.moveFile(newdFile, backupOrigFileName);
            org.apache.commons.io.FileUtils.moveFile(oldFile, destFile);
        } catch (IOException e) {
            log.warn("Cannot rename {} to {} or {} to {}: {}",
                    newdFile, backupOrigFileName, oldFile, destFile, e.getMessage());
        }
    }

    public static File createRandomDir(File parentDir, String prefix) {
        File dir = new File(parentDir, prefix + intGenerator.nextInt());
        if (dir.exists()) {
            //Either we did not clean up or are VERY unlucky
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " already exists!");
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Failed to create directory '" + dir.getPath() + "'.");
        }
        return dir;
    }

    /**
     * Deletes all empty directories (directories that don't contain any files) under the input directory. The input
     * directory will not be deleted.
     *
     * @param directory The directory to cleanup
     */
    public static void cleanupEmptyDirectories(File directory) {
        cleanupEmptyDirectories(directory, false);
    }

    /**
     * Deletes all empty directories (directories that don't contain any files) under the input directory.
     *
     * @param directory            The directory to cleanup
     * @param includeBaseDirectory if true the input directory will also be deleted if it is empty
     */
    private static void cleanupEmptyDirectories(File directory, boolean includeBaseDirectory) {
        if (!directory.exists()) {
            log.warn("{} does not exist", directory);
            return;
        }

        if (!directory.isDirectory()) {
            log.warn("{} is not a directory", directory);
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            log.warn("Failed to list contents of {}: {}", directory, readFailReason(directory));
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                cleanupEmptyDirectories(file, true);  // recursive call always include new base directory
            }
        }

        // all other directories scanned - now check if this direcotry should be deleted
        if (includeBaseDirectory && directory.listFiles().length == 0) {
            // empty directory - delete and return
            boolean deleted = directory.delete();
            if (!deleted) {
                log.warn("Failed to delete empty directory {}", directory);
            }
        }

    }

    public static boolean removeFile(File file) {
        if (file != null && file.exists()) {
            //Try to delete the file
            if (!remove(file)) {
                log.warn("Unable to remove " + file.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    private static boolean remove(final File file) {
        if (!file.delete()) {
            // NOTE: fix for java/win bug. see:
            // http://forum.java.sun.com/thread.jsp?forum=4&thread=158689&tstart=0&trange=15
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // nop
            }

            // Try one more time to delete the file
            return file.delete();
        }
        return true;
    }

    public static String readFileToString(File file) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file, "utf-8");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read text file from " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Writes the content to the given file while maintaining a rolling file policy.<br> The file's last-modified
     * timestamp will be appended to each file that is rolled.<br> The amount of rolled files to retain can be defined
     * in {@link org.artifactory.common.ConstantValues.fileRollerMaxFilesToRetain}
     *
     * @param content    Content to write to the file
     * @param targetFile Target file
     */
    public static void writeContentToRollingFile(String content, File targetFile) throws IOException {
        if (!targetFile.exists()) {

            targetFile.createNewFile();
        } else {
            File parentDir = targetFile.getParentFile();

            final String fileNameWithNoExtension = PathUtils.stripExtension(targetFile.getName());
            final String fileExtension = PathUtils.getExtension(targetFile.getName());
            String newTargetFileName = fileNameWithNoExtension + "." + targetFile.lastModified() + "." + fileExtension;
            org.apache.commons.io.FileUtils.copyFile(targetFile, new File(parentDir, newTargetFileName));

            List<File> existingFileList = Lists.newArrayList(parentDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(fileNameWithNoExtension) && name.endsWith(fileExtension);
                }
            }));
            Collections.sort(existingFileList);

            int maxFiles = ConstantValues.fileRollerMaxFilesToRetain.getInt();
            if (maxFiles < 0) {
                log.warn("A negative integer value '{}' was provided for '{}'. Ignoring and falling back to '{}'.",
                        maxFiles, ConstantValues.fileRollerMaxFilesToRetain.getPropertyName(),
                        ConstantValues.fileRollerMaxFilesToRetain.getDefValue());
                maxFiles = Integer.parseInt(ConstantValues.fileRollerMaxFilesToRetain.getDefValue());
            }

            while (existingFileList.size() > maxFiles) {
                File toRemove = existingFileList.remove(0);
                org.apache.commons.io.FileUtils.deleteQuietly(toRemove);
            }
        }

        org.apache.commons.io.FileUtils.writeStringToFile(targetFile, content);
    }

    public static long writeToFileAndClose(InputStream in, File file) throws IOException {
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(in, file);
        } catch (IOException e) {
            try {
                String msg = "Could not fill content in file '" + file.getAbsolutePath()
                        + "' due to " + e.getMessage() + "\n. Deleting it if possible!";
                if (log.isDebugEnabled()) {
                    log.error(msg, e);
                } else {
                    log.error(msg);
                }
                if (file.exists()) {
                    if (!file.delete()) {
                        log.error("Could not delete wrong file '" + file.getAbsolutePath() + "'!");
                    }
                }
            } catch (Exception ignore) {
                // Ignore all exception here, to throw back the original failure
                log.debug("Fail to remove file after failed fill data", ignore);
            }
            throw e;
        }
        return file.length();
    }

    /**
     * Tries to determine why the file could not be read.
     * NOTICE: This method doesn't determine whether the file is accessible or not, just provides pretty reason string
     * @param file to test
     * @return A string describing why the file cannot be read.
     */
    public static String readFailReason(File file) {
        Path asPath = file.toPath();

        if (!java.nio.file.Files.notExists(asPath)) {
            return "File not found";
        } else if (!java.nio.file.Files.isReadable(asPath)) {
            return "Access denied";
        } else {
            return "Unknown error";
        }

    }

    public static long nextLong(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }
        // error checking and 2^x checking removed for simplicity.
        long bits, val;
        do {
            bits = (intGenerator.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }
}