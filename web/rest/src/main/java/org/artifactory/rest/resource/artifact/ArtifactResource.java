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

package org.artifactory.rest.resource.artifact;

import com.google.common.collect.Iterables;
import com.sun.jersey.api.core.ExtendedUriInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.PropertiesAddon;
import org.artifactory.addon.rest.AuthorizationRestException;
import org.artifactory.addon.rest.MissingRestAddonException;
import org.artifactory.addon.rest.RestAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.VirtualRepoItem;
import org.artifactory.api.repo.exception.BlackedOutException;
import org.artifactory.api.repo.exception.FolderExpectedException;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.rest.artifact.ItemLastModified;
import org.artifactory.api.rest.artifact.ItemPermissions;
import org.artifactory.api.rest.artifact.ItemProperties;
import org.artifactory.api.rest.artifact.ItemStatsInfo;
import org.artifactory.api.rest.artifact.RestBaseStorageInfo;
import org.artifactory.api.rest.artifact.RestFileInfo;
import org.artifactory.api.rest.artifact.RestFolderInfo;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.checksum.ChecksumsInfo;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.md.Properties;
import org.artifactory.mime.NamingUtils;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.Repo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.virtual.VirtualRepo;
import org.artifactory.rest.common.exception.BadRequestException;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.list.KeyValueList;
import org.artifactory.rest.common.list.StringList;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.util.DoesNotExistException;
import org.artifactory.util.HttpUtils;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.artifactory.api.rest.constant.ArtifactRestConstants.*;

