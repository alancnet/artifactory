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

package org.artifactory.repo;

import org.artifactory.binstore.BinaryInfo;
import org.artifactory.fs.RepoResource;
import org.artifactory.io.RemoteResourceStreamHandle;
import org.artifactory.md.Properties;
import org.artifactory.resource.ResourceStreamHandle;

import java.io.InputStream;

/**
 * @author Noam Y. Tenne
 */
public class SaveResourceContext {

    private final RepoResource repoResource;
    private final ResourceStreamHandle handle;
    private final InputStream inputStream;
    private final Properties properties;
    private final long created;
    private final String createBy;
    private final String modifiedBy;
    /**
     * Optional existing binary info. If this field is not null, the content is already in the binary store and there's
     * no need to re-read the input stream.
     */
    private final BinaryInfo binaryInfo;

    public SaveResourceContext(RepoResource repoResource, ResourceStreamHandle handle, InputStream inputStream,
            Properties properties, long created, String createBy, String modifiedBy, BinaryInfo binaryInfo) {
        this.repoResource = repoResource;
        this.handle = handle;
        this.inputStream = inputStream;
        this.properties = properties;
        this.created = created;
        this.createBy = createBy;
        this.modifiedBy = modifiedBy;
        this.binaryInfo = binaryInfo;
    }

    public RepoResource getRepoResource() {
        return repoResource;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Properties getProperties() {
        return properties;
    }

    public long getCreated() {
        return created;
    }

    public String getCreateBy() {
        return createBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setException(Throwable throwable) {
        if (handle instanceof RemoteResourceStreamHandle) {
            ((RemoteResourceStreamHandle) handle).setThrowable(throwable);
        }
    }

    public BinaryInfo getBinaryInfo() {
        return binaryInfo;
    }

    public static class Builder {

        private RepoResource repoResource;
        private InputStream inputStream;
        private Properties properties;
        private long created;
        private String createBy;
        private String modifiedBy;
        private ResourceStreamHandle handle;
        private BinaryInfo binaryInfo;

        public Builder(RepoResource repoResource, ResourceStreamHandle handle) {
            this(repoResource, handle.getInputStream());
            this.handle = handle;
        }

        public Builder(RepoResource repoResource, InputStream inputStream) {
            this.repoResource = repoResource;
            this.inputStream = inputStream;
        }

        public Builder(SaveResourceContext other) {
            repoResource = other.repoResource;
            inputStream = other.inputStream;
            handle = other.handle;
            properties = other.properties;
            createBy = other.createBy;
            created = other.created;
            modifiedBy = other.modifiedBy;
            binaryInfo = other.binaryInfo;
        }

        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder createdBy(String createBy) {
            this.createBy = createBy;
            return this;
        }

        public Builder modifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public Builder binaryInfo(BinaryInfo binaryInfo) {
            this.binaryInfo = binaryInfo;
            return this;
        }

        public SaveResourceContext build() {
            return new SaveResourceContext(repoResource, handle, inputStream, properties, created, createBy,
                    modifiedBy, binaryInfo);
        }
    }
}
