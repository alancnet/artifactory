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

package org.artifactory.repo.snapshot;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.util.RepoLayoutUtils;

import static org.testng.Assert.assertEquals;

/**
 * @author Noam Y. Tenne
 */
public abstract class BaseSnapshotAdapterTest<T extends MavenSnapshotVersionAdapter> extends ArtifactoryHomeBoundTest {

    private T adapter;

    public BaseSnapshotAdapterTest(T adapter) {
        this.adapter = adapter;
    }

    private ModuleInfo getModuleInfo(String path) {
        return ModuleInfoUtils.moduleInfoFromArtifactPath(path, RepoLayoutUtils.MAVEN_2_DEFAULT);
    }

    protected void adapt(String pathToAdjust, String expectedResult) {
        adapt(pathToAdjust, expectedResult, null);
    }

    protected void adapt(String pathToAdjust, String expectedResult, String errorMessage) {
        String result = adjust(pathToAdjust);

        if (StringUtils.isBlank(errorMessage)) {
            assertEquals(result, expectedResult);
        } else {
            assertEquals(result, expectedResult, errorMessage);
        }
    }

    protected String adjust(String pathToAdjust) {
        MavenSnapshotVersionAdapterContext context = new MavenSnapshotVersionAdapterContext(
                InternalRepoPathFactory.create("local", pathToAdjust), getModuleInfo(pathToAdjust));
        return adapter.adaptSnapshotPath(context);
    }
}
