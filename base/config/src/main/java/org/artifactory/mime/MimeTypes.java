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

package org.artifactory.mime;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds a list of mime entries.
 *
 * @author Yossi Shaul
 */
@XStreamAlias("mimetypes")
public class MimeTypes implements Serializable {
    /**
     * The xml version (controlled by the converters only)
     */
    @XStreamAsAttribute
    private String version;

    @XStreamImplicit
    private final Set<MimeType> mimeTypes;

    transient private Map<String, MimeType> typeByExtension;

    protected MimeTypes(ImmutableSet<MimeType> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public Set<MimeType> getMimeTypes() {
        return mimeTypes;
    }

    @Nullable
    public MimeType getByMime(String type) {
        for (MimeType mimeType : mimeTypes) {
            if (mimeType.getType().equals(type)) {
                return mimeType;
            }
        }
        return null;
    }

    @Nullable
    public MimeType getByExtension(String extension) {
        if (extension == null) {
            return null;
        }
        if (typeByExtension == null) {
            initializeTypeByExtensionIfNeeded();
        }
        return typeByExtension.get(extension.toLowerCase());
    }

    private synchronized void initializeTypeByExtensionIfNeeded() {
        if (typeByExtension == null) {
            typeByExtension = new HashMap<>(mimeTypes.size());
            for (MimeType mimeType : mimeTypes) {
                for (String extension : mimeType.getExtensions()) {
                    typeByExtension.put(extension, mimeType);
                }
            }
        }
    }
}