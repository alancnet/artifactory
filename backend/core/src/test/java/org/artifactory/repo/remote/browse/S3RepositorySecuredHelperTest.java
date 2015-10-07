/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.repo.remote.browse;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.repo.HttpRepo;
import org.artifactory.test.ArtifactoryHomeStub;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author mamo
 */
public class S3RepositorySecuredHelperTest {

    @Test
    public void testBuildSecuredS3RequestUrl() throws Exception {
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        HttpRepo httpRepo = EasyMock.createMock(HttpRepo.class);
        EasyMock.expect(httpRepo.getUsername()).andReturn("aws-sandbox@jfrog.com").anyTimes();
        EasyMock.expect(httpRepo.getPassword()).andReturn("forgot").anyTimes();
        EasyMock.expect(httpRepo.getRetrievalCachePeriodSecs()).andReturn(1000l).anyTimes();
        EasyMock.replay(httpRepo);

        long expiration = new Date(1000).getTime(); //whatever

        Assert.assertEquals(S3RepositorySecuredHelper.buildSecuredS3RequestUrl(
                "https://s3.amazonaws.com/jfrog-sandbox-bucket/Folder1/filestroreFile.txt", "filestroreFile.txt",
                httpRepo,
                expiration),

                "https://jfrog-sandbox-bucket.s3.amazonaws.com/filestroreFile.txt" +
                        "?AWSAccessKeyId=aws-sandbox@jfrog.com&Expires=1&Signature=gyBqk0wwPmbzZz%2BaonQW%2FX05M0o%3D");

        Assert.assertEquals(S3RepositorySecuredHelper.buildSecuredS3RequestUrl(
                "https://s3.amazonaws.com/some-bucket/somefolder/somejar-1.0.jar", "somefolder/somejar-1.0.jar",
                httpRepo,
                expiration),

                "https://some-bucket.s3.amazonaws.com/somefolder/somejar-1.0.jar" +
                        "?AWSAccessKeyId=aws-sandbox@jfrog.com&Expires=1&Signature=sPhzSCsOHDrmRvjKAtOdvC6M8JQ%3D");
    }
}
