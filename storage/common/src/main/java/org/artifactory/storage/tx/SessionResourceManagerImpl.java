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

package org.artifactory.storage.tx;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-session single threaded session resources (listeners) manager
 *
 * @author freds
 * @date Sep 22, 2008
 */
public class SessionResourceManagerImpl implements SessionResourceManager {
    private static final Logger log = LoggerFactory.getLogger(SessionResourceManagerImpl.class);

    private final Map<Class, SessionResource> resources = new HashMap<>();

    @Override
    @SuppressWarnings({"unchecked"})
    public <T extends SessionResource> T getOrCreateResource(Class<T> resourceClass) {
        T result = (T) resources.get(resourceClass);
        if (result == null) {
            try {
                result = resourceClass.newInstance();
                resources.put(resourceClass, result);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public void onSessionSave() {
        for (SessionResource resource : resources.values()) {
            resource.onSessionSave();
        }
    }

    @Override
    public void afterCompletion(boolean commit) {
        RuntimeException ex = null;
        for (SessionResource resource : resources.values()) {
            try {
                //Always call after completion on resources
                resource.afterCompletion(commit);
            } catch (RuntimeException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error while releasing resources " + resource + " : " + e.getMessage(), e);
                }
                ex = e;
            }
        }
        if (ex != null) {
            throw ex;
        }
    }

    @Override
    public boolean hasPendingResources() {
        for (SessionResource resource : resources.values()) {
            if (resource.hasPendingResources()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<SessionResource> pendingResources() {
        List<SessionResource> pending = Lists.newArrayList();
        for (SessionResource resource : resources.values()) {
            if (resource.hasPendingResources()) {
                pending.add(resource);
            }
        }
        return pending;
    }
}
