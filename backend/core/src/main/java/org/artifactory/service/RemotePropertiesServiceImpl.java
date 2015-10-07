package org.artifactory.service;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.PropertiesAddon;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.RepoResource;
import org.artifactory.md.Properties;
import org.artifactory.repo.HttpRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.cache.expirable.CacheExpiry;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.request.RepoRequests;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by Michael Pasternak on 9/1/15.
 */
@Service
public class RemotePropertiesServiceImpl implements RemotePropertiesService {

    private static final Logger log = LoggerFactory.getLogger(RemotePropertiesServiceImpl.class);

    @Autowired
    private InternalRepositoryService repositoryService;

    /**
     * Updates properties from remote repository (if expired)
     *
     * @param resource an resource to update properties for
     *
     * @return success/failure
     */
    @Override
    public boolean update(RepoResource resource) {
        try {
            String repoKey = StringUtils.replaceLast(resource.getRepoPath().getRepoKey(), "-cache", "");
            HttpRepoDescriptor descriptor = (HttpRepoDescriptor) repositoryService.remoteRepoDescriptorByKey(repoKey);
            if (!shouldPull(descriptor)) {
                return false;
            }
            return doPull(resource);
        }catch (Exception e){
            log.debug("Smart repo failed to get properties from remote repo :" + resource.getRepoPath(), e);
            log.error("Smart repo failed to get properties from remote repo '{}'",resource.getRepoPath());
            return false;
        }
    }

    /**
     * Checks whether current state is applicable for the properties update
     *
     * @param descriptor
     *
     * @return success/failure
     */
    private boolean shouldPull(HttpRepoDescriptor descriptor) {
        if (descriptor == null) {
            log.debug("Not performing content synchronization due to missing HttpRepoDescriptor in context," +
                    " for instance, this may happen if repoKey is incorrect");
            return false;
        }

        if (descriptor.getContentSynchronisation() == null) {
            log.debug("Not performing content synchronization due to missing ContentSynchronisation config");
            return false;
        }

        if (!descriptor.getContentSynchronisation().isEnabled() ||
                !descriptor.getContentSynchronisation().getProperties().isEnabled()) {
            log.debug("Not performing content synchronization due to disabled ContentSynchronisation");
            return false;
        }
        return true;
    }

    /**
     * Performs local properties update
     *
     * @param resource
     *
     * @return success/failure
     */
    private boolean doPull(RepoResource resource) {
        log.debug("Downloading properties for artifact {}", resource);
        String repoKey = StringUtils.replaceLast(resource.getRepoPath().getRepoKey(), "-cache", "");
        HttpRepo repo = (HttpRepo)repositoryService.remoteRepositoryByKey(repoKey);
        // If file doesn't exist then do mot update properties since it will be updated during file download
        if (!repositoryService.exists(resource.getRepoPath())) {
            return false;
        }
        // If properties not expire the return false, no need to get properties drom the remote repo
        if (repo == null || resourceIsExpirable(resource,repo) ||!isPropertiesExpired(resource, repo) ) {
            return false;
        }
        String remotePath = repo.getUrl() + "/" + resource.getRepoPath().getPath() + ":properties";
        HttpGet getMethod = new HttpGet(HttpUtils.encodeQuery(remotePath));
        try {
            CloseableHttpResponse getResponse = repo.executeMethod(getMethod);
            boolean ok = HttpStatus.SC_OK == getResponse.getStatusLine().getStatusCode();
            boolean notFound = HttpStatus.SC_NOT_FOUND == getResponse.getStatusLine().getStatusCode();
            if (ok || notFound) {
                InputStream stream = getResponse.getEntity().getContent();
                Properties properties = (Properties) InfoFactoryHolder.get().createProperties();
                if (ok && stream != null) {
                    RepoRequests.logToContext("Received remote property content");
                    Properties remoteProperties = (Properties) InfoFactoryHolder.get().getFileSystemXStream().fromXML(stream);
                    for (String remotePropertyKey : remoteProperties.keySet()) {
                        Set<String> values = remoteProperties.get(remotePropertyKey);
                        RepoRequests.logToContext("Found remote property key '{}' with values '%s'", remotePropertyKey,
                                values);
                        if (!remotePropertyKey.startsWith(ReplicationAddon.PROP_REPLICATION_PREFIX)) {
                            properties.putAll(remotePropertyKey, values);
                        }
                    }
                }
                updateRemoteProperties(resource, properties);
            }
            repositoryService.unexpireIfExists(repo.getLocalCacheRepo(),resource.getRepoPath().getPath());
        } catch (IOException e) {
            log.debug("Cannot update remote properties", e);
            return false;
        }
        return true;
    }

    private boolean resourceIsExpirable(RepoResource resource, HttpRepo repo) {
        // If the file is expirable then the expirable mechanism is responsible to update the file and the properties
        String path = resource.getRepoPath().getPath();
        CacheExpiry cacheExpiry = ContextHelper.get().beanForType(CacheExpiry.class);
        return cacheExpiry.isExpirable(repo.getLocalCacheRepo(), path);
    }

    /**
     * update remote Properties
     *
     * @param resource   - repo resource
     * @param properties - properties
     */
    private void updateRemoteProperties(RepoResource resource, Properties properties) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        PropertiesAddon propertiesAddon = addonsManager.addonByType(PropertiesAddon.class);
        propertiesAddon.setProperties(resource.getRepoPath(), properties);
    }

    /**
     * Checks whether given resource's properties cache has expired
     *
     * @param resource - repo resource
     * @param repo - http repo
     *
     * @return yes/no
     */
    private boolean isPropertiesExpired(RepoResource resource, HttpRepo repo) {
        RepoPath repoPath = resource.getRepoPath();
        long lastUpdated = repositoryService.getFileInfo(repoPath).getLastUpdated();
        long cacheAge = System.currentTimeMillis() - lastUpdated;
        long retrievalCachePeriodMillis = repo.getRetrievalCachePeriodSecs() * 1000L;
        // If cache age is less than retrieval cache period then do not update properties.
        if (cacheAge < retrievalCachePeriodMillis) {
            return false;
        }
        return true;
    }
}
