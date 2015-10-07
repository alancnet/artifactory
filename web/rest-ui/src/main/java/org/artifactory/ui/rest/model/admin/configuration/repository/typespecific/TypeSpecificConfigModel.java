package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.model.RestModel;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * @author Dan Feldman
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "repoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BowerTypeSpecificConfigModel.class, name = "Bower"),
        @JsonSubTypes.Type(value = DebTypeSpecificConfigModel.class, name = "Debian"),
        @JsonSubTypes.Type(value = DockerTypeSpecificConfigModel.class, name = "Docker"),
        @JsonSubTypes.Type(value = GemsTypeSpecificConfigModel.class, name = "Gems"),
        @JsonSubTypes.Type(value = GenericTypeSpecificConfigModel.class, name = "Generic"),
        @JsonSubTypes.Type(value = GitLfsTypeSpecificConfigModel.class, name = "GitLfs"),
        @JsonSubTypes.Type(value = GradleTypeSpecificConfigModel.class, name = "Gradle"),
        @JsonSubTypes.Type(value = IvyTypeSpecificConfigModel.class, name = "Ivy"),
        @JsonSubTypes.Type(value = MavenTypeSpecificConfigModel.class, name = "Maven"),
        @JsonSubTypes.Type(value = NpmTypeSpecificConfigModel.class, name = "Npm"),
        @JsonSubTypes.Type(value = NugetTypeSpecificConfigModel.class, name = "NuGet"),
        @JsonSubTypes.Type(value = P2TypeSpecificConfigModel.class, name = "P2"),
        @JsonSubTypes.Type(value = PypiTypeSpecificConfigModel.class, name = "Pypi"),
        @JsonSubTypes.Type(value = SbtTypeSpecificConfigModel.class, name = "SBT"),
        @JsonSubTypes.Type(value = VagrantTypeSpecificConfigModel.class, name = "Vagrant"),
        @JsonSubTypes.Type(value = VcsTypeSpecificConfigModel.class, name = "VCS"),
        @JsonSubTypes.Type(value = YumTypeSpecificConfigModel.class, name = "YUM"),
})
public interface TypeSpecificConfigModel extends RestModel {

    RepoType getRepoType();

    /**
     * This should retrieve the default remote url for each package type
     * For instance: Maven, Gradle, Ivy and SBT should return http://jcenter.bintray.com
     * <p>
     * Notice: method name corresponds to the JSON model field name and is used by the UI.
     */
    String getUrl();
}
