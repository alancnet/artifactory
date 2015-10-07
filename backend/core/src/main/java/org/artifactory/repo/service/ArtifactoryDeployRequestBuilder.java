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

package org.artifactory.repo.service;

import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A builder for {@link ArtifactoryDeployRequest}
 *
 * @author Shay Yaakov
 */
public class ArtifactoryDeployRequestBuilder {

    private RepoPath repoPath;
    private InputStream inputStream;
    private int contentLength = -1;
    private long lastModified = System.currentTimeMillis();
    private Properties properties;

    public ArtifactoryDeployRequestBuilder(RepoPath repoPath) {
        this.repoPath = repoPath;
    }

    /**
     * The file to be deployed. InputStream, contentLength and lastModified values will get extracted
     * from the given file and hence shouldn't set separately.
     */
    public ArtifactoryDeployRequestBuilder fileToDeploy(File fileToDeploy) throws FileNotFoundException {
        this.inputStream = new FileInputStream(fileToDeploy);
        this.contentLength = (int) fileToDeploy.length();
        this.lastModified = fileToDeploy.lastModified();
        return this;
    }

    /**
     * An input stream to deploy, this field is mandatory.
     */
    public ArtifactoryDeployRequestBuilder inputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    /**
     * Optional content length of the deployed artifact, defaults to -1.
     */
    public ArtifactoryDeployRequestBuilder contentLength(int contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    /**
     * Optional last modified value of the deployed artifact, default to system current millis.
     */
    public ArtifactoryDeployRequestBuilder lastModified(long lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Optional properties (usually extracted from matrix params) to attach the deployed artifact.
     */
    public ArtifactoryDeployRequestBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    public ArtifactoryDeployRequest build() {
        if (repoPath == null) {
            throw new IllegalArgumentException("Repo path cannot be null");
        }

        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }

        if (properties == null) {
            properties = (Properties) InfoFactoryHolder.get().createProperties();
        }

        return new ArtifactoryDeployRequest(repoPath, inputStream, contentLength, lastModified, properties);
    }
}
