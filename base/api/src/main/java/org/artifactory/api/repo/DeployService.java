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

package org.artifactory.api.repo;

import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.md.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * Provides artifacts deploy services.
 *
 * @author Yossi Shaul
 */
public interface DeployService {

    @Request
    void deploy(RepoDescriptor targetRepo, UnitInfo artifactInfo, File fileToDeploy, @Nullable Properties properties)
            throws RepoRejectException;

    @Request
    void deploy(RepoDescriptor targetRepo, UnitInfo artifactInfo, File fileToDeploy, String pomString,
            boolean forceDeployPom, boolean partOfBundleDeploy, Properties properties) throws RepoRejectException;

    @Request
    void deployBundle(File bundle, RealRepoDescriptor targetRepo, BasicStatusHolder status, boolean failFast);

    @Request
    void deployBundle(File bundle, RealRepoDescriptor targetRepo, BasicStatusHolder status, boolean failFast,
            @Nonnull String prefix, Properties properties);
}
