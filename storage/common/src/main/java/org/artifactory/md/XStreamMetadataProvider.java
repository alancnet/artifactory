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

import com.thoughtworks.xstream.XStream;
import org.artifactory.factory.InfoFactoryHolder;

/**
 * @author freds
 */
public abstract class XStreamMetadataProvider<T, MT> implements XmlMetadataProvider<T, MT> {

    private final XStream xstream;
    private final String metadataName;

    public XStreamMetadataProvider(String metadataName) {
        if (metadataName == null) {
            throw new IllegalArgumentException("Metadata name mandatory");
        }
        this.xstream = InfoFactoryHolder.get().getFileSystemXStream();
        this.metadataName = metadataName;
    }

    public XStream getXstream() {
        return xstream;
    }

    @Override
    public String getMetadataName() {
        return metadataName;
    }
}
