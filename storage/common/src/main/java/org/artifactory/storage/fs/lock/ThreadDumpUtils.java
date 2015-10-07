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

package org.artifactory.storage.fs.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yoav Landman
 */
public abstract class ThreadDumpUtils {

    private static final Logger log = LoggerFactory.getLogger(ThreadDumpUtils.class);

    public static void dumpThreads(StringBuilder msg) {
        try {
            log.info("Printing locking debug information...");
            CharSequence dump = new ThreadDumper().dumpThreads();
            msg.append("\n").append(dump);
        } catch (Exception e) {
            log.info("Could not dump threads", e);
        }
    }
}