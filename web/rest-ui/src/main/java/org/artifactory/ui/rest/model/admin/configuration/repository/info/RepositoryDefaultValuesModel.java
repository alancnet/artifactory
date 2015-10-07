package org.artifactory.ui.rest.model.admin.configuration.repository.info;

import com.google.common.collect.Maps;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.local.LocalBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteCacheRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteNetworkRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.*;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.virtual.VirtualBasicRepositoryConfigModel;

import java.util.Map;

/**
 * @author Dan Feldman
 */
public class RepositoryDefaultValuesModel {

    private Map<String, RestModel> defaultModels = Maps.newHashMap();

    public RepositoryDefaultValuesModel() {
        // Construct models with default values
        defaultModels.put("localBasic", new LocalBasicRepositoryConfigModel());
        defaultModels.put("localAdvanced", new LocalAdvancedRepositoryConfigModel());
        defaultModels.put("remoteBasic", new RemoteBasicRepositoryConfigModel());
        defaultModels.put("remoteAdvanced", new RemoteAdvancedRepositoryConfigModel());
        defaultModels.put("virtualBasic", new VirtualBasicRepositoryConfigModel());
        defaultModels.put("virtualAdvanced", new VirtualAdvancedRepositoryConfigModel());
        defaultModels.put("network", new RemoteNetworkRepositoryConfigModel());
        defaultModels.put("cache", new RemoteCacheRepositoryConfigModel());
        // TODO: [by dan] add type specific models here if values are added to the descriptor
        defaultModels.put("bower", new BowerTypeSpecificConfigModel());
        defaultModels.put("gradle", new GradleTypeSpecificConfigModel());
        defaultModels.put("ivy", new IvyTypeSpecificConfigModel());
        defaultModels.put("debian", new DebTypeSpecificConfigModel());
        defaultModels.put("docker", new DockerTypeSpecificConfigModel());
        defaultModels.put("gradle", new GradleTypeSpecificConfigModel());
        defaultModels.put("ivy", new IvyTypeSpecificConfigModel());
        defaultModels.put("maven", new MavenTypeSpecificConfigModel());
        defaultModels.put("nuget", new NugetTypeSpecificConfigModel());
        defaultModels.put("npm", new NpmTypeSpecificConfigModel());
        defaultModels.put("p2", new P2TypeSpecificConfigModel());
        defaultModels.put("sbt", new SbtTypeSpecificConfigModel());
        defaultModels.put("vcs", new VcsTypeSpecificConfigModel());
        defaultModels.put("yum", new YumTypeSpecificConfigModel());
        defaultModels.put("pypi", new PypiTypeSpecificConfigModel());
        defaultModels.put("generic", new GenericTypeSpecificConfigModel());
        defaultModels.put("gems", new GemsTypeSpecificConfigModel());
    }

    public Map<String, RestModel> getDefaultModels() {
        return defaultModels;
    }
}
