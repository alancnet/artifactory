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

package org.artifactory.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author Yoav Landman
 */
public abstract class ExpiringDelayed implements Delayed {

    private static final Logger log = LoggerFactory.getLogger(ExpiringDelayed.class);

    private final long expiry;

    /**
     * Creates a new instance.
     *
     * @param expiry In milliseconds
     */
    public ExpiringDelayed(long expiry) {
        this.expiry = expiry;
        if (log.isTraceEnabled()) {
            log.trace("Created delayed entry on {}, expiring on {} for {}.",
                    System.currentTimeMillis(), expiry, getClass().getName());
        }
    }

    /**
     * @return The subject/entry/actual value that this delay is wrapping
     */
    public abstract String getSubject();

    @Override
    public final long getDelay(TimeUnit unit) {
        long millisToExpiry = expiry - System.currentTimeMillis();
        long delay = unit.convert(millisToExpiry < 0 ? 0 : millisToExpiry, TimeUnit.MILLISECONDS);
        return delay;
    }

    @Override
    public final int compareTo(Delayed o) {
        long d = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
        return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && getSubject().equals(o);
    }

    @Override
    public final int hashCode() {
        return getSubject().hashCode();
    }
}