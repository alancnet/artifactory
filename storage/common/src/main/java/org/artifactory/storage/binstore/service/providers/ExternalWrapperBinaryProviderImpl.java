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

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.BinaryProviderHelper;
import org.artifactory.storage.binstore.service.FileBinaryProvider;
import org.artifactory.storage.binstore.service.ProviderConnectMode;
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Date: 12/16/12
 * Time: 4:30 PM
 *
 * @author freds
 */
@BinaryProviderClassInfo(nativeName = "external-wrapper")
public class ExternalWrapperBinaryProviderImpl extends FileBinaryProviderReadOnlyBase {
    private static final Logger log = LoggerFactory.getLogger(ExternalWrapperBinaryProviderImpl.class);

    ProviderConnectMode connectMode;

    @Override
    public void initialize() {
        super.initialize();
        String connectModeName = getParam("connectMode", ProviderConnectMode.COPY_FIRST.name());
        this.connectMode = ProviderConnectMode.getConnectMode(connectModeName);
        if (connectMode == null) {
            throw new IllegalArgumentException("Cannot create Wrapper with null connection mode!");
        }
    }

    public void setConnectMode(ProviderConnectMode connectMode) {
        this.connectMode = connectMode;
    }

    public FileBinaryProvider nextFileProvider() {
        BinaryProviderBase bp = next();
        while (!(bp instanceof FileBinaryProvider)) {
            bp = bp.next();
            if (bp == null) {
                throw new IllegalStateException("Could not find file binary provider to wrap!");
            }
        }
        FileBinaryProvider result = (FileBinaryProvider) bp;
        // TODO: Verify the wrapper points to the binary store folder => Need a isValid method on all binary provider
        return result;
    }

    @Nonnull
    @Override
    public InputStream getStream(String sha1) throws BinaryNotFoundException {
        if (connectMode == ProviderConnectMode.COPY_ON_READ) {
            try {
                return new CopyOnReadInputStream(next().getStream(sha1), sha1);
            } catch (IOException e) {
                throw new BinaryNotFoundException("Could read on copy stream for " + sha1, e);
            }
        }
        connect(sha1);
        return super.getStream(sha1);
    }

    public boolean connect(String sha1) {
        Path src = nextFileProvider().getFile(sha1).toPath();
        Path dest = getFile(sha1).toPath();
        if (!Files.exists(dest)) {
            switch (connectMode) {
                case PASS_THROUGH:
                    // Nothing
                    break;
                case COPY_FIRST:
                    // No way to copy on read with a wrapper?!
                case COPY_ON_READ:
                    try {
                        Files.createDirectories(dest.getParent());
                        Files.copy(src, dest, StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (IOException e) {
                        throw new BinaryNotFoundException("Could not copy " + src + " into " + dest, e);
                    }
                    return true;
                case MOVE:
                    try {
                        Files.createDirectories(dest.getParent());
                        Files.move(src, dest, StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException e) {
                        throw new BinaryNotFoundException("Could not move " + src + " into " + dest, e);
                    }
                    return true;
                default:
                    throw new IllegalStateException("Connect mode " + connectMode + " not supported!");
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public BinaryInfo addStream(InputStream in) throws IOException {
        // No add to a wrapper binary provider
        return next().addStream(in);
    }

    @Override
    public boolean delete(String sha1) {
        // No deletion of a wrapper binary provider
        return next().delete(sha1);
    }

    @Override
    public void prune(BasicStatusHolder statusHolder) {
        throw new UnsupportedOperationException("An wrapper binary provider cannot be pruned!");
    }

    class CopyOnReadInputStream extends SavedToFileInputStream {
        private final String sha1;

        CopyOnReadInputStream(InputStream in, String sha1) throws IOException {
            super(in, BinaryProviderHelper.createTempBinFile(tempBinariesDir));
            this.sha1 = sha1;
        }

        @Override
        protected boolean afterClose() throws IOException {
            if (somethingWrong != null) {
                log.error("Something went wrong saving the temp copy on read file!", somethingWrong);
                return true;
            }
            if (!fullyRead) {
                log.debug("Did not fully read entry " + sha1 + " for copy on read. Not using temp file.");
                return true;
            }
            File finalFile = getFile(sha1);
            if (finalFile.exists()) {
                if (finalFile.length() != tempFile.length()) {
                    log.error("After read, found a file with checksum '" + sha1 + "' " +
                            "but length is " + finalFile.length() + " not " + tempFile.length());
                    return true;
                } else {
                    // All good already there, just delete the temp file
                    return true;
                }
            }
            moveTempFileTo(finalFile);
            if (somethingWrong != null) {
                log.error("Something went wrong saving the temp copy on read file!", somethingWrong);
                return true;
            }
            return true;
        }
    }
}
