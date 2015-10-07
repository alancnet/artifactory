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

package org.artifactory.storage.fs.tree.file;

import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.model.xstream.fs.FolderInfoImpl;
import org.artifactory.storage.fs.tree.FileNode;
import org.artifactory.storage.fs.tree.FolderNode;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

/**
 * Units tests for the {@link org.artifactory.storage.fs.tree.file.JavaIOFileAdapter}.
 *
 * @author Yossi Shaul
 */
@Test
public class JavaIOFileAdapterTest {

    public void fileInfoAdapter() {
        FileInfoImpl fileInfo = new FileInfoImpl(new RepoPathImpl("repoX", "test/file.txt"));
        JavaIOFileAdapter fileAdapter = new JavaIOFileAdapter(new FileNode(fileInfo));

        assertFalse(fileAdapter.isDirectory());
        assertTrue(fileAdapter.isFile());
        assertEquals(fileAdapter.getRepoPath(), fileInfo.getRepoPath());
        assertEquals(fileAdapter.getFileInfo(), fileInfo);
        assertEquals(fileAdapter.getAbsolutePath(), new File("repoX/test/file.txt").getPath());
        assertFalse(fileAdapter.canWrite());
    }

    public void folderInfoAdapter() {
        FolderInfoImpl folderInfo = new FolderInfoImpl(new RepoPathImpl("repoX", "test/folder/"));
        JavaIOFileAdapter folderAdapter = new JavaIOFileAdapter(new FolderNode(folderInfo, null));

        assertTrue(folderAdapter.isDirectory());
        assertFalse(folderAdapter.isFile());
        assertEquals(folderAdapter.getRepoPath(), folderInfo.getRepoPath());
        assertEquals(folderAdapter.getInfo(), folderInfo);
        assertEquals(folderAdapter.getAbsolutePath(), new File("repoX/test/folder").getPath());
    }

}
