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

package org.artifactory.rest.resource.search.types;

import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.addon.rest.MissingRestAddonException;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.rest.search.result.InfoRestSearchResult;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.common.exception.RestException;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.util.StorageInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;

/**
 * Exposes the checksum searcher to REST via the REST addon
 *
 * @author Noam Y. Tenne
 */
public class ChecksumSearchResource {
    private static final Logger log = LoggerFactory.getLogger(ChecksumSearchResource.class);

    private AuthorizationService authorizationService;

    private RestAddon restAddon;
    private RepositoryService repositoryService;
    private RepositoryBrowsingService repoBrowsingService;
    private HttpServletRequest request;

    public ChecksumSearchResource(AuthorizationService authorizationService, RestAddon restAddon,
            RepositoryService repositoryService, RepositoryBrowsingService repoBrowsingService,
            HttpServletRequest request) {
        this.authorizationService = authorizationService;
        this.restAddon = restAddon;
        this.repositoryService = repositoryService;
        this.repoBrowsingService = repoBrowsingService;
        this.request = request;
    }

    /**
     * Searches for artifacts by checksum
     *
     * @param md5Checksum   MD5 checksum value
     * @param sha1Checksum  SHA1 checksum value
     * @param reposToSearch Specific repositories to search within
     * @return Search results object
     */
    @GET
    @Produces({SearchRestConstants.MT_CHECKSUM_SEARCH_RESULT, MediaType.APPLICATION_JSON})
    public InfoRestSearchResult get(@QueryParam(SearchRestConstants.PARAM_MD5_CHECKSUM) String md5Checksum,
            @QueryParam(SearchRestConstants.PARAM_SHA1_CHECKSUM) String sha1Checksum,
            @QueryParam(SearchRestConstants.PARAM_REPO_TO_SEARCH) StringList reposToSearch)
            throws IOException, AuthorizationRestException {
        if (!authorizationService.isAuthenticated()) {
            throw new AuthorizationRestException();
        }

        try {
            return search(md5Checksum, sha1Checksum, reposToSearch);
        } catch (MissingRestAddonException mrae) {
            throw mrae;
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (Exception e) {
            String errorMessage =
                    String.format("Error occurred while searching for artifacts by checksum: %s", e.getMessage());
            log.error(errorMessage, e);
            throw new RestException(errorMessage);
        }
    }

    private InfoRestSearchResult search(String md5Checksum, String sha1Checksum, StringList reposToSearch) {
        Set<RepoPath> matchingArtifacts = restAddon.searchArtifactsByChecksum(md5Checksum, sha1Checksum, reposToSearch);

        InfoRestSearchResult resultToReturn = new InfoRestSearchResult();
        for (RepoPath matchingArtifact : matchingArtifacts) {
            FileInfo fileInfo = repositoryService.getFileInfo(matchingArtifact);
            StorageInfoHelper storageInfoHelper = new StorageInfoHelper(request, repositoryService, repoBrowsingService,
                    fileInfo);
            resultToReturn.results.add(storageInfoHelper.createStorageInfo());
        }

        return resultToReturn;
    }
}
