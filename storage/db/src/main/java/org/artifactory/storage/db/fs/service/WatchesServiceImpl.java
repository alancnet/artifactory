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

package org.artifactory.storage.db.fs.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.addon.ha.message.HaMessageTopic;
import org.artifactory.addon.ha.message.WatchesHaMessage;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.fs.MutableWatchersInfo;
import org.artifactory.fs.WatcherInfo;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.model.WatcherRepoPathInfo;
import org.artifactory.model.xstream.fs.WatcherImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.fs.dao.WatchesDao;
import org.artifactory.storage.db.fs.entity.Watch;
import org.artifactory.storage.fs.VfsException;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.storage.fs.service.InternalWatchesService;
import org.artifactory.storage.fs.service.WatchesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Yossi Shaul
 */
@Service
public class WatchesServiceImpl implements WatchesService, InternalWatchesService {
    private static final Logger log = LoggerFactory.getLogger(WatchesServiceImpl.class);

    @Autowired
    private DbService dbService;

    @Autowired
    private WatchesDao watchesDao;

    @Autowired
    private FileService fileService;

    private Multimap<RepoPath, Watch> watchersCache;
    private volatile boolean initialized = false;

    @Override
    public WatchersInfo getWatches(RepoPath repoPath) {
        MutableWatchersInfo watchers = InfoFactoryHolder.get().createWatchers();
        Collection<Watch> nodeWatches = getWatchersFromCache(repoPath);
        for (Watch nodeWatch : nodeWatches) {
            watchers.addWatcher(watchToWatchInfo(nodeWatch));
        }
        return watchers;
    }

    @Override
    public boolean isUserWatchingRepoPath(RepoPath repoPath,String userName){
        boolean isUserWatchingRepoPath = false;
         Collection<Watch> nodeWatches = getWatchersFromCache(repoPath);
        for (Watch nodeWatch : nodeWatches) {
            if (nodeWatch.getUsername().equals(userName)) {
                isUserWatchingRepoPath = true;
            }
        }
        return isUserWatchingRepoPath;
    }

    @Override
    public boolean hasWatches(RepoPath repoPath) {
        return getWatchersCache().containsKey(repoPath);
    }

    @Override
    @Nonnull
    public List<WatcherRepoPathInfo> loadWatches() {
        Collection<Map.Entry<RepoPath, Watch>> allWatchers = getAllWatchersFromCache();
        List<WatcherRepoPathInfo> watchersInfo = Lists.newArrayList();
        for (Map.Entry<RepoPath, Watch> watchEntry : allWatchers) {
            RepoPath repoPath = watchEntry.getKey();
            WatcherInfo watcherInfo = watchToWatchInfo(watchEntry.getValue());
            watchersInfo.add(new WatcherRepoPathInfo(repoPath, watcherInfo));
        }

        return watchersInfo;
    }

    @Override
    public void addWatches(long nodeId, List<WatcherInfo> watches) {
        for (WatcherInfo watch : watches) {
            addWatch(nodeId, watch);
        }
    }

    @Nonnull
    @Override
    public int addWatch(long nodeId, WatcherInfo watchInfo) {
        int updateCount = internalAddWatch(nodeId, watchInfo);
        notify(new WatchesHaMessage.AddWatch(nodeId, watchInfo));
        return updateCount;
    }

    @Override
    public int deleteWatches(long nodeId) {
        try {
            int deletedCount = watchesDao.deleteWatches(nodeId);
            if (deletedCount > 0) {
                RepoPath repoPath = fileService.loadItem(nodeId).getRepoPath();
                deleteAllWatchesForRepoPath(repoPath);
            }
            return deletedCount;
        } catch (SQLException e) {
            throw new StorageException("Failed to delete watches for node: " + nodeId);
        }
    }

    @Override
    public int deleteUserWatches(RepoPath repoPath, String username) {
        long nodeId = fileService.getNodeId(repoPath);
        try {
            if (nodeId > 0) {
                int deletedCount = watchesDao.deleteUserWatches(nodeId, username);
                if (deletedCount > 0) {
                    deleteUserWatchesFromCache(repoPath, username);
                }
                return deletedCount;
            }
            return 0;
        } catch (SQLException e) {
            throw new StorageException("Failed to delete watches for node: " + nodeId);
        }
    }

    @Override
    public int deleteAllUserWatches(String username) {
        try {
            int deletedCount = watchesDao.deleteAllUserWatches(username);
            if (deletedCount > 0) {
                deleteAllUserWatchesFromCache(username);
            }
            return deletedCount;
        } catch (SQLException e) {
            throw new StorageException("Failed to delete watches for user: " + username);
        }
    }

