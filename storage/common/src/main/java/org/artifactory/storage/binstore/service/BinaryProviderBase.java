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

package org.artifactory.storage.binstore.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.StorageProperties;
import org.artifactory.storage.config.model.Property;
import org.artifactory.storage.config.model.ProviderMetaData;

import java.util.List;
import java.util.Set;

/**
 * Date: 12/12/12
 * Time: 3:03 PM
 *
 * @author freds
 */
public abstract class BinaryProviderBase implements BinaryProvider {
    private InternalBinaryStore binaryStore;
    private ProviderMetaData providerMetaData;
    private StorageProperties storageProperties;
    private BinaryProviderBase binaryProvider;
    private List<BinaryProviderBase> subBinaryProviders = Lists.newArrayList();
    private BinaryProviderBase empty;

    public void initialize() {
    }

    public void setEmpty(BinaryProviderBase empty) {
        this.empty = empty;
    }

    protected String getParam(String name, String defaultValue) {
        String param = providerMetaData.getParamValue(name);
        if (StringUtils.isBlank(param)) {
            param = defaultValue;
        }
        return param;
    }

    protected Set<Property> getproperties() {
        return providerMetaData.getProperties();
    }

    protected int getIntParam(String name, int defaultValue) {
        String param = providerMetaData.getParamValue(name);
        if (StringUtils.isBlank(param)) {
            return defaultValue;
        }
        return Integer.valueOf(param);
    }

    protected long getLongParam(String name, long defaultValue) {
        String param = providerMetaData.getParamValue(name);
        if (StringUtils.isBlank(param)) {
            return defaultValue;
        }
        return Long.valueOf(param);
    }


    public BinaryProviderBase next() {
        if (binaryProvider == null) {
            return empty;
        }
        return binaryProvider;
    }

    public void addSubBinaryProvider(BinaryProviderBase binaryProviderBase) {
        subBinaryProviders.add(binaryProviderBase);
    }

    public InternalBinaryStore getBinaryStore() {
        return binaryStore;
    }

    public void setBinaryStore(InternalBinaryStore binaryStore) {
        this.binaryStore = binaryStore;
    }

    public StorageProperties getStorageProperties() {
        return storageProperties;
    }

    public void setStorageProperties(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public ProviderMetaData getProviderMetaData() {
        return providerMetaData;
    }

    public void setProviderMetaData(ProviderMetaData providerMetaData) {
        this.providerMetaData = providerMetaData;
    }

    public BinaryProviderBase getBinaryProvider() {
        return binaryProvider;
    }

    public void setBinaryProvider(BinaryProviderBase binaryProviderBase) {
        this.binaryProvider = binaryProviderBase;
    }

    public List<BinaryProviderBase> getSubBinaryProviders() {
        return subBinaryProviders;
    }


}
