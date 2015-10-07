package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.nodes;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.md.Properties;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Collection;

/**
 * @author Chen Keinan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = JunctionNode.class, name = "junction"),
        @JsonSubTypes.Type(value = RootNode.class, name = "root"),
        @JsonSubTypes.Type(value = FolderNode.class, name = "folder"),
        @JsonSubTypes.Type(value = RepositoryNode.class, name = "repository"),
        @JsonSubTypes.Type(value = ZipFileNode.class, name = "archive"),
        @JsonSubTypes.Type(value = FileNode.class, name = "file")})
public interface RestTreeNode extends RestModel {

    /**
     * update additional tree data
     */
    Collection<? extends RestModel> fetchItemTypeData(AuthorizationService authService, boolean isCompact,
            Properties props, ArtifactoryRestRequest request);

    /**
     * get node child's by authorization service service
     *
     * @param authService - authorization service
     * @param request
     * @return list for tee nodes
     */
    Collection<? extends RestTreeNode> getChildren(AuthorizationService authService, boolean isCompact,
            ArtifactoryRestRequest request);


}
