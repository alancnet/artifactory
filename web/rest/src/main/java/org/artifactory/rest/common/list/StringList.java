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

package org.artifactory.rest.common.list;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * Special list that add split constructor string by token"," and add only not only whiteSpaces elements
 *
 * @author Eli Givoni
 */
public class StringList extends ArrayList<String> {

    private static final Logger log = LoggerFactory.getLogger(StringList.class);

    public StringList(String s) {
        super();

        for (String v : s.split(",")) {
            try {
                if (!StringUtils.isWhitespace(v)) {
                    add(v.trim());
                }
            } catch (Exception ex) {
                log.error("Error while parsing list parameter '{}': {}.", s, ex.getMessage());
                throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
            }
        }
    }
}
