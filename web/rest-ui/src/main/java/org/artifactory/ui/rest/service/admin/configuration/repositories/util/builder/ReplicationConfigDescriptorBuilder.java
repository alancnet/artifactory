package org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder;

import com.google.common.collect.Sets;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.local.LocalReplicationConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.replication.remote.RemoteReplicationConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Utility class for converting model to descriptor
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReplicationConfigDescriptorBuilder {

    @Autowired
    CentralConfigService centralConfig;

    public Set<LocalReplicationDescriptor> buildLocalReplications(List<LocalReplicationConfigModel> models, String repoKey) {
        Set<LocalReplicationDescriptor> descriptors = Sets.newHashSet();
        for (LocalReplicationConfigModel model : models) {
            LocalReplicationDescriptor descriptor = buildLocalReplication(model, repoKey);
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    public LocalReplicationDescriptor buildLocalReplication(LocalReplicationConfigModel model, String repoKey) {
        LocalReplicationDescriptor descriptor = new LocalReplicationDescriptor();
        descriptor.setEnableEventReplication(model.isEnableEventReplication());
        descriptor.setUrl(model.getUrl());
        descriptor.setUsername(model.getUsername());
        descriptor.setPassword(CryptoHelper.encryptIfNeeded(model.getPassword()));
        descriptor.setProxy(centralConfig.getDescriptor().getProxy(model.getProxy()));
        descriptor.setSocketTimeoutMillis(model.getSocketTimeout());
        descriptor.setCronExp(model.getCronExp());
        descriptor.setEnabled(model.isEnabled());
        descriptor.setRepoKey(repoKey);
        descriptor.setSyncDeletes(model.isSyncDeletes());
        descriptor.setSyncProperties(model.isSyncProperties());
        return descriptor;
    }

    public RemoteReplicationDescriptor buildRemoteReplication(RemoteReplicationConfigModel model, String repoKey) {
        RemoteReplicationDescriptor descriptor = new RemoteReplicationDescriptor();
        descriptor.setSyncDeletes(model.isSyncDeletes());
        descriptor.setSyncProperties(model.isSyncProperties());
        descriptor.setRepoKey(repoKey);
        descriptor.setEnabled(model.isEnabled());
        descriptor.setCronExp(model.getCronExp());
        descriptor.setPathPrefix(model.getPathPrefix());
        return descriptor;
    }
}
