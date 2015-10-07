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

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.storage.fs.tree.ItemNode;
import org.artifactory.util.PathUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * A read only adapter for frameworks that expect to work with {@link java.io.File}.
 * This class is backed by {@link org.artifactory.storage.fs.tree.ItemTree} and keeps reference to its parent.
 *
 * @author Yossi Shaul
 */
public class JavaIOFileAdapter extends File {

    /**
     * Parent file of the current file. Null if this is the root node.
     */
    private final JavaIOFileAdapter parent;

    private final String absolutePath;
    private final ItemNode itemNode;
    private final ItemInfo info;

    public JavaIOFileAdapter(ItemNode itemNode) {
        this(itemNode, null);
    }

    public JavaIOFileAdapter(ItemNode itemNode, JavaIOFileAdapter parent) {
        super(itemNode.getRepoPath().toPath());
        this.itemNode = itemNode;
        this.parent = parent;
        this.absolutePath = super.getPath();
        info = itemNode.getItemInfo();
    }

    public ItemInfo getInfo() {
        return info;
    }

    public FileInfo getFileInfo() {
        assert isFile();
        return (FileInfo) info;
    }

    public RepoPath getRepoPath() {
        return info.getRepoPath();
    }

    public InputStream getStream() {
        assert isFile();
        return ContextHelper.get().beanForType(BinaryStore.class).getBinary(getFileInfo().getSha1());
    }

    public JavaIOFileAdapter getChild(String name) {
        if (!isDirectory()) {
            return null;
        }

        JavaIOFileAdapter[] children = listFiles();
        if (children != null) {
            for (JavaIOFileAdapter child : children) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }

    public JavaIOFileAdapter getSibling(String name) {
        JavaIOFileAdapter parentFile = getParentFile();
        if (parentFile == null) {
            return null;
        }

        return parentFile.getChild(name);
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * @return Parent folder of the current file or null if current file is the root
     */
    @Override
    @Nullable
    public JavaIOFileAdapter getParentFile() {
        return parent;
    }

    @Override
    public String getParent() {
        return PathUtils.getParent(getAbsolutePath());
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public File getAbsoluteFile() {
        return this;
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return getAbsolutePath();
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }

    @Override
    public boolean canRead() {
        return getAuthorizationService().canRead(getRepoPath());
    }

    @Override
    public boolean canWrite() {
        return false;   // this is a read only adapter
    }

    @Override
    public boolean exists() {
        return true;    // exists at the time of construction
    }

    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public boolean isHidden() {
        return getName().startsWith(".");
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public int compareTo(File item) {
        //TODO: [by YS] why name and not abs path?
        return getName().compareTo(item.getName());
    }

    @Override
    public String getPath() {
        return getAbsolutePath();
    }

    @Override
    public final long lastModified() {
        return info.getLastModified();
    }

    @Override
    public boolean isDirectory() {
        return info.isFolder();
    }

    @Override
    public long length() {
        if (isDirectory()) {
            return 0;
        } else {
            return getFileInfo().getSize();
        }
    }

    @Override
    public String[] list() {
        if (!isDirectory()) {
            return null;
        } else {
            List<ItemNode> children = itemNode.getChildren();
            String[] childrenNames = new String[children.size()];
            for (int i = 0; i < childrenNames.length; i++) {
                childrenNames[i] = children.get(i).getName();
            }
            return childrenNames;
        }
    }

    @Override
    public JavaIOFileAdapter[] listFiles() {
        if (!isDirectory()) {
            return null;
        } else {
            List<ItemNode> children = itemNode.getChildren();
            JavaIOFileAdapter[] childrenFiles = new JavaIOFileAdapter[children.size()];
            for (int i = 0; i < childrenFiles.length; i++) {
                childrenFiles[i] = new JavaIOFileAdapter(children.get(i), this);
            }
            return childrenFiles;
        }
    }

    @Override
    public String toString() {
        return getAbsolutePath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavaIOFileAdapter)) {
            return false;
        }
        JavaIOFileAdapter item = (JavaIOFileAdapter) o;
        return item.getAbsolutePath().equals(item.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return getAbsolutePath().hashCode();
    }

    @Override
    public boolean setReadOnly() {
        return false;
    }

    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        return false;
    }

    @Override
    public boolean setWritable(boolean writable) {
        return false;
    }

    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        return false;
    }

    @Override
    public boolean setReadable(boolean readable) {
        return false;
    }

    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        return false;
    }

    @Override
    public boolean setExecutable(boolean executable) {
        return false;
    }

    @Override
    public String[] list(FilenameFilter filter) {
        throw new UnsupportedOperationException("list(FilenameFilter) is not supported in VFS.");
    }

    @Override
    public File[] listFiles(FilenameFilter filter) {
        throw new UnsupportedOperationException("listFiles() is not supported in VFS.");
    }

    @Override
    public File[] listFiles(FileFilter filter) {
        throw new UnsupportedOperationException("listFiles(FileFilter) is not supported in VFS.");
    }

    @SuppressWarnings({"deprecation"})
    @Override
    @Deprecated
    public URL toURL() throws MalformedURLException {
        throw new UnsupportedOperationException("toURL() is not supported in VFS.");
    }

    @Override
    public URI toURI() {
        throw new UnsupportedOperationException("toURI() is not supported in VFS.");
    }

    @Override
    public final boolean setLastModified(long time) {
        throw new UnsupportedOperationException("setLastModified() is not supported on VFS");
    }

    @Override
    public boolean mkdir() {
        throw new UnsupportedOperationException("mkdir() is not supported in VFS.");
    }

    @Override
    public boolean mkdirs() {
        throw new UnsupportedOperationException("mkdirs() is not supported in VFS.");
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("delete() is not supported in VFS.");
    }

    @Override
    public boolean createNewFile() throws IOException {
        throw new UnsupportedOperationException("createNewFile() is not supported in VFS.");
    }

    @Override
    public void deleteOnExit() {
        throw new UnsupportedOperationException("deleteOnExit() is not supported in VFS.");
    }

    @Override
    public boolean renameTo(File dest) {
        throw new UnsupportedOperationException("renameTo() is not supported in VFS.");
    }

    @Override
    public long getTotalSpace() {
        throw new UnsupportedOperationException("getTotalSpace() is not supported in VFS.");
    }

    @Override
    public long getFreeSpace() {
        throw new UnsupportedOperationException("getFreeSpace() is not supported in VFS.");
    }

    @Override
    public long getUsableSpace() {
        throw new UnsupportedOperationException("getUsableSpace() is not supported in VFS.");
    }

    private AuthorizationService getAuthorizationService() {
        return ContextHelper.get().getAuthorizationService();
    }
}
