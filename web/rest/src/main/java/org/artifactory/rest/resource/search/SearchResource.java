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

package org.artifactory.rest.resource.search;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.SearchRestConstants;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.resource.license.LicenseResource;
import org.artifactory.rest.resource.search.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 * Resource class that handles search actions
 *
 * @author Yoav Landman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(SearchRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_USER, AuthorizationService.ROLE_ADMIN})
public class SearchResource {

    @Context
    private HttpServletRequest request;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    SearchService searchService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RepositoryBrowsingService repoBrowsingService;

    @Autowired
    AddonsManager addonsManager;

    /**
     * Delegates the request to the artifact search resource
     *
     * @return Artifact search resource
     */
    @Path(SearchRestConstants.PATH_ARTIFACT)
    public ArtifactSearchResource artifactQuery() {
        return new ArtifactSearchResource(authorizationService, searchService, repositoryService, repoBrowsingService,
                request);
    }

    /**
     * Delegates the request to the archive search resource
     *
     * @return Archive search resource
     */
    @Path(SearchRestConstants.PATH_ARCHIVE)
    public ArchiveSearchResource archiveQuery() {
        return new ArchiveSearchResource(authorizationService, searchService, request);
    }

    /**
     * Delegates the request to the GAVC search resource
     *
     * @return GAVC search resource
     */
    @Path(SearchRestConstants.PATH_GAVC)
    public GavcSearchResource gavcQuery() {
        return new GavcSearchResource(authorizationService, searchService, repositoryService, repoBrowsingService,
                request);
    }

    /**
     * Delegates the request to the property search resource
     *
     * @return property search resource
     */
    @Path(SearchRestConstants.PATH_PROPERTY)
    public PropertySearchResource propertyQuery() {
        return new PropertySearchResource(authorizationService, searchService, repositoryService, repoBrowsingService,
                request);
    }

    /**
     * Delegates the request to the usage since search resource
     *
     * @return Usage since resource
     */
    @Path(SearchRestConstants.PATH_USAGE_SINCE)
    public UsageSinceResource notDownloadedSinceQuery() {
        return new UsageSinceResource(authorizationService, searchService, request);
    }

    /**
     * Delegates the request to the in date range search resource
     *
     * @return create in range resource
     */
    @Path(SearchRestConstants.PATH_CREATED_IN_RANGE)
    public CreatedInRangeResource createdInDateRangeQuery() {
        return new CreatedInRangeResource(authorizationService, searchService, request);
    }

    @Path(SearchRestConstants.PATH_DATES_IN_RANGE)
    public AnyDateInRangeResource datesInRangeQuery() {
        return new AnyDateInRangeResource(authorizationService, searchService, request, repositoryService);
    }

    @Path(SearchRestConstants.PATH_PATTERN)
    public PatternSearchResource patternSearchQuery() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new PatternSearchResource(authorizationService, repositoryService, restAddon, request);
    }

    @Path(SearchRestConstants.PATH_LICENSE)
    public LicenseResource licensesSearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new LicenseResource(restAddon, request, repositoryService);
    }

    @Path(SearchRestConstants.PATH_CHECKSUM)
    public ChecksumSearchResource checksumSearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new ChecksumSearchResource(authorizationService, restAddon, repositoryService, repoBrowsingService,
                request);
    }

    @Path(SearchRestConstants.PATH_BAD_CHECKSUM)
    public BadChecksumSearchResource badChecksumSearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new BadChecksumSearchResource(authorizationService, restAddon, request);
    }

    @Path(SearchRestConstants.PATH_DEPENDENCY)
    public DependencySearchResource dependencySearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new DependencySearchResource(restAddon, request, authorizationService);
    }

    @Path(SearchRestConstants.PATH_VERSIONS)
    public ArtifactVersionsSearchResource versionsSearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new ArtifactVersionsSearchResource(restAddon);
    }

    @Path(SearchRestConstants.PATH_LATEST_VERSION)
    public ArtifactLatestVersionSearchResource latestVersionSearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new ArtifactLatestVersionSearchResource(restAddon);
    }

    @Path(SearchRestConstants.PATH_BUILD_ARTIFACTS)
    public BuildArtifactsSearchResource buildArtifactsSearch() {
        RestAddon restAddon = addonsManager.addonByType(RestAddon.class);
        return new BuildArtifactsSearchResource(restAddon, authorizationService, request);
    }
}