    private void updateCache(RepoPath repoPath, Watch watch) {
        getWatchersCache().put(repoPath, watch);
    }

    private Collection<Map.Entry<RepoPath, Watch>> getAllWatchersFromCache() {
        return getWatchersCache().entries();
    }

    private Collection<Watch> getWatchersFromCache(RepoPath repoPath) {
        return getWatchersCache().get(repoPath);
    }

    private void deleteAllWatchesForRepoPath(RepoPath repoPath) {
        internalDeleteAllWatchesForRepoPath(repoPath);
        notify(new WatchesHaMessage.DeleteAllWatches(repoPath));
    }

    private void deleteUserWatchesFromCache(RepoPath repoPath, String username) {
        internalDeleteUserWatchesFromCache(repoPath, username);
        notify(new WatchesHaMessage.DeleteUserWatches(repoPath, username));
    }

    private void deleteAllUserWatchesFromCache(String username) {
        internalDeleteAllUserWatchesFromCache(username);
        notify(new WatchesHaMessage.DeleteAllUserWatches(username));
    }

    //
    @Override
    public int internalAddWatch(long nodeId, WatcherInfo watchInfo) {
        log.debug("Adding watch to {}", nodeId);
        long watchId = dbService.nextId();
        Watch watch = watchInfoToWatch(watchId, nodeId, watchInfo);
        int updateCount;
        try {
            updateCount = watchesDao.create(watch);
            if (updateCount > 0) {
                RepoPath repoPath = fileService.loadItem(nodeId).getRepoPath();
                updateCache(repoPath, watch);
            }
            return updateCount;
        } catch (SQLException e) {
            throw new VfsException(e);
        }
    }

    @Override
    public void internalDeleteAllWatchesForRepoPath(RepoPath repoPath) {
        getWatchersCache().removeAll(repoPath);
    }

    @Override
    public void internalDeleteUserWatchesFromCache(RepoPath repoPath, String username) {
        Collection<Watch> userWatches = getWatchersFromCache(repoPath);
        List<Watch> watchesAfterDelete = Lists.newArrayList();
        for (Watch watch : userWatches) {
            if (!StringUtils.equals(watch.getUsername(), username)) {
                watchesAfterDelete.add(watch);
            }
        }

        getWatchersCache().replaceValues(repoPath, watchesAfterDelete);
    }

    @Override
    public void internalDeleteAllUserWatchesFromCache(String username) {
        Collection<Map.Entry<RepoPath, Watch>> allWatches = getAllWatchersFromCache();
        for (Iterator<Map.Entry<RepoPath, Watch>> it = allWatches.iterator(); it.hasNext(); ) {
            Map.Entry<RepoPath, Watch> watchEntry = it.next();
            if (StringUtils.equals(watchEntry.getValue().getUsername(), username)) {
                it.remove();
            }
        }
    }

    private Multimap<RepoPath, Watch> getWatchersCache() {
        lazyInitCacheIfNeeded();
        return watchersCache;
    }

    private void lazyInitCacheIfNeeded() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    if (watchersCache == null) {
                        watchersCache = HashMultimap.create();
                        watchersCache = Multimaps.synchronizedMultimap(watchersCache);
                    }

                    try {
                        //TODO: [by YS] consider using single query to get watch + repo path
                        List<Watch> nodeWatches = watchesDao.getWatches();
                        for (Watch nodeWatch : nodeWatches) {
                            RepoPath repoPath = fileService.loadItem(nodeWatch.getNodeId()).getRepoPath();
                            watchersCache.put(repoPath, nodeWatch);
                        }
                        initialized = true;
                    } catch (SQLException e) {
                        throw new StorageException("Failed to load watches", e);
                    }
                }
            }
        }
    }

    private Watch watchInfoToWatch(long watchId, long nodeId, WatcherInfo watchInfo) {
        return new Watch(watchId, nodeId, watchInfo.getUsername(), watchInfo.getWatchingSinceTime());
    }

    private WatcherInfo watchToWatchInfo(Watch nodeWatch) {
        return new WatcherImpl(nodeWatch.getUsername(), nodeWatch.getSince());
    }

    private void notify(WatchesHaMessage haMessage) {
        HaCommonAddon haAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(HaCommonAddon.class);
        haAddon.notify(HaMessageTopic.WATCHES_TOPIC, haMessage);
    }
}
