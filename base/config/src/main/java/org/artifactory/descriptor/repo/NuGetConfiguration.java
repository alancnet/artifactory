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

package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Noam Y. Tenne
 */
@XmlType(name = "NuGetConfigurationType", propOrder = {"feedContextPath", "downloadContextPath"},
        namespace = Descriptor.NS)
public class NuGetConfiguration implements Descriptor {

    @XmlElement(defaultValue = "", required = false)
    private String feedContextPath = "";

    @XmlElement(defaultValue = "", required = false)
    private String downloadContextPath = "";

    public String getFeedContextPath() {
        return feedContextPath;
    }

    public void setFeedContextPath(String feedContextPath) {
        if (feedContextPath == null) {
            feedContextPath = "";
        }
        this.feedContextPath = feedContextPath;
    }

    public String getDownloadContextPath() {
        return downloadContextPath;
    }

    public void setDownloadContextPath(String downloadContextPath) {
        this.downloadContextPath = downloadContextPath;
    }
}
