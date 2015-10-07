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

package org.artifactory.util;

import java.io.File;

/**
 * Check every now and then that a certain file has not changed. If it has, then call the {@link #doOnChange} method.
 * Based on the log4j FileWatchdog implementation.
 *
 * @author Yossi Shaul
 */
public abstract class FileWatchDog extends Thread {
    /**
     * The default delay between every file modification check, set to 30 seconds.
     */
    public static final long DEFAULT_DELAY = 30000;

    /**
     * The delay to observe between every check. By default set {@link #DEFAULT_DELAY}.
     */
    protected long delay = DEFAULT_DELAY;

    protected final File file;
    private long lastChanged = 0;
    private boolean warnedAlready = false;
    private boolean interrupted = false;

    /**
     * Creates a new watch dog thread.
     *
     * @param file     The file to watch
     * @param checkNow If true will check the file status before the constructor returns
     */
    protected FileWatchDog(File file, boolean checkNow) {
        this.file = file;
        setDaemon(true);
        if (checkNow) {
            checkAndConfigure();
        } else {
            lastChanged = System.currentTimeMillis();
        }
    }

    /**
     * Set the delay to observe between each check of the file changes.
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    protected abstract void doOnChange();

    protected void checkAndConfigure() {
        boolean fileExists;
        try {
            fileExists = file.exists();
        } catch (SecurityException e) {
            System.err.printf("Check for file existence - read denied, file: [%s].", file.getAbsolutePath());
            interrupted = true;// there is no point in continuing
            return;
        }

        if (fileExists) {
            long l = file.lastModified();
            if (l > lastChanged) {
                lastChanged = l;
                doOnChange();
                warnedAlready = false;
            }
        } else {
            if (!warnedAlready) {
                System.err.printf("[%s] does not exist.", file.getAbsolutePath());
                warnedAlready = true;
            }
        }
    }

    @Override
    public void run() {
        while (!interrupted) {
            try {
                sleep(delay);
            } catch (InterruptedException e) {
                interrupted = true;
            }
            checkAndConfigure();
        }
    }
}
