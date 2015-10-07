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

package org.artifactory.api.build.diff;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Builds diff model object which wraps properties diff
 *
 * @author Shay Yaakov
 */
public class BuildsDiffPropertyModel implements Serializable {

    private String key;
    private String value;
    private String diffValue;

    @JsonIgnore
    private BuildsDiffStatus status;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDiffValue() {
        return diffValue;
    }

    public void setDiffValue(String diffValue) {
        this.diffValue = diffValue;
    }

    public BuildsDiffStatus getStatus() {
        return status;
    }

    public void setStatus(BuildsDiffStatus status) {
        this.status = status;
    }

    public String getCompoundKeyValue() {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return key + ":" + value;
    }

    public String getCompoundDiffKeyValue() {
        if (StringUtils.isBlank(diffValue)) {
            return null;
        }

        return key + ":" + diffValue;
    }
}
