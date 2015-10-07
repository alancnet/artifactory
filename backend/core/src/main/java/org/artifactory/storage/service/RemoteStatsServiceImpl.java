package org.artifactory.storage.service;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.semaphore.SemaphoreWrapper;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.rest.constant.ArtifactRestConstants;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.StatsImpl;
import org.artifactory.repo.HttpRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.storage.db.fs.entity.Stat;
import org.artifactory.storage.db.fs.service.AbstractStatsService;
import org.artifactory.util.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Michael Pasternak on 8/25/15.
 */
@Service
public class RemoteStatsServiceImpl extends AbstractStatsService implements RemoteStatsService {

    // queue interpretation:    destination |  source | content to send
    private volatile ConcurrentMap<String, Map<String, Collection<StatsEvent>>> delegationQueue = Maps.newConcurrentMap();
    private static final Logger log = LoggerFactory.getLogger(RemoteStatsServiceImpl.class);
    private static final String ARTIFACTORY_APP_PATH = "/artifactory";

    private static final String REMOTE_STATS_ARTIFACTORY_API_PATH = ARTIFACTORY_APP_PATH + "/api/"+
            ArtifactRestConstants.PATH_ROOT+"/"+ArtifactRestConstants.PATH_STATISTICS+"/";

    private static final String REMOTE_STATS_API_PATH = "/api/"+
            ArtifactRestConstants.PATH_ROOT+"/"+ArtifactRestConstants.PATH_STATISTICS+"/";

    public static final String CACHE_SUFFIX = "-cache";

