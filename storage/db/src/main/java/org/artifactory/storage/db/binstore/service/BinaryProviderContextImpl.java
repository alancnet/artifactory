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

package org.artifactory.storage.db.binstore.service;

import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.BinaryProviderContext;

import java.util.Set;

/**
 * Date: 12/12/12
 * Time: 3:25 PM
 *
 * @author freds
 */
class BinaryProviderContextImpl implements BinaryProviderContext {
    final BinaryStoreImpl binaryStore;
    final BinaryProviderBase next;

    BinaryProviderContextImpl(BinaryStoreImpl binaryStore, BinaryProviderBase next) {
        this.binaryStore = binaryStore;
        this.next = next;
    }

    @Override
    public Set<String> isInStore(Set<String> sha1List) {
        return binaryStore.isInStore(sha1List);
    }

    /**
     * @see BinaryStoreImpl#isActivelyUsed(java.lang.String)
     */
    @Override
    public boolean isActivelyUsed(String sha1) {
        return binaryStore.isActivelyUsed(sha1);
    }

    @Override
    public BinaryProviderBase next() {
        return next;
    }
}
