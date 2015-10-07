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

package org.artifactory.maven;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferEventSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Yoav Landman
 */
public class TransferProgressReportingInputStream extends FileInputStream {

    private TransferEventSupport eventSupport;
    private TransferEvent event;

    public TransferProgressReportingInputStream(File file, TransferEventSupport eventSupport,
            TransferEvent event)
            throws FileNotFoundException {
        super(file);
        this.eventSupport = eventSupport;
        this.event = event;
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read() throws IOException {
        byte b[] = new byte[1];
        return read(b);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int retValue = super.read(b, off, len);
        if (retValue > 0) {
            event.setTimestamp(System.currentTimeMillis());
            eventSupport.fireTransferProgress(event, b, retValue);
        }
        return retValue;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        eventSupport.fireTransferStarted(event);
    }
}
