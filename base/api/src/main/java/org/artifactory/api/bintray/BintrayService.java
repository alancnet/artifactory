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

package org.artifactory.api.bintray;

import com.jfrog.bintray.client.api.handle.Bintray;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.Async;
import org.artifactory.api.search.BintrayItemSearchResults;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.BintrayUploadInfoOverride;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides different Bintray related business methods
 *
 * @author Shay Yaakov
 */
public interface BintrayService {

    String BINTRAY_REPO = "bintray.repo";
    String BINTRAY_PACKAGE = "bintray.package";
    String BINTRAY_VERSION = "bintray.version";
    String BINTRAY_PATH = "bintray.path";

    String PATH_CONTENT = "content";
    String PATH_REPOS = "repos";
    String PATH_PACKAGES = "packages";
    String PATH_USERS = "users";

    final static BintrayPackageInfo PACKAGE_NOT_FOUND = new BintrayPackageInfo();
    final static BintrayPackageInfo PACKAGE_IN_PROCESS = new BintrayPackageInfo();
    final static BintrayPackageInfo PACKAGE_RETRIEVAL_ERROR = new BintrayPackageInfo();

    /**
     * Pushing synchronously single artifact to Bintray
     *
     * @param itemInfo      The item info to push, in case of a folder all it's content will get pushed
     * @param bintrayParams The Bintray model which holds the properties where to push
     * @param headersMap    request header
     * @return Multi status holder containing all the logs during the process
     * @throws IOException In case of connection errors with Bintray
     */
    BasicStatusHolder pushArtifact(ItemInfo itemInfo, BintrayParams bintrayParams,
            @Nullable Map<String, String> headersMap) throws IOException;

    /**
     * Pushing synchronously all build artifacts to Bintray
     *
     * @param build         The build of which to collect the artifacts to push
     * @param bintrayParams The Bintray model which holds the properties where to push
     * @param headersMap    request header
     * @return Multi status holder containing all the logs during the process
     * @throws IOException In case of connection errors with Bintray
     */
    BasicStatusHolder pushBuild(Build build, BintrayParams bintrayParams, @Nullable Map<String, String> headersMap)
            throws IOException;

    /**
     * * Pushes a promoted build to Bintray according to the info supplied in the json spec file.
     * expects to find the json spec file as one of the build's artifacts.
     *
     * @param build           The build to push
     * @param gpgPassphrase   The key that is used with the subject's Bintray-stored gpg key to sign the version
     * @param gpgSignOverride if set to true, overrides the gpgSign in the descriptor(if exists) or causes the version
     *                        to be signed without a passphrase if no descriptor
     * @param override        Overrides the descriptor with minimal parameters for version creation
     * @return MultiStatusHolder containing results of the push operation
     */
    BasicStatusHolder pushPromotedBuild(Build build, String gpgPassphrase, Boolean gpgSignOverride,
            BintrayUploadInfoOverride override);

    /**
     * Pushes a version to Bintray according to the file paths that are specified in the JSON file, if no paths are
     * specified pushes the entire directory tree that resides under the folder containing the json file
     *
     * @param jsonFile      The json file
     * @param gpgPassphrase The key that is used with the subject's Bintray-stored gpg key to sign the version
     * @return MultiStatusHolder containing results of the push operation
     */
    BasicStatusHolder pushVersionFilesAccordingToSpec(FileInfo jsonFile, Boolean gpgSignOverride, String gpgPassphrase);

    /**
     * Pushing asynchronously all build artifacts to Bintray
     *
     * @param build         The build of which to collect the artifacts to push
     * @param bintrayParams The Bintray model which holds the properties where to push
     * @param headersMap    request header
     */
    @Async
    void executeAsyncPushBuild(Build build, BintrayParams bintrayParams, @Nullable Map<String, String> headersMap);

    /**
     * Generates Bintray properties model from the metadata attached to a certain repo path
     *
     * @param repoPath The repo path to search attached metadata from
     * @return The bintray model constructed from the metadata, empty model in case no metadata exists
     */
    @Nonnull
    BintrayParams createParamsFromProperties(RepoPath repoPath);

    /**
     * Saves the given bintray model parameters as metadata properties on the given repo path
     *
     * @param repoPath      The repo path to attach metadata on
     * @param bintrayParams The bintray model to attach as metadata
     */
    void savePropertiesOnRepoPath(RepoPath repoPath, BintrayParams bintrayParams);

    /**
     * Get available repositories from Bintray
     * The list will contain repositories which the logged in user has permissions to deploy to
     *
     * @param headersMap request header
     * @throws IOException      In case of connection errors with Bintray
     * @throws BintrayException In case we received any response other than 200 OK
     */
    List<Repo> getReposToDeploy(@Nullable Map<String, String> headersMap) throws IOException, BintrayException;

    /**
     * Get available packages of specific repository from Bintray
     * The list will contain packages which the logged in user has permissions to deploy to
     *
     * @param repoKey    The repository key to search packages under
     * @param headersMap request header
     * @throws IOException      In case of connection errors with Bintray
     * @throws BintrayException In case we received any response other than 200 OK
     */
    List<String> getPackagesToDeploy(String repoKey, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException;

    /**
     * Get available package versions of specific repository and package from Bintray
     *
     * @param repoKey    The repository key to search packages under
     * @param packageId  The package name to search for versions
     * @param headersMap request header
     * @throws IOException      In case of connection errors with Bintray
     * @throws BintrayException In case we received any response other than 200 OK
     */
    List<String> getVersions(String repoKey, String packageId, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException;

    /**
     * Get the version URL in Bintray of which the user can browse into
     *
     * @param bintrayParams The bintray model to extract the URL from
     */
    String getVersionFilesUrl(BintrayParams bintrayParams);

    /**
     * Get a Bintray user information
     *
     * @param username   The username to search
     * @param apiKey     The apiKey which belongs to the given username
     * @param headersMap request header
     * @throws IOException      In case of connection errors with Bintray
     * @throws BintrayException In case we received any response other than 200 OK
     */
    BintrayUser getBintrayUser(String username, String apiKey, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException;

    /**
     * Get a Bintray user information
     *
     * @param username The username to search
     * @param apiKey   The apiKey which belongs to the given username
     * @throws IOException      In case of connection errors with Bintray
     * @throws BintrayException In case we received any response other than 200 OK
     */
    BintrayUser getBintrayUser(String username, String apiKey) throws IOException, BintrayException;

    /**
     * Validates that the user properly configured his Bintray credentials
     */
    boolean isUserHasBintrayAuth();

    /**
     * Get the registation URL for Bintray including Artifactory specific source query param.
     * In case of Artifactory Pro, the license hash is also included with the query param value.
     */
    String getBintrayRegistrationUrl();

    /**
     * Search for a files by name, can take the * and ? wildcard characters.
     */
    BintrayItemSearchResults<BintrayItemInfo> searchByName(String query, @Nullable Map<String, String> headersMap)
            throws IOException, BintrayException;

    /**
     * Retrieves JCenter repo
     */
    RemoteRepoDescriptor getJCenterRepo();

    /**
     * Retrieves from  Bintray package info for  item's sha1.
     */
    BintrayPackageInfo getBintrayPackageInfo(String sha1, @Nullable Map<String, String> headersMap);

    /**
     * Retrieves true if system Bintray API key exists
     */
    boolean hasBintraySystemUser();

    Bintray createBintrayClient(BasicStatusHolder status) throws IllegalArgumentException;
}
