package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Chen Keinan
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = FileGeneralArtifactInfo.class, name = "file"),
                @JsonSubTypes.Type(value = FolderGeneralArtifactInfo.class, name = "folder"),
                @JsonSubTypes.Type(value = ArchiveGeneralArtifactInfo.class, name = "archive"),
                @JsonSubTypes.Type(value = RepositoryGeneralArtifactInfo.class, name = "repository")})
public interface RestGeneralTab extends RestModel {

    /**
     * populate general tab data base on type (repository /folder / file)
     */
    void populateGeneralData(ArtifactoryRestRequest artifactoryRestRequest, AuthorizationService authService);
}
