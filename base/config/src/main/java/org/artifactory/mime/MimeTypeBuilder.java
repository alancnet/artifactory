/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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
import org.apache.commons.lang.StringUtils;

/**
 * Builder for {@link MimeType}.
 *
 * @author Yossi Shaul
 */
public class MimeTypeBuilder {
    // @formatter:off
    /** The unique string representation of the mime type (eg, plain/text) */
    private String type;
    /** List of file extensions mapped to this mime type */
    private String[] extensions;
    /** True if this mime type can be viewed as text file */
    private boolean viewable;
    /** True if this mime type should be indexed (applies to xml and archive files) */
    private boolean index;
    /** True if this mime type is a browsable archive (currently zip/jar variants are supported) */
    private boolean archive;
    /** The UI highlighter syntax to for this mime type (only relevant if this is a viewable type) */
    private String syntax;
    /** The css class of a display icon for this mime type */
    private String css;
    // @formatter:on


    public MimeTypeBuilder(String type) {
        this.type = type;
    }

    public MimeType build() {
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("Type cannot be empty");
        }
        return new MimeType(type, ImmutableSet.copyOf(extensions), viewable, index, archive, syntax, css);
    }

    public MimeTypeBuilder extensions(String... extensions) {
        this.extensions = extensions;
        return this;
    }

    public MimeTypeBuilder viewable(boolean viewable) {
        this.viewable = viewable;
        return this;
    }

    public MimeTypeBuilder index(boolean index) {
        this.index = index;
        return this;
    }

    public MimeTypeBuilder archive(boolean archive) {
        this.archive = archive;
        return this;
    }

    public MimeTypeBuilder syntax(String syntax) {
        this.syntax = syntax;
        return this;
    }

    public MimeTypeBuilder css(String css) {
        this.css = css;
        return this;
    }
}
