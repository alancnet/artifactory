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

package org.artifactory.common.wicket.application;

import org.apache.wicket.util.resource.UrlResourceStream;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Yoav Aharoni
 */
public class ExternalResourceStream extends UrlResourceStream {
    public ExternalResourceStream(URL url) {
        super(url);
    }

    public ExternalResourceStream(String url) {
        super(createURL(url));
    }

    private static URL createURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad Resource URL: " + url, e);
        }
    }
}