/**
 * @author Eli Givoni
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(PATH_ROOT + "/{" + PATH_PARAM + ": .+}")
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class ArtifactResource {
    private static final Logger log = LoggerFactory.getLogger(ArtifactResource.class);

    private static final String LIST_PARAM = "list";
    private static final String DEEP_PARAM = "deep";
    private static final String DEPTH_PARAM = "depth";
    private static final String LIST_FOLDERS_PARAM = "listFolders";
    private static final String MD_TIMESTAMPS_PARAM = "mdTimestamps";
    private static final String INCLUDE_ROOT_PATH_PARAM = "includeRootPath";
    private static final String LAST_MODIFIED_PARAM = "lastModified";
    private static final String PERMISSIONS_PARAM = "permissions";
    private static final String STATS_PARAM = "stats";

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Context
    private HttpHeaders requestHeaders;

    @Context
    private ExtendedUriInfo uriInfo;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private AddonsManager addonsManager;

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private RepositoryBrowsingService repoBrowsingService;

    @Autowired
    private CentralConfigService centralConfig;



    @PathParam(PATH_PARAM)
    String path;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MT_FOLDER_INFO, MT_FILE_INFO, MT_ITEM_PROPERTIES,
            MT_FILE_LIST, MT_ITEM_LAST_MODIFIED, MT_ITEM_PERMISSIONS, MT_STATS_INFO})
    public Object getStorageInfo() throws IOException {
        return prepareResponseAccordingToType();
    }

    @PUT
    public Response savePathProperties(@QueryParam("recursive") String recursive,
            @QueryParam("properties") KeyValueList properties) {
        return restAddon().savePathProperties(path, recursive, properties);
    }

    @DELETE
    public Response deletePathProperties(@QueryParam("recursive") String recursive,
            @QueryParam("properties") StringList properties) {
        return restAddon().deletePathProperties(path, recursive, properties);
    }

    private RepoPath repoPathFromRequestPath() {
        return RestUtils.calcRepoPathFromRequestPath(path);
    }

    private Response prepareResponseAccordingToType() throws IOException {
        if (isFileListRequest()) {
            return prepareFileListResponse();
        } else if (isLastModifiedRequest()) {
            return prepareLastModifiedResponse();
        } else if (isPropertiesRequest()) {
            return preparePropertiesResponse();
        } else if (isPropertiesXmlRequest()) {
            return preparePropertiesXmlResponse();
        } else if (isPermissionsRequest()) {
            return preparePermissionsResponse();
        } else if (isStatsRequest()) {
            return prepareStatsResponse();
        } else {
            return prepareStorageInfoResponse();
        }
    }

    private Response prepareStatsResponse() throws IOException {
        RepoPath repoPath = repoPathFromRequestPath();
        if (!authorizationService.canRead(repoPath)) {
            return prepareUnAuthorizedResponse(repoPath);
        }
        if (!repositoryService.exists(repoPath)) {
            throw new NotFoundException("Unable to find item path");
        }
        StatsInfo statsInfo = repositoryService.getStatsInfo(repoPath);
        String uri = buildDownloadUri();
        if (statsInfo == null) {
            return okResponse(new ItemStatsInfo(uri, 0, 0, null), MT_STATS_INFO);
        }
        if (isMediaTypeAcceptableByUser(MT_STATS_INFO)) {
            ItemStatsInfo entity = new ItemStatsInfo(uri, statsInfo.getDownloadCount(),
                    statsInfo.getLastDownloaded(), statsInfo.getLastDownloadedBy());
            return okResponse(entity, MT_STATS_INFO);
        } else {
            return notAcceptableResponse(MT_STATS_INFO);
        }
    }

    private boolean isStatsRequest() {
        return queryParamsContainKey(STATS_PARAM);
    }

    private Response prepareUnAuthorizedResponse(RepoPath unAuthorizedResource) throws IOException {
        boolean hideUnauthorizedResources = centralConfig.getDescriptor().getSecurity().isHideUnauthorizedResources();
        if (hideUnauthorizedResources) {
            throw new NotFoundException("Resource not found");
        } else {
            throw new AuthorizationRestException("Request for '" + unAuthorizedResource + "' is forbidden for user '" +
                    authorizationService.currentUsername() + "'.");
        }
    }

    private boolean isFileListRequest() {
        return queryParamsContainKey(LIST_PARAM);
    }

    private boolean isLastModifiedRequest() {
        return queryParamsContainKey(LAST_MODIFIED_PARAM);
    }

    private boolean isPropertiesRequest() {
        return queryParamsContainKey(PROPERTIES_PARAM);
    }

    private boolean isPropertiesXmlRequest() {
        return queryParamsContainKey(PROPERTIES_XML_PARAM);
    }

    private boolean isPermissionsRequest() {
        return queryParamsContainKey(PERMISSIONS_PARAM);
    }

    private boolean queryParamsContainKey(String key) {
        MultivaluedMap<String, String> queryParameters = queryParams();
        return queryParameters.containsKey(key);
    }

    private Response prepareFileListResponse() throws IOException {
        if (authorizationService.isAnonymous()) {
            throw new AuthorizationRestException("This resource is available to authenticated users only.");
        }

        log.debug("Received file list request for: {}. ", path);
        return writeStreamingFileList();
    }

    private int getQueryParameterAsInt(String parameterName) {
        if (queryParams().containsKey(parameterName)) {
            String value = queryParams().getFirst(parameterName);
            if (StringUtils.isNotBlank(value)) {
                return convertStringToInt(value);
            }
        }

        return 0;
    }

    private MultivaluedMap<String, String> queryParams() {
        return uriInfo.getQueryParameters();
    }

    private int convertStringToInt(String integer) {
        return Integer.parseInt(integer);
    }

    private Response writeStreamingFileList() throws IOException {
        try {
            restAddon().writeStreamingFileList(response, getRequestStorageUri(), path,
                    getQueryParameterAsInt(DEEP_PARAM), getQueryParameterAsInt(DEPTH_PARAM),
                    getQueryParameterAsInt(LIST_FOLDERS_PARAM), getQueryParameterAsInt(MD_TIMESTAMPS_PARAM),
                    getQueryParameterAsInt(INCLUDE_ROOT_PATH_PARAM));
            return Response.ok().build();
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (DoesNotExistException dnee) {
            log.debug("Does not exist", dnee);
            throw new NotFoundException(dnee.getMessage());
        } catch (FolderExpectedException fee) {
            log.debug("Folder expected", fee);
            throw new BadRequestException(fee.getMessage());
        } catch (BlackedOutException boe) {
            log.debug("Repository is blacked out", boe);
            throw new NotFoundException(boe.getMessage());
        } catch (MissingRestAddonException mrae) {
            throw mrae;
        } catch (Exception e) {
            log.error("Could not retrieve list", e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving file list: " + e.getMessage()).build();
        }
    }

    private String getRequestStorageUri() {
        RepoPath repoPath = repoPathFromRequestPath();
        return RestUtils.buildStorageInfoUri(request, repoPath.getRepoKey(), repoPath.getPath());
    }

    /**
     * Returns the highest last modified value of the given file or folder (recursively)
     *
     * @return Latest modified item
     */
    private Response prepareLastModifiedResponse() throws IOException {
        fixPathIfNeeded();
        try {
            if (isRequestToNoneLocalRepo()) {
                throw new BadRequestException("This method can only be invoked on local repositories.");
            }
            ItemInfo lastModifiedItem = restAddon().getLastModified(path);
            String uri = getLastModifiedRequestUri(lastModifiedItem);
            String lastModifiedAsISo =
                    DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").print(lastModifiedItem.getLastModified());
            ItemLastModified itemLastModified = new ItemLastModified(uri, lastModifiedAsISo);
            Date lastModifiedDate = new Date(lastModifiedItem.getLastModified());
            return Response.ok(itemLastModified, MT_ITEM_LAST_MODIFIED).lastModified(lastModifiedDate).build();
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (ItemNotFoundRuntimeException infre) {
            throw new NotFoundException(infre.getMessage());
        }
    }

    private String getLastModifiedRequestUri(@Nonnull ItemInfo lastModifiedItem) {
        return RestUtils.buildStorageInfoUri(request, lastModifiedItem.getRepoKey(),
                lastModifiedItem.getRelPath());
    }

    private boolean isMediaTypeAcceptableByUser(String mediaTypeToCheckString) {
        List<MediaType> mediaTypesAcceptableByUser = requestHeaders.getAcceptableMediaTypes();
        MediaType mediaTypeToCheck = MediaType.valueOf(mediaTypeToCheckString);
        for (MediaType acceptableMediaType : mediaTypesAcceptableByUser) {
            //Always accept application/json for backwards compatibility
            if (mediaTypeToCheck.isCompatible(acceptableMediaType) ||
                    MediaType.APPLICATION_JSON_TYPE.equals(acceptableMediaType)) {
                return true;
            }
        }
        return false;
    }

    private Response preparePropertiesResponse() throws IOException {
        if (isMediaTypeAcceptableByUser(MT_ITEM_PROPERTIES)) {
            return getPropertiesResponse();
        } else {
            return notAcceptableResponse(MT_ITEM_PROPERTIES);
        }
    }

    private Response getPropertiesResponse() throws IOException {

        ItemProperties itemProperties = new ItemProperties();
        Properties propertiesAnnotatingItem = resolveProperties();
        if (propertiesAnnotatingItem != null) {
            StringList requestProperties = new StringList(queryParams().getFirst(PROPERTIES_PARAM));
            if (!requestProperties.isEmpty()) {
                for (String propertyName : requestProperties) {
                    Set<String> propertySet = propertiesAnnotatingItem.get(propertyName);
                    if ((propertySet != null) && !propertySet.isEmpty()) {
                        itemProperties.properties.put(propertyName, Iterables.toArray(propertySet, String.class));
                    }
                }
            } else {
                for (String propertyName : propertiesAnnotatingItem.keySet()) {
                    itemProperties.properties.put(propertyName,
                            Iterables.toArray(propertiesAnnotatingItem.get(propertyName), String.class));
                }
            }
        }
        if (!itemProperties.properties.isEmpty()) {
            itemProperties.slf = getRequestStorageUri();
            return Response.ok(itemProperties, MT_ITEM_PROPERTIES)
                    .header(org.apache.http.HttpHeaders.CACHE_CONTROL, "no-store")
                    .build();
        }
        throw new NotFoundException("No properties could be found.");
    }

    private Response preparePropertiesXmlResponse() throws IOException {
        Properties properties = resolveProperties();
        if (properties != null && !properties.isEmpty()) {
            return Response.ok(properties, MediaType.APPLICATION_XML)
                    .header(org.apache.http.HttpHeaders.CACHE_CONTROL, "no-store")
                    .build();
        }
        throw new NotFoundException("No properties could be found.");
    }

    private Properties resolveProperties() throws IOException {
        fixPathIfNeeded();
        RepoPath repoPath = RestUtils.calcRepoPathFromRequestPath(path);
        Repo repo = repositoryService.repositoryByKey(repoPath.getRepoKey());
        Properties properties = null;
        if (repo == null) {
            return properties;
        }
        if (repo.isLocal() || repo.isCache()) {
            if (repo.isCache()) {
                updateProperties(repoPath, repo);
            }
            properties = repositoryService.getProperties(repoPathFromRequestPath());
        } else {
            VirtualRepo virtualRepo = repositoryService.virtualRepositoryByKey(repoPath.getRepoKey());
            if (virtualRepo != null) {
                VirtualRepoItem virtualRepoItem = virtualRepo.getVirtualRepoItem(repoPath);
                if (virtualRepoItem != null) {
                    for (String repoKey : virtualRepoItem.getRepoKeys()) {
                        properties = repositoryService.getProperties(new RepoPathImpl(repoKey, repoPath.getPath()));
                        if (properties != null && !properties.isEmpty()) {
                            return properties;
                        }
                    }
                }
            }
        }
        return properties;
    }

    /**
     * Updates remote properties if cache has expired
     *
     * @param repoPath
     * @param repo
     */
    private void updateProperties(RepoPath repoPath, Repo repo) {
        PropertiesAddon propertiesAddon = ContextHelper.get().beanForType(AddonsManager.class)
                .addonByType(PropertiesAddon.class);
        propertiesAddon.updateRemoteProperties(repo, repoPath);
    }

    private void fixPathIfNeeded() {
        // In case that the path reference to remote repository, then change the path to remote local
        RepoPath repoPath = RestUtils.calcRepoPathFromRequestPath(path);
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        if (descriptor != null && descriptor.isCache()) {
            path = StringUtils.replaceOnce(path, repoPath.getRepoKey(), descriptor.getKey());
        }
    }

    private Response preparePermissionsResponse() throws IOException {
        if (isMediaTypeAcceptableByUser(MT_ITEM_PERMISSIONS)) {
            if (isRequestToNoneLocalRepo()) {
                throw new BadRequestException("This method can only be invoked on local repositories.");
            }
            return getPermissionsResponse(path);
        } else {
            return notAcceptableResponse(MT_ITEM_PERMISSIONS);
        }
    }

    private Response getPermissionsResponse(String path) throws IOException {
        try {
            ItemPermissions itemPermissions = restAddon().getItemPermissions(request, path);
            return okResponse(itemPermissions, MT_ITEM_PERMISSIONS);
        } catch (IllegalArgumentException iae) {
            throw new BadRequestException(iae.getMessage());
        } catch (ItemNotFoundRuntimeException infre) {
            throw new NotFoundException(infre.getMessage());
        }
    }

    private Response prepareStorageInfoResponse() throws IOException {
        RepoPath repoPath = repoPathFromRequestPath();
        String repoKey = repoPath.getRepoKey();
        RestBaseStorageInfo storageInfoRest;
        org.artifactory.fs.ItemInfo itemInfo = null;
        if (isLocalRepo(repoKey)) {
            try {
                if (!authorizationService.canRead(repoPath)) {
                    return prepareUnAuthorizedResponse(repoPath);
                }
                itemInfo = repositoryService.getItemInfo(repoPath);
            } catch (ItemNotFoundRuntimeException e) {
                //no item found, will send 404
            }
        } else if (isVirtualRepo(repoKey)) {
            VirtualRepoItem virtualRepoItem = repoBrowsingService.getVirtualRepoItem(repoPath);
            if (virtualRepoItem == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            itemInfo = virtualRepoItem.getItemInfo();
        }

        if (itemInfo == null) {
            throw new NotFoundException("Unable to find item");
        }

        storageInfoRest = createStorageInfoData(repoKey, itemInfo);
        // we don't use the repo key from the item info because we want to set the virtual repo key if it came
        // from a virtual repository
        storageInfoRest.repo = repoKey;
        if (itemInfo.isFolder()) {
            if (isMediaTypeAcceptableByUser(MT_FOLDER_INFO)) {
                return okResponse(storageInfoRest, MT_FOLDER_INFO);
            } else {
                return notAcceptableResponse(MT_FOLDER_INFO);

            }
        } else {
            if (isMediaTypeAcceptableByUser(MT_FILE_INFO)) {
                return okResponse(storageInfoRest, MT_FILE_INFO);
            } else {
                return notAcceptableResponse(MT_FILE_INFO);
            }
        }
    }

    private RestBaseStorageInfo createStorageInfoData(String repoKey, ItemInfo itemInfo) {
        if (itemInfo.isFolder()) {
            return createFolderInfoData(repoKey, (FolderInfo) itemInfo);
        } else {
            return createFileInfoData((FileInfo) itemInfo, repoKey);
        }
    }

    private boolean isVirtualRepo(String repoKey) {
        VirtualRepoDescriptor virtualRepoDescriptor = repositoryService.virtualRepoDescriptorByKey(repoKey);
        return virtualRepoDescriptor != null;
    }

    private boolean isLocalRepo(String repoKey) {
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoKey);
        return descriptor != null && !(descriptor.isCache() && !descriptor.getKey().equals(repoKey));
    }

    private String buildDownloadUrl(org.artifactory.fs.FileInfo fileInfo) {
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(fileInfo.getRepoKey());
        if (descriptor == null || !descriptor.isCache()) {
            return null;
        }
        RemoteRepoDescriptor remoteRepoDescriptor = ((LocalCacheRepoDescriptor) descriptor).getRemoteRepo();
        StringBuilder sb = new StringBuilder(remoteRepoDescriptor.getUrl());
        sb.append("/").append(fileInfo.getRelPath());
        return sb.toString();
    }


    private RestFileInfo createFileInfoData(FileInfo itemInfo, String repoKey) {
        RestFileInfo fileInfo = new RestFileInfo();
        setBaseStorageInfo(fileInfo, itemInfo, repoKey);

        fileInfo.mimeType = NamingUtils.getMimeTypeByPathAsString(path);
        fileInfo.downloadUri = buildDownloadUri();
        fileInfo.remoteUrl = buildDownloadUrl(itemInfo);
        fileInfo.size = String.valueOf(itemInfo.getSize());
        ChecksumsInfo checksumInfo = itemInfo.getChecksumsInfo();
        ChecksumInfo sha1 = checksumInfo.getChecksumInfo(ChecksumType.sha1);
        ChecksumInfo md5 = checksumInfo.getChecksumInfo(ChecksumType.md5);
        String originalSha1 = sha1 != null ? sha1.getOriginal() : checksumInfo.getSha1();
        String originalMd5 = md5 != null ? md5.getOriginal() : checksumInfo.getMd5();
        fileInfo.checksums = new RestFileInfo.Checksums(checksumInfo.getSha1(), checksumInfo.getMd5());
        fileInfo.originalChecksums = new RestFileInfo.Checksums(originalSha1, originalMd5);
        return fileInfo;
    }

    private RestFolderInfo createFolderInfoData(String repoKey, FolderInfo itemInfo) {
        RestFolderInfo folderInfo = new RestFolderInfo();
        setBaseStorageInfo(folderInfo, itemInfo, repoKey);
        RepoPath folderRepoPath = InternalRepoPathFactory.create(repoKey, itemInfo.getRepoPath().getPath());

        folderInfo.children = new ArrayList<>();

        //if local or cache repo
        if (isLocalRepo(repoKey)) {
            List<ItemInfo> children = repositoryService.getChildren(folderRepoPath);
            for (ItemInfo child : children) {
                folderInfo.children.add(new RestFolderInfo.DirItem("/" + child.getName(), child.isFolder()));
            }
            //for virtual repo
        } else {
            List<VirtualRepoItem> virtualRepoItems = repoBrowsingService.getVirtualRepoItems(folderRepoPath);
            for (VirtualRepoItem item : virtualRepoItems) {
                folderInfo.children.add(new RestFolderInfo.DirItem("/" + item.getName(), item.isFolder()));
            }
        }
        return folderInfo;
    }

    private void setBaseStorageInfo(RestBaseStorageInfo storageInfoRest, ItemInfo itemInfo, String repoKey) {
        storageInfoRest.slf = RestUtils.buildStorageInfoUri(request, repoKey, itemInfo.getRelPath());
        storageInfoRest.path = "/" + itemInfo.getRelPath();
        storageInfoRest.created = RestUtils.toIsoDateString(itemInfo.getCreated());
        storageInfoRest.createdBy = itemInfo.getCreatedBy();
        storageInfoRest.lastModified = RestUtils.toIsoDateString(itemInfo.getLastModified());
        storageInfoRest.modifiedBy = itemInfo.getModifiedBy();
        storageInfoRest.lastUpdated = RestUtils.toIsoDateString(itemInfo.getLastUpdated());
    }

    private String buildDownloadUri() {
        String servletContextUrl = HttpUtils.getServletContextUrl(request);
        StringBuilder sb = new StringBuilder(servletContextUrl);
        sb.append("/").append(path);
        return sb.toString();
    }

    private Response okResponse(Object entity, String mediaType) {
        return Response.ok(entity, mediaType).build();
    }

    private Response notAcceptableResponse(String producedMediaType) {
        return Response.status(HttpStatus.SC_NOT_ACCEPTABLE).entity(
                "Resource produces " + producedMediaType
                        + " but client only accepts " + requestHeaders.getAcceptableMediaTypes()
        ).build();
    }

    private boolean isRequestToNoneLocalRepo() {
        return !isLocalRepo(repoPathFromRequestPath().getRepoKey());
    }

    private RestAddon restAddon() {
        return addonsManager.addonByType(RestAddon.class);
    }
}