    private SemaphoreWrapper flushingSemaphore;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private InternalRepositoryService repoService;

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }


    /**
     * Queueing event for delegation,
     * (triggered on artifact local download event)
     *
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     */
    public synchronized void fileDownloaded(RepoPath repoPath,
            String downloadedBy, long downloadedTime) {
        StatsEvent statsEvent = getStatsEvents().get(repoPath);
        if (statsEvent == null) {
            getStatsEvents().put(repoPath, statsEvent = new StatsEvent(repoPath, null));
        }
        statsEvent.update(downloadedBy, null, downloadedTime, 1);
    }

    /**
     * Queueing event for delegation,
     * (triggered on artifact remote download event)
     *
     * @param origin         The origin hosts
     * @param repoPath       The file repo path to set/update stats
     * @param downloadedBy   User who downloaded the file
     * @param downloadedTime Time the file was downloaded
     * @param count          Amount of performed downloads
     */
    @Override
    public synchronized void fileDownloaded(String origin, RepoPath repoPath,
            String downloadedBy, long downloadedTime, long count) {
        String source = getRequestInitiator(origin, downloadedBy);
        StatsEvent statsEvent = getStatsEvents().get(repoPath);
        if (statsEvent == null) {
            getStatsEvents().put(repoPath, statsEvent = new StatsEvent(repoPath, source));
        }
        statsEvent.update(downloadedBy, source, downloadedTime, count);
    }

    /**
     * Queue statistics for delegation to remote Artifactory instance,
     * queueing grouping is based on RemoteRepo target. e.g actual remote
     * repository
     *
     * @param event stats event
     * @param nodeId id representing artifact location
     * @param stats statistics
     *
     * @throws java.sql.SQLException
     */
    @Override
    protected void updateStats(StatsEvent event, long nodeId, Stat stats) throws SQLException {
        String localRepoKey = event.getRepoPath().getRepoKey();
        RemoteRepoDescriptor remoteRepoDescriptor =
                getRepositoryService().remoteRepoDescriptorByKey(fromCachedRepoKey(localRepoKey));
        if (remoteRepoDescriptor != null) {
            String remoteRepoKey = getRemoteRepoKey(remoteRepoDescriptor);
            if(!Strings.isNullOrEmpty(remoteRepoKey)) {
                log.debug("Event {} saved for delegation to repo {}", event, remoteRepoKey);
                getQueuedStatsEvents(localRepoKey, remoteRepoKey).add(event);
            } else {
                log.debug("Can't find remoteRepoKey by URL {}", remoteRepoDescriptor.getUrl());
            }
        } else {
            log.debug("Can't find repoDescriptor by id {}", localRepoKey);
        }
    }

    /**
     * Locates actual remote repoKey by RemoteRepoDescriptor#Url
     *
     * @param remoteRepoDescriptor
     *
     * @return remote repoKey
     */
    private String getRemoteRepoKey(RemoteRepoDescriptor remoteRepoDescriptor) {
        try {
            URI uri = URI.create(remoteRepoDescriptor.getUrl());
            if(uri != null) {
                String[] pathItems = uri.getPath().split("/");
                if(pathItems.length > 0)
                    return pathItems[pathItems.length-1];
            }
            log.debug("Error on converting RepoDescriptor Url to URI");
        } catch (Exception ex) {
            log.debug("Error on converting RepoDescriptor Url to URI: {}", ex);
        }
        return null;
    }

    @Override
    protected void onTraversingStart() {
        log.debug("Starting events grouping for processing");
    }

    /**
     * In remote context, this method triggers actual grouped
     * events delegation
     */
    @Override
    protected void onTraversingEnd() {
        log.debug("Finished events grouping, delegating ...");

        Iterator<Map.Entry<String, Map<String, Collection<StatsEvent>>>> iterator
                = delegationQueue.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<String, Map<String, Collection<StatsEvent>>> entry = iterator.next();
            final String repoKey = entry.getKey();
            final Map<String, Collection<StatsEvent>> events = entry.getValue();
            iterator.remove();
            delegateStats(repoKey, events);
        }
    }

    /**
     * Fetches collection of events targeted to remoteRepoKey grouped by localRepoKey
     *
     * @param localRepoKey
     * @param remoteRepoKey
     *
     * @return a collection of events
     */
    private Collection<StatsEvent> getQueuedStatsEvents(String localRepoKey, String remoteRepoKey) {
        Map<String, Collection<StatsEvent>> events = delegationQueue.get(remoteRepoKey);
        if (events == null) {
            synchronized (delegationQueue) {
                events = delegationQueue.get(remoteRepoKey);
                if(events == null) {
                    delegationQueue.put(remoteRepoKey, new HashMap<>());
                    events = delegationQueue.get(remoteRepoKey);
                    events.put(localRepoKey, new ConcurrentLinkedQueue<>());
                }
            }
        }
        if (events.get(localRepoKey) == null) {
            synchronized (delegationQueue) {
                if (events.get(localRepoKey) == null){
                    events.put(localRepoKey, new ConcurrentLinkedQueue<>());
                }
            }
        }
        return events.get(localRepoKey);
    }

    /**
     * Performs statistics delegation to RemoteRepository
     *
     * @param events an events to delegated (grouped by source repo)
     *
     * @return if item/s was/where delegated or no
     */
    private boolean delegateStats(String repoKey, Map<String, Collection<StatsEvent>> events) {

        if (!filterEvents(events)) {
            return false;
        }

        log.debug("Delegating event/s to remote repo \"{}\"", repoKey);
        return doDelegate(repoKey, events);
    }

    /**
     * Filters out events not applicable for delegation
     *
     * @param events an events to delegated (grouped by source repo)
     *
     * @return if delegation should occur (e.g any events left)
     */
    private boolean filterEvents(Map<String, Collection<StatsEvent>> events) {

        boolean shouldRemove;
        Iterator<Map.Entry<String, Collection<StatsEvent>>> iterator = events.entrySet().iterator();
        while (iterator.hasNext()) {

            shouldRemove = false;
            final Map.Entry<String, Collection<StatsEvent>> event = iterator.next();
            String repoKey = fromCachedRepoKey(event.getKey());

            if (events.size() == 0) return false;

            HttpRepoDescriptor descriptor =
                    (HttpRepoDescriptor) getRepositoryService()
                            .remoteRepoDescriptorByKey(repoKey);

            if (descriptor == null) {
                log.debug("Not performing content synchronization due to missing HttpRepoDescriptor in context," +
                        " for instance, this may happen if current repository is LOCAL or repoKey is incorrect");
                shouldRemove = true;
            }

            if (descriptor.getContentSynchronisation() == null) {
                log.debug("Not performing content synchronization due to missing ContentSynchronisation config");
                shouldRemove = true;
            }

            if (!descriptor.getContentSynchronisation().isEnabled() ||
                    !descriptor.getContentSynchronisation().getStatistics().isEnabled()) {
                log.debug("Not performing content synchronization due to disabled ContentSynchronisation");
                shouldRemove = true;
            }

            if(shouldRemove) iterator.remove();
        }

        return !events.isEmpty();
    }

    /**
     * actual content delegation in groups targeted to specific
     * remoteRepo
     *
     * @param remoteRepoKey
     * @param eventsCollection
     *
     * @return is event/s where delegated
     */
    private boolean doDelegate(String remoteRepoKey, Map<String, Collection<StatsEvent>> eventsCollection) {

        boolean succeeded=true;
        HttpRepoDescriptor descriptor;
        for (Map.Entry<String, Collection<StatsEvent>> localRepo : eventsCollection.entrySet()) {
            String localRemoteRepoKey = fromCachedRepoKey(localRepo.getKey());
            Collection<StatsEvent> events = localRepo.getValue();
            descriptor = (HttpRepoDescriptor) getRepositoryService().remoteRepoDescriptorByKey(localRemoteRepoKey);

            // though we grouping events by (target) remoteRepo, we fire them
            // in sub-groups per (local) remoteRepo (initiated these events) cause
            // it has own configuration (user/password/proxy/timeout/etc.)

            if(descriptor != null) {
                try {
                    if(events.size() == 0) continue;
                    CloseableHttpResponse response = null;
                    URI uri = URI.create(descriptor.getUrl());
                    if (uri != null) {
                        String actualUrl = produceRequestUrl(uri);
                        HttpPut putMethod = new HttpPut(actualUrl);
                        putMethod.setEntity(new StringEntity(serialize(toStatsImpl(remoteRepoKey, events)),
                                        ContentType.APPLICATION_JSON)
                        );
                        putMethod.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
                        response = request(putMethod, descriptor);
                        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            log.debug("Successfully delegated content to \"{}\" from \"{}\"", remoteRepoKey, localRemoteRepoKey);
                        } else {
                            succeeded &= false;
                            if (response != null)
                                log.debug("Bad response \"{}\" from delegation request", response.getStatusLine());
                        }
                    } else {
                        succeeded &= false;
                        log.warn("Could not combine URI from descriptor url '{}'",
                                descriptor.getUrl());
                    }
                } catch (IllegalArgumentException | IOException ex) {
                    succeeded &= false;
                    log.error("Error during content delegation", ex);

                }
            } else {
                log.debug("Can't find local remoteRepo by key {} from delegation targeted events", localRemoteRepoKey);
            }
        }

        return succeeded;
    }

    /**
     * Converts StatsEvent to StatsImpl
     *
     * (StatsEvent is not available in artifactory-api layer)
     *
     * @param remoteRepoKey
     * @param statsEvents
     *
     * @return {@link StatsImpl}
     */
    private StatsImpl[] toStatsImpl(String remoteRepoKey, Collection<StatsEvent> statsEvents) {
        StatsImpl[] stats = new StatsImpl[statsEvents.size()];
        int i=-1;

        for(StatsEvent statsEvent : statsEvents) {
            StatsImpl statsImpl = new StatsImpl();
            statsImpl.setDownloadCount(statsEvent.getLocalEventCount().get());
            statsImpl.setLastDownloaded(statsEvent.getLocalDownloadedTime());
            statsImpl.setLastDownloadedBy(statsEvent.getLocalDownloadedBy());

            statsImpl.setRemoteDownloadCount(statsEvent.getRemoteEventCount().get());
            statsImpl.setRemoteLastDownloaded(statsEvent.getRemoteDownloadedTime());
            statsImpl.setRemoteLastDownloadedBy(statsEvent.getRemoteDownloadedBy());

            statsImpl.setRepoPath(statsEvent.getRepoPath().getPath());

            stats[++i] = statsImpl;
        }

        return stats;
    }

    /**
     * Produces url to be used against target host
     * @param uri original URI
     *
     * @return url to be used
     */
    private String produceRequestUrl(URI uri) {
        String[] uriParts = uri.getPath().split("/");
        String remoteRepoKey = uriParts[uriParts.length - 1];
        return new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost())
                .append(uri.getPort() != -1 ?
                                ":" + uri.getPort()
                                :
                                ""
                )
                .append(uri.getPath().startsWith(ARTIFACTORY_APP_PATH) ?
                                REMOTE_STATS_ARTIFACTORY_API_PATH
                                :
                                REMOTE_STATS_API_PATH
                )
                .append(toCachedRepoKey(remoteRepoKey))
                .toString();
    }

    /**
     * Converts repoKey to repoKey-cache
     *
     * @param remoteRepoKey
     *
     * @return remote repoKey
     */
    private String toCachedRepoKey(String remoteRepoKey) {
        boolean isLocalRepo = repoService
                .localRepositoryByKey(remoteRepoKey) != null;

        return (remoteRepoKey + (isLocalRepo || remoteRepoKey.endsWith(CACHE_SUFFIX) ?
                ""
                :
                CACHE_SUFFIX)
        );
    }

    /**
     * Extracts repoKey from repoKey-cache
     *
     * @param repoKey
     *
     * @return remote repoKey
     */
    private String fromCachedRepoKey(String repoKey) {
        if (!Strings.isNullOrEmpty(repoKey) && repoKey.endsWith(CACHE_SUFFIX))
            repoKey = StringUtils.replaceLast(repoKey, CACHE_SUFFIX, "");
        return repoKey;
    }

    @Override
    protected SemaphoreWrapper getFlushingSemaphore() {
        if (flushingSemaphore == null) {
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            HaCommonAddon haCommonAddon = addonsManager.addonByType(HaCommonAddon.class);
            flushingSemaphore = haCommonAddon.getSemaphore(HaCommonAddon.STATS_REMOTE_SEMAPHORE_NAME);
        }
        return flushingSemaphore;
    }

    /**
     * Serializes given item to json
     *
     * @param item an object to serialize
     *
     * @return serialized json string
     *
     * @throws IOException
     */
    private <T> String serialize(T item) throws IOException {
        return getObjectMapper().writeValueAsString(item);
    }

    /**
     * Creates a Jackson object mapper
     *
     * @return {@link org.codehaus.jackson.map.ObjectMapper}
     */
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Executes HttpMethod against RemoteRepo target
     *
     * @param method
     *
     * @return {@link CloseableHttpResponse}
     */
    private CloseableHttpResponse request(HttpRequestBase method, HttpRepoDescriptor descriptor) {
        HttpRepo httpRepo = getHttpRepo(descriptor);
        if (httpRepo != null) {
            try {
                return httpRepo.executeMethod(method);
            } catch (IOException e) {
                log.debug("Executing remote statistics delegation to \"{}\" has failed due to:\n{}",
                        descriptor.getUrl(),
                        e
                );
            }
        } else {
            log.debug("RemoteRepo \"{}\" was not found",
                    descriptor.getKey()
            );
        }
        return null;
    }

    /**
     * Fetches HttpRepo by HttpRepoDescriptor
     *
     * @param descriptor
     *
     * @return {@link HttpRepo}
     */
    private HttpRepo getHttpRepo(HttpRepoDescriptor descriptor) {
        return  (HttpRepo) repoService.remoteRepositoryByKey(
                descriptor.getKey()
        );
    }
}
