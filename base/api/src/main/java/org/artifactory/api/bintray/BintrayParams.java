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

package org.artifactory.api.bintray;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;

/**
 * Bintray parameters model
 *
 * @author Shay Yaakov
 */
public class BintrayParams implements Serializable {

    private String repo;
    private String packageId;
    private String version;
    private String path;
    private boolean useExistingProps;
    private boolean notify;

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        if (StringUtils.isNotBlank(repo)) {
            this.repo = repo;
        }
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        if (StringUtils.isNotBlank(packageId)) {
            this.packageId = packageId;
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        if (StringUtils.isNotBlank(version)) {
            this.version = version;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            this.path = path;
        }
    }

    public boolean isUseExistingProps() {
        return useExistingProps;
    }

    public void setUseExistingProps(boolean useExistingProps) {
        this.useExistingProps = useExistingProps;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(repo) && StringUtils.isNotBlank(packageId) &&
                StringUtils.isNotBlank(version) && StringUtils.isNotBlank(path);
    }
}