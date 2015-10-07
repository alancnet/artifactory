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

package org.artifactory.api.rest;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 * A resource link
 *
 * @author yoavl
 */
@XStreamAlias("link")
public class Link implements Serializable {
    public static final String REL_SELF = "self";

    public final String href;
    public final String rel;
    public final String type;

    public Link(String href) {
        this(href, REL_SELF);
    }

    public Link(String href, String type) {
        this(href, REL_SELF, type);
    }

    public Link(String href, String rel, String type) {
        this.href = href;
        this.rel = rel;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Link link = (Link) o;
        return href.equals(link.href) && rel.equals(link.rel) &&
                !(type != null ? !type.equals(link.type) : link.type != null);

    }

    @Override
    public int hashCode() {
        int result = href.hashCode();
        result = 31 * result + rel.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}