package org.artifactory.storage.binstore.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.io.checksum.Sha1Md5ChecksumInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;

/**
 * @author Gidi Shabat
 */
public class BinaryProviderHelper {
    private static final Logger log = LoggerFactory.getLogger(BinaryProviderHelper.class);
    private static Random random;

    private static synchronized Random initRNG() {
        Random rnd = random;
        return (rnd == null) ? (random = new Random()) : rnd;
    }

    public static BinaryInfoImpl saveStreamToTempFile(InputStream in, File preFileStoreFile) throws IOException {
        Sha1Md5ChecksumInputStream checksumStream = null;
        try {
            // First save to a temp file and calculate checksums while saving
            if (in instanceof Sha1Md5ChecksumInputStream) {
                checksumStream = (Sha1Md5ChecksumInputStream) in;
            } else {
                checksumStream = new Sha1Md5ChecksumInputStream(in);
            }
            log.trace("Saving temp file:  '{}'", preFileStoreFile.getAbsolutePath());
            FileUtils.copyInputStreamToFile(checksumStream, preFileStoreFile);
            log.trace("Saved  temp file:  '{}'", preFileStoreFile.getAbsolutePath());
            long fileLength = preFileStoreFile.length();
            if (fileLength != checksumStream.getTotalBytesRead()) {
                throw new IOException("File length is " + fileLength + " while total bytes read on stream is " +
                        checksumStream.getTotalBytesRead());
            }
            return new BinaryInfoImpl(preFileStoreFile.length(), checksumStream.getMd5(), checksumStream.getSha1());
        } finally {
            IOUtils.closeQuietly(checksumStream);
        }
    }

    public static File persistFile(BinaryInfo binaryInfo, File preFileStoreFile,
            FileProviderStrategy fileProviderStrategy) throws IOException {
        // move the file to its final destination
        File targetFile = fileProviderStrategy.getFile(binaryInfo.getSha1());
        Path targetPath = targetFile.toPath();
        if (!java.nio.file.Files.exists(targetPath)) {
            // Move the file from the pre-filestore to the filestore
            java.nio.file.Files.createDirectories(targetPath.getParent());
            try {
                log.trace("Moving {} to  '{}'", preFileStoreFile.getAbsolutePath(), targetPath);
                java.nio.file.Files.move(preFileStoreFile.toPath(), targetPath, StandardCopyOption.ATOMIC_MOVE);
                log.trace("Moved  {} to  '{}'", preFileStoreFile.getAbsolutePath(), targetPath);
            } catch (FileAlreadyExistsException ignore) {
                // May happen in heavy concurrency cases
                log.trace("Failed moving  '{}'  to  '{}'. File already exist", preFileStoreFile.getAbsolutePath(),
                        targetPath);
            }
            return null;
        } else {
            log.trace("File  '{}'  already exist in the file store. Deleting temp file:  '{}'",
                    targetPath, preFileStoreFile.getAbsolutePath());
            return preFileStoreFile;
        }
    }

    public static BinaryInfo saveStreamFileAndMove(InputStream in, FileProviderStrategy fileProviderStrategy)
            throws IOException {
        File preFileStoreFile = null;
        try {
            preFileStoreFile = createTempBinFile(fileProviderStrategy);
            BinaryInfoImpl binaryInfo = saveStreamToTempFile(in, preFileStoreFile);
            preFileStoreFile = persistFile(binaryInfo, preFileStoreFile, fileProviderStrategy);
            return binaryInfo;
        } finally {
            if (preFileStoreFile != null && preFileStoreFile.exists()) {
                if (!preFileStoreFile.delete()) {
                    log.error("Could not delete temp file  '{}'", preFileStoreFile.getAbsolutePath());
                }
            }
        }
    }


    /**
     * Creates a temp file.
     *
     * @param fileProviderStrategy
     * @return the file
     * @throws IOException On failure writing to the temp file
     */
    public static File createTempBinFile(FileProviderStrategy fileProviderStrategy) throws IOException {
        File temp = fileProviderStrategy.getTempBinariesDir(random);
        return temp;
    }

    public static File createTempBinFile(File tempDir) throws IOException {
        Random rnd = random;
        if (rnd == null) {
            rnd = initRNG();
        }
        long n = rnd.nextLong();
        if (n == Long.MIN_VALUE) {
            n = 0;      // corner case
        } else {
            n = Math.abs(n);
        }
        return new File(tempDir, "dbRecord" + n + ".bin");
    }


    public static File getDataFolder(File rootDataDir, String name) {
        File currentFile = new File(name);
        if (currentFile.isAbsolute()) {
            return currentFile;
        }
        return new File(rootDataDir, name);
    }

    public static String getRelativePath(String sha1) {
        return sha1.substring(0, 2) + "/" + sha1;
    }
}
