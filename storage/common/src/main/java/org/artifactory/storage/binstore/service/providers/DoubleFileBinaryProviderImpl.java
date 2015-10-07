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

package org.artifactory.storage.binstore.service.providers;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.io.checksum.Sha1Md5ChecksumInputStream;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryInfoImpl;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.BinaryProviderHelper;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A binary provider that manage low level checksum files on filesystem.
 *
 * @author Fred Simon
 */
@BinaryProviderClassInfo(nativeName = "double")
public class DoubleFileBinaryProviderImpl extends BinaryProviderBase implements FileBinaryProvider {
    private static final Logger log = LoggerFactory.getLogger(DoubleFileBinaryProviderImpl.class);

    public void syncFilestores() {
        log.info("Synchronizing Binary Stores");
        for (int i = 0; i < 256; i++) {
            String key = String.format("%02X", i & 0xFF).toLowerCase();
            List<Set<String>> sets = new ArrayList<>(getSubBinaryProviders().size());
            for (BinaryProviderBase provider : getSubBinaryProviders()) {
                DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
                File folder = new File(dynamicProvider.getBinariesDir(), key);
                if (folder.exists()) {
                    sets.add(Sets.newHashSet(folder.list()));
                } else {
                    sets.add(new HashSet<String>(0));
                }
            }
            for (int j = 0; j < getSubBinaryProviders().size(); j++) {
                DynamicFileBinaryProviderImpl myProvider = (DynamicFileBinaryProviderImpl) getSubBinaryProviders().get(
                        j);
                // Copy the files that the other provider has that I do not have
                Set<String> myFiles = sets.get(j);
                for (int k = 0; k < getSubBinaryProviders().size(); k++) {
                    if (k != j) {
                        DynamicFileBinaryProviderImpl otherProvider = (DynamicFileBinaryProviderImpl) getSubBinaryProviders().get(
                                k);
                        Set<String> otherFiles = sets.get(k);
                        for (String sha1 : otherFiles) {
                            if (!myFiles.contains(sha1)) {
                                copyShaBetweenProviders(myProvider, otherProvider, sha1);
                            }
                        }
                    }
                }
            }
        }
    }

    private void copyShaBetweenProviders(DynamicFileBinaryProviderImpl myProvider,
            DynamicFileBinaryProviderImpl otherProvider, String sha1) {
        log.info("Copying missing checksum file " + sha1 +
                " from '" + otherProvider.getBinariesDir().getAbsolutePath() +
                "' to '" + myProvider.getBinariesDir().getAbsolutePath() + "'");
        File src = otherProvider.getFile(sha1);
        File destFile = myProvider.getFile(sha1);
        if (destFile.exists()) {
            log.info("Checksum file '" + destFile.getAbsolutePath() + "' already exists");
            return;
        }
        File tempBinFile = null;
        try {
            tempBinFile = BinaryProviderHelper.createTempBinFile(myProvider.tempBinariesDir);
            FileUtils.copyFile(src, tempBinFile);
            Path target = destFile.toPath();
            if (!java.nio.file.Files.exists(target)) {
                // move the file from the pre-filestore to the filestore
                java.nio.file.Files.createDirectories(target.getParent());
                try {
                    log.trace("Moving {} to {}", tempBinFile.getAbsolutePath(), target);
                    java.nio.file.Files.move(tempBinFile.toPath(), target, StandardCopyOption.ATOMIC_MOVE);
                    log.trace("Moved  {} to {}", tempBinFile.getAbsolutePath(), target);
                } catch (FileAlreadyExistsException ignore) {
                    // May happen in heavy concurrency cases
                    log.trace("Failed moving {} to {}. File already exist", tempBinFile.getAbsolutePath(),
                            target);
                }
                tempBinFile = null;
            } else {
                log.trace("File {} already exist in the file store. Deleting temp file: {}",
                        target, tempBinFile.getAbsolutePath());
            }
        } catch (IOException e) {
            String msg = "Error copying file " + src.getAbsolutePath() + " into " + destFile.getAbsolutePath();
            if (log.isDebugEnabled()) {
                log.warn(msg, e);
            } else {
                log.warn(msg + " due to: " + e.getMessage());
            }
        } finally {
            if (tempBinFile != null && tempBinFile.exists()) {
                if (!tempBinFile.delete()) {
                    log.error("Could not delete temp file {}", tempBinFile.getAbsolutePath());
                }
            }
        }
    }

    private DynamicFileBinaryProviderImpl getFirst() {
        DynamicFileBinaryProviderImpl result = findActiveBinaryProvider();

        if (result == null) {
            throw new StorageException("Could not find any file binary provider active!\n");
        }
        return result;
    }

