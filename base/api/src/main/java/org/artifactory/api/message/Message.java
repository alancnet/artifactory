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

package org.artifactory.api.message;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public class Message implements Serializable {
    private final String id;
    private final String body;

    public Message(String id, String body) {
        this.id = id;
        this.body = body;
    }

    @Override
    public String toString() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;
        return body.equals(message.body) && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }

    public String getBody() {
        return body;
    }

    public String getId() {
        return id;
    }
}
