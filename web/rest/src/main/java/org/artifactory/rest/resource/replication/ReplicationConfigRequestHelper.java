package org.artifactory.rest.resource.replication;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.rest.replication.ReplicationConfigRequest;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.ReplicationBaseDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.rest.common.exception.BadRequestException;
import org.quartz.CronExpression;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author mamo
 */
public abstract class ReplicationConfigRequestHelper {

    public static void fillBaseReplicationDescriptor(ReplicationConfigRequest replicationRequest,
            ReplicationBaseDescriptor newReplication) {
        if (replicationRequest.isEnabled() != null) {
            newReplication.setEnabled(replicationRequest.isEnabled());
        }
        if (replicationRequest.getCronExp() != null) {
            newReplication.setCronExp(replicationRequest.getCronExp());
        }
        if (replicationRequest.getSyncDeletes() != null) {
            newReplication.setSyncDeletes(replicationRequest.getSyncDeletes());
        }
        if (replicationRequest.getSyncProperties() != null) {
            newReplication.setSyncProperties(replicationRequest.getSyncProperties());
        }
        if (replicationRequest.getPathPrefix() != null) {
            newReplication.setPathPrefix(replicationRequest.getPathPrefix());
        }
    }

    public static void fillLocalReplicationDescriptor(ReplicationConfigRequest replicationRequest,
            LocalReplicationDescriptor newReplication) {
        fillBaseReplicationDescriptor(replicationRequest, newReplication);
        if (replicationRequest.getUrl() != null) {
            newReplication.setUrl(replicationRequest.getUrl());
        }
        if (replicationRequest.getEnableEventReplication() != null) {
            newReplication.setEnableEventReplication(replicationRequest.getEnableEventReplication());
        }
        if (replicationRequest.getUsername() != null) {
            newReplication.setUsername(replicationRequest.getUsername());
        }
        if (replicationRequest.getPassword() != null) {
            newReplication.setPassword(replicationRequest.getPassword());
        }
        final String proxy = replicationRequest.getProxy();
        if (proxy != null) {
            try {
                CentralConfigService centralConfigService = ContextHelper.get().beanForType(CentralConfigService.class);
                List<ProxyDescriptor> proxies = centralConfigService.getDescriptor().getProxies();
                ProxyDescriptor proxyDescriptor = Iterables.find(proxies,
                        new Predicate<ProxyDescriptor>() {
                            @Override
                            public boolean apply(@Nullable ProxyDescriptor input) {
                                return (input != null) && proxy.equals(input.getKey());
                            }
                        });
                newReplication.setProxy(proxyDescriptor);
            } catch (NoSuchElementException nsee) {
                throw new BadRequestException("Could not find proxy");
            }
        }
        if (replicationRequest.getSocketTimeoutMillis() != null) {
            newReplication.setSocketTimeoutMillis(replicationRequest.getSocketTimeoutMillis());
        }
    }

    public static void verifyBaseReplicationRequest(ReplicationBaseDescriptor replication) {
        if (StringUtils.isBlank(replication.getCronExp())) {
            throw new BadRequestException("cronExp is required");
        }
        if (!CronExpression.isValidExpression(replication.getCronExp())) {
            throw new BadRequestException("Invalid cronExp");
        }
    }

    public static void verifyLocalReplicationRequest(LocalReplicationDescriptor replication) {
        if (StringUtils.isBlank(replication.getUrl())) {
            throw new BadRequestException("url is required");
        }
        try {
            new URL(replication.getUrl());
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid url [" + e.getMessage() + "]");
        }
        verifyBaseReplicationRequest(replication);
        if (StringUtils.isBlank(replication.getUsername())) {
            throw new BadRequestException("username is required");
        }
    }
}