    private DynamicFileBinaryProviderImpl findActiveBinaryProvider() {
        // TODO: Load balance or weigh based read
        DynamicFileBinaryProviderImpl result = null;
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            if (dynamicProvider.isActive()) {
                result = dynamicProvider;
                break;
            }
        }
        return result;
    }

    @Nonnull
    @Override
    public File getBinariesDir() {
        DynamicFileBinaryProviderImpl provider = findActiveBinaryProvider();
        if (provider == null) {
            // Don't throw exception here always return the first one
            return ((DynamicFileBinaryProviderImpl) getSubBinaryProviders().get(0)).getBinariesDir();
        }
        return provider.getBinariesDir();
    }

    @Nonnull
    @Override
    public File getFile(String sha1) {
        return getFirst().getFile(sha1);
    }

    @Override
    public void prune(BasicStatusHolder statusHolder) {
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            if (dynamicProvider.isActive()) {
                dynamicProvider.prune(statusHolder);
            }
        }
    }

    @Override
    public boolean isAccessible() {
        boolean oneActive = false;
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            dynamicProvider.verifyState(dynamicProvider.binariesDir);
            if (dynamicProvider.isActive()) {
                oneActive = true;
            }
        }
        return oneActive;
    }

    @Override
    public boolean exists(String sha1, long length) {
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            if (dynamicProvider.isActive() && provider.exists(sha1, length)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public InputStream getStream(String sha1) throws BinaryNotFoundException {
        BinaryNotFoundException eNotFound = null;
        // TODO: Load balance or weigh based read
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            if (dynamicProvider.isActive()) {
                File file = dynamicProvider.getFile(sha1);
                if (file.exists()) {
                    log.trace("File found: {}", file.getAbsolutePath());
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        log.info("Failed accessing existing file due to " + e.getMessage() + ".\n" +
                                "Will mark provider " + dynamicProvider.getBinariesDir().getAbsolutePath() + " inactive!");
                        dynamicProvider.markInactive(e);
                        eNotFound = new BinaryNotFoundException("Couldn't access file '" + file.getAbsolutePath() + "'",
                                e);
                    }
                }
            }
        }
        if (eNotFound != null) {
            throw eNotFound;
        }
        return next().getStream(sha1);
    }

    @Override
    public boolean delete(String sha1) {
        // We are blocking GC from running if not ALL providers are up.
        // So, should return true only if all deletion worked
        boolean result = true;
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            if (dynamicProvider.isActive()) {
                result &= dynamicProvider.delete(sha1);
            } else {
                result = false;
            }
        }
        return result;
    }

    @Override
    @Nonnull
    public BinaryInfo addStream(InputStream in) throws IOException {
        ProviderAndTempFile[] providerAndTempFiles = null;
        Sha1Md5ChecksumInputStream checksumStream = null;
        try {
            // first save to a temp file and calculate checksums while saving
            if (in instanceof Sha1Md5ChecksumInputStream) {
                checksumStream = (Sha1Md5ChecksumInputStream) in;
            } else {
                checksumStream = new Sha1Md5ChecksumInputStream(in);
            }
            providerAndTempFiles = writeToTempFile(checksumStream);
            BinaryInfo bd = new BinaryInfoImpl(checksumStream);
            log.trace("Inserting {} in file binary provider", bd);

            String sha1 = bd.getSha1();
            boolean oneGoodMove = false;
            for (ProviderAndTempFile providerAndTempFile : providerAndTempFiles) {
                File tempFile = providerAndTempFile.tempFile;
                if (tempFile != null && providerAndTempFile.somethingWrong == null) {
                    try {
                        long fileLength = tempFile.length();
                        if (fileLength != checksumStream.getTotalBytesRead()) {
                            throw new IOException("File length is " + fileLength + " while total bytes read on" +
                                    " stream is " + checksumStream.getTotalBytesRead());
                        }
                        File file = providerAndTempFile.provider.getFile(sha1);
                        Path target = file.toPath();
                        if (!java.nio.file.Files.exists(target)) {
                            // move the file from the pre-filestore to the filestore
                            java.nio.file.Files.createDirectories(target.getParent());
                            try {
                                log.trace("Moving {} to {}", tempFile.getAbsolutePath(), target);
                                java.nio.file.Files.move(tempFile.toPath(), target, StandardCopyOption.ATOMIC_MOVE);
                                log.trace("Moved  {} to {}", tempFile.getAbsolutePath(), target);
                            } catch (FileAlreadyExistsException ignore) {
                                // May happen in heavy concurrency cases
                                log.trace("Failed moving {} to {}. File already exist", tempFile.getAbsolutePath(),
                                        target);
                            }
                            providerAndTempFile.tempFile = null;
                            oneGoodMove = true;
                        } else {
                            log.trace("File {} already exist in the file store. Deleting temp file: {}",
                                    target, tempFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        providerAndTempFile.somethingWrong = e;
                        providerAndTempFile.provider.markInactive(e);
                    }
                }
            }
            if (!oneGoodMove) {
                StringBuilder msg = new StringBuilder("Could not move checksum file ")
                        .append(sha1).append(" to any filestore:\n");
                IOException oneEx = null;
                for (ProviderAndTempFile providerAndTempFile : providerAndTempFiles) {
                    DynamicFileBinaryProviderImpl provider = providerAndTempFile.provider;
                    msg.append("\t'").append(provider.getBinariesDir().getAbsolutePath())
                            .append("' actif=").append(provider.isActive())
                            .append("\n");
                    if (providerAndTempFile.somethingWrong != null) {
                        oneEx = providerAndTempFile.somethingWrong;
                        msg.append("\t\tWith Exception:").append(providerAndTempFile.somethingWrong.getMessage());
                    }
                    msg.append("\n");
                }
                if (oneEx != null) {
                    throw new IOException(msg.toString(), oneEx);
                } else {
                    throw new IOException(msg.toString());
                }
            }
            return bd;
        } finally {
            IOUtils.closeQuietly(checksumStream);
            if (providerAndTempFiles != null) {
                for (ProviderAndTempFile providerAndTempFile : providerAndTempFiles) {
                    File file = providerAndTempFile.tempFile;
                    if (file != null && file.exists()) {
                        if (!file.delete()) {
                            log.error("Could not delete temp file {}", file.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a temp file for each active provider and copies the data there.
     * The input stream is closed afterwards.
     *
     * @param in the input stream
     * @return the collection of provider and temp file
     * @throws java.io.IOException On failure writing to all temp file
     */
    private ProviderAndTempFile[] writeToTempFile(InputStream in) throws IOException {
        ProviderAndTempFile[] result = new ProviderAndTempFile[getSubBinaryProviders().size()];
        int i = 0;
        for (BinaryProviderBase provider : getSubBinaryProviders()) {
            DynamicFileBinaryProviderImpl dynamicProvider = (DynamicFileBinaryProviderImpl) provider;
            result[i++] = new ProviderAndTempFile(dynamicProvider);
        }

        //log.trace("Saving temp files: {} {}", 0, 0);
        try (OutputStream os = new MultipleFilesOutputStream(result)) {
            byte[] buffer = new byte[4 * 1024];
            int n;
            while (-1 != (n = in.read(buffer))) {
                os.write(buffer, 0, n);
            }
            os.close();
            in.close();
        }
        //log.trace("Saved  temp file: {} {}", temp[0].getAbsolutePath(), temp[1].getAbsolutePath());
        return result;
    }

    interface Do {
        void call(FileOutputStream os) throws IOException;
    }

    /*
    @Override
    protected void pruneFiles(MultiStatusHolder statusHolder, MovedCounter movedCounter, File first) {
        statusHolder.status("Starting checking if files in " + first.getAbsolutePath() + " are in DB!", log);
        //Set<DataIdentifier> identifiersSet = getIdentifiersSet();
        File[] files = first.listFiles();
        if (files == null) {
            statusHolder.status("Nothing to do in " + first.getAbsolutePath() + " " + Files.readFailReason(first), log);
            return;
        }
        Set<String> filesInFolder = new HashSet<>(files.length);
        for (File file : files) {
            filesInFolder.add(file.getName());
        }
        Set<String> existingSha1 = getContext().isInStore(filesInFolder);
        for (File file : files) {
            String sha1 = file.getName();
            if (!existingSha1.contains(sha1)) {
                if (getContext().isActivelyUsed(sha1)) {
                    statusHolder.status("Skipping deletion for in-use artifact record: " + sha1, log);
                } else {
                    long size = file.length();
                    Files.removeFile(file);
                    if (file.exists()) {
                        statusHolder.error("Could not delete file " + file.getAbsolutePath(), log);
                    } else {
                        movedCounter.filesMoved++;
                        movedCounter.totalSize += size;
                    }
                }
            }
        }
    }
    */

    static class ProviderAndTempFile {
        final DynamicFileBinaryProviderImpl provider;
        File tempFile = null;
        IOException somethingWrong = null;

        ProviderAndTempFile(DynamicFileBinaryProviderImpl provider) throws IOException {
            this.provider = provider;
            if (provider.isActive()) {
                tempFile = BinaryProviderHelper.createTempBinFile(provider.tempBinariesDir);
            }
        }
    }

    static class DoWrite implements Do {
        final byte[] b;
        final int off;
        final int len;

        DoWrite(byte[] b, int off, int len) {
            this.b = b.clone();
            this.off = off;
            this.len = len;
        }

        @Override
        public void call(FileOutputStream os) throws IOException {
            os.write(b, off, len);
        }
    }

    static class DoWriteByte implements Do {
        final int b;

        DoWriteByte(int b) {
            this.b = b;
        }

        @Override
        public void call(FileOutputStream os) throws IOException {
            os.write(b);
        }
    }

    static class DoFlush implements Do {
        @Override
        public void call(FileOutputStream os) throws IOException {
            os.flush();
        }
    }

    static class DoClose implements Do {
        @Override
        public void call(FileOutputStream os) throws IOException {
            os.close();
        }
    }

    static class FileOutputStreamWithQueue {
        private final ProviderAndTempFile providerAndTempFile;
        private final BlockingQueue<Do> queue;
        private final Thread consumer;
        private boolean closed;

        FileOutputStreamWithQueue(ProviderAndTempFile pAndTemp) {
            this.closed = false;
            this.providerAndTempFile = pAndTemp;
            this.queue = new ArrayBlockingQueue<>(4);
            this.consumer = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        try (FileOutputStream os = new FileOutputStream(providerAndTempFile.tempFile)) {
                            while (!closed || !queue.isEmpty()) {
                                try {
                                    Do take = queue.take();
                                    take.call(os);
                                } catch (InterruptedException e) {
                                    throw new IOException("Got interrupted writing", e);
                                }
                            }
                        }
                    } catch (IOException e) {
                        providerAndTempFile.somethingWrong = e;
                    }
                }
            }, "par-file-writer-" + providerAndTempFile.tempFile.getName());
        }

        void add(Do call) throws IOException {
            try {
                queue.offer(call, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IOException("Got interrupted sending " + call.getClass().getSimpleName()
                        + " to the Output Stream", e);
            }
        }

        void close() throws IOException {
            try {
                add(new DoFlush());
                this.closed = true;
                add(new DoClose());
                this.consumer.join(5000L);
            } catch (InterruptedException e) {
                throw new IOException("Got interrupted closing Output Stream", e);
            }
        }
    }

    static class MultipleFilesOutputStream extends OutputStream {
        private final ProviderAndTempFile[] providerAndTempFiles;
        private final FileOutputStreamWithQueue[] outputStreams;

        MultipleFilesOutputStream(ProviderAndTempFile[] providerAndTempFiles) throws FileNotFoundException {
            if (providerAndTempFiles == null || providerAndTempFiles.length == 0) {
                throw new IllegalArgumentException("Cannot create File Output Stream from empty file!");
            }
            this.providerAndTempFiles = providerAndTempFiles;
            this.outputStreams = new FileOutputStreamWithQueue[providerAndTempFiles.length];
            int i = 0;
            for (ProviderAndTempFile providerAndTempFile : providerAndTempFiles) {
                if (providerAndTempFile.tempFile != null) {
                    outputStreams[i++] = new FileOutputStreamWithQueue(providerAndTempFile);
                }
            }
            for (FileOutputStreamWithQueue outputStream : outputStreams) {
                if (outputStream != null) {
                    outputStream.consumer.start();
                }
            }
        }


        private void execute(Do d) throws IOException {
            for (int i = 0; i < outputStreams.length; i++) {
                // If something failed before, don't try again
                if (outputStreams[i] != null && providerAndTempFiles[i].somethingWrong == null) {
                    try {
                        outputStreams[i].add(d);
                    } catch (IOException e) {
                        providerAndTempFiles[i].somethingWrong = e;
                        // If all providers are failing => throw back the exception
                        for (int j = 0; j < providerAndTempFiles.length; j++) {
                            if (outputStreams[j] != null
                                    && providerAndTempFiles[j].somethingWrong == null) {
                                // We are good still one provider on
                                return;
                            }
                        }
                        // No more valid providers
                        throw e;
                    }
                }
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            execute(new DoWrite(b, off, len));
        }

        @Override
        public void write(int b) throws IOException {
            execute(new DoWriteByte(b));
        }

        @Override
        public void flush() throws IOException {
            execute(new DoFlush());
        }

        @Override
        public void close() throws IOException {
            for (FileOutputStreamWithQueue outputStream : outputStreams) {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }
}
