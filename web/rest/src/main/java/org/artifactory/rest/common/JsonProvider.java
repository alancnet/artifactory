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

package org.artifactory.rest.common;

import org.artifactory.api.rest.constant.*;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * A wrapper provider that makes it simpler to configure providers in one place. <br/> (This is more crucial for WAS,
 * where discovery is done by direct jar reference).
 * <p/>
 * Regarding supported media types - we do not support inference/inheritence of media types (e.g. if we serve
 * application/json we can also serve application/vnd.xxx+json, unless there is a provider with a more specific match.
 * This would require overriding -com.sun.jersey.core.spi.factory.MessageBodyFactory#getClassCapability or
 * +com.sun.jersey.core.spi.factory.MessageBodyFactory#init, but MessageBodyFactory is not pluggable.
 *
 * @author Yoav Landman
 */
@Provider
@Consumes({
        MediaType.APPLICATION_JSON,
        RestConstants.MT_LEGACY_ARTIFACTORY_APP,
        BuildRestConstants.MT_BUILDS,
        BuildRestConstants.MT_BUILDS_BY_NAME,
        BuildRestConstants.MT_BUILD,
        BuildRestConstants.MT_BUILD_INFO,
        BuildRestConstants.MT_PROMOTION_REQUEST,
        ArtifactRestConstants.MT_FOLDER_INFO,
        ArtifactRestConstants.MT_FILE_INFO,
        RepositoriesRestConstants.MT_REMOTE_REPOSITORY_CONFIG,
        RepositoriesRestConstants.MT_LOCAL_REPOSITORY_CONFIGURATION,
        RepositoriesRestConstants.MT_VIRTUAL_REPOSITORY_CONFIGURATION,
        SearchRestConstants.MT_ARTIFACT_RESULT,
        SearchRestConstants.MT_ARTIFACT_SEARCH_RESULT,
        SearchRestConstants.MT_ARCHIVE_ENTRY_SEARCH_RESULT,
        SearchRestConstants.MT_GAVC_SEARCH_RESULT,
        SearchRestConstants.MT_PROPERTY_SEARCH_RESULT,
        SearchRestConstants.MT_XPATH_SEARCH_RESULT,
        SearchRestConstants.MT_USAGE_SINCE_SEARCH_RESULT,
        SearchRestConstants.MT_CREATED_IN_RANGE_SEARCH_RESULT,
        SearchRestConstants.MT_PATTERN_SEARCH_RESULT,
        SystemRestConstants.MT_IMPORT_SETTINGS,
        SystemRestConstants.MT_EXPORT_SETTINGS,
        SecurityRestConstants.MT_GROUP,
        SecurityRestConstants.MT_USER,
        SecurityRestConstants.MT_PERMISSION_TARGET,
        SecurityRestConstants.MT_GROUPS,
        SecurityRestConstants.MT_USERS,
        SecurityRestConstants.MT_PERMISSION_TARGETS,
        ReplicationRestConstants.MT_REPLICATION_REQUEST,
        ReplicationsRestConstants.MT_REPLICATION_CONFIG_REQUEST,
        ReplicationsRestConstants.MT_MULTI_REPLICATION_CONFIG_REQUEST,
        BuildRestConstants.MT_BUILD_PATTERN_ARTIFACTS_REQUEST,
        BuildRestConstants.MT_BUILD_ARTIFACTS_REQUEST,
        GitLfsResourceConstants.LFS_JSON
})
@Produces({
        MediaType.APPLICATION_JSON,
        BuildRestConstants.MT_BUILDS,
        BuildRestConstants.MT_BUILDS_BY_NAME,
        BuildRestConstants.MT_BUILD,
        BuildRestConstants.MT_BUILD_INFO,
        BuildRestConstants.MT_BUILDS_DIFF,
        BuildRestConstants.MT_COPY_MOVE_RESULT,
        BuildRestConstants.MT_PROMOTION_RESULT,
        RepositoriesRestConstants.MT_REPOSITORY_DETAILS_LIST,
        RepositoriesRestConstants.MT_REMOTE_REPOSITORY_CONFIGURATION,
        RepositoriesRestConstants.MT_REMOTE_REPOSITORY_CONFIG,
        RepositoriesRestConstants.MT_LOCAL_REPOSITORY_CONFIGURATION,
        RepositoriesRestConstants.MT_VIRTUAL_REPOSITORY_CONFIGURATION,
        ArtifactRestConstants.MT_FOLDER_INFO,
        ArtifactRestConstants.MT_FILE_INFO,
        ArtifactRestConstants.MT_ITEM_PROPERTIES,
        SearchRestConstants.MT_ARTIFACT_RESULT,
        SearchRestConstants.MT_ARTIFACT_SEARCH_RESULT,
        SearchRestConstants.MT_ARCHIVE_ENTRY_SEARCH_RESULT,
        SearchRestConstants.MT_GAVC_SEARCH_RESULT,
        SearchRestConstants.MT_PROPERTY_SEARCH_RESULT,
        SearchRestConstants.MT_XPATH_SEARCH_RESULT,
        SearchRestConstants.MT_LICENSE_SEARCH_RESULT,
        SearchRestConstants.MT_USAGE_SINCE_SEARCH_RESULT,
        SearchRestConstants.MT_CREATED_IN_RANGE_SEARCH_RESULT,
        SystemRestConstants.MT_VERSION_RESULT,
        SystemRestConstants.MT_IMPORT_SETTINGS,
        SystemRestConstants.MT_EXPORT_SETTINGS,
        ArtifactRestConstants.MT_COPY_MOVE_RESULT,
        SearchRestConstants.MT_PATTERN_SEARCH_RESULT,
        ArtifactRestConstants.MT_FILE_LIST,
        ArtifactRestConstants.MT_ITEM_LAST_MODIFIED,
        SearchRestConstants.MT_CHECKSUM_SEARCH_RESULT,
        SearchRestConstants.MT_BAD_CHECKSUM_SEARCH_RESULT,
        ArtifactRestConstants.MT_ITEM_PERMISSIONS,
        ArtifactRestConstants.MT_STATS_INFO,
        SearchRestConstants.MT_DEPENDENCY_BUILDS,
        SecurityRestConstants.MT_GROUP,
        SecurityRestConstants.MT_USER,
        SecurityRestConstants.MT_PERMISSION_TARGET,
        SecurityRestConstants.MT_GROUPS,
        SecurityRestConstants.MT_USERS,
        SecurityRestConstants.MT_PERMISSION_TARGETS,
        ReplicationRestConstants.MT_REPLICATION_REQUEST,
        ReplicationRestConstants.MT_REPLICATION_STATUS,
        ReplicationsRestConstants.MT_REPLICATION_CONFIG_REQUEST,
        ReplicationsRestConstants.MT_MULTI_REPLICATION_CONFIG_REQUEST,
        PluginRestConstants.MT_BUILD_STAGING_STRATEGY,
        BuildRestConstants.MT_BUILD_PATTERN_ARTIFACTS_RESULT,
        SearchRestConstants.MT_ARTIFACT_VERSIONS_SEARCH_RESULT,
        SearchRestConstants.MT_BUILD_ARTIFACTS_SEARCH_RESULT,
        GitLfsResourceConstants.LFS_JSON
})
public class JsonProvider extends JacksonJsonProvider implements ContextResolver<ObjectMapper> {

    public JsonProvider() {
        ObjectMapper mapper = getMapper();
        //Update the annotation interceptor to also include jaxb annotations as a second choice
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);

        mapper.getSerializationConfig().setAnnotationIntrospector(pair);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        mapper.getDeserializationConfig().setAnnotationIntrospector(pair);
        //Ignore missing properties
        mapper.getDeserializationConfig().disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Provide a contextual (by media type and type) mapper object that is configured to pretty-print when in dev mode
     *
     * @param objectType
     * @return
     */
    @Override
    public ObjectMapper getContext(Class objectType) {
        ObjectMapper mapper = getMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        return mapper;
    }

    private ObjectMapper getMapper() {
        ObjectMapper mapper = super._mapperConfig.getDefaultMapper();
        return mapper;
    }
}