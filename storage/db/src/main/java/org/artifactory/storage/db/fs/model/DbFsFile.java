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

package org.artifactory.storage.db.fs.model;

import org.artifactory.fs.FileInfo;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.storage.fs.repo.StoringRepo;

import java.io.InputStream;

/**
 * yossi
 * Virtual filesystem file backed by a database record.
 *
 * @author Yossi Shaul
 */
public class DbFsFile extends DbFsItem<FileInfo> implements VfsFile<FileInfo> {

    public DbFsFile(StoringRepo storingRepo, long id, FileInfo info) {
        super(storingRepo, id, info);
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public long length() {
        return getInfo().getSize();
    }

    @Override
    public String getSha1() {
        return getInfo().getSha1();
    }

    @Override
    public String getMd5() {
        return getInfo().getMd5();
    }

    @Override
    public InputStream getStream() {
        return getBinariesService().getBinary(getSha1());
    }
}
