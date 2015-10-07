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

package org.artifactory.md;

import java.io.Serializable;

/**
 * @author freds
 * @date Sep 3, 2008
 */
public class MetadataDefinition<T, MT> implements Serializable {
    /**
     * A Java class that can be marshall/unmarshall this metadata XML stream.
     */
    private final XmlMetadataProvider<T, MT> xmlProvider;
    /**
     * A Java class that can read and save this metadata to the underlying storage.
     */
    private final MetadataPersistenceHandler<T, MT> persistenceHandler;
    /**
     * If true this metadata will not be display as part of metadata names for an fs item.
     */
    private final boolean internal;

    public MetadataDefinition(XmlMetadataProvider<T, MT> xmlProvider,
            MetadataPersistenceHandler<T, MT> persistenceHandler, boolean internal) {
        this.xmlProvider = xmlProvider;
        this.persistenceHandler = persistenceHandler;
        this.internal = internal;
    }

    public XmlMetadataProvider<T, MT> getXmlProvider() {
        return xmlProvider;
    }

    public MetadataPersistenceHandler<T, MT> getPersistenceHandler() {
        return persistenceHandler;
    }

    public String getMetadataName() {
        return xmlProvider.getMetadataName();
    }

    public boolean isInternal() {
        return internal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetadataDefinition that = (MetadataDefinition) o;
        return getMetadataName().equals(that.getMetadataName());
    }

    @Override
    public int hashCode() {
        return getMetadataName().hashCode();
    }

    @Override
    public String toString() {
        return "MetadataDefinition{" +
                "metadataName='" + getMetadataName() + '\'' +
                ", persistentClass=" + persistenceHandler.getClass().getName() +
                ", xmlProviderClass=" + xmlProvider.getClass().getName() +
                '}';
    }
}
