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

package org.artifactory.descriptor.delegation;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
* Delegation definitions container
*
* Created by Michael Pasternak on 8/12/15.
*/
@XmlType(name = "ContentSynchronisation", propOrder = {"enabled", "statistics", "properties"},
        namespace = Descriptor.NS)
public class ContentSynchronisation implements Descriptor {

    @XmlElement(name = "enabled", required = true, namespace = Descriptor.NS)
    private boolean enabled = false;
    @XmlElement(name = "statistics", required = true, namespace = Descriptor.NS)
    private StatisticsContent statistics;
    @XmlElement(name = "properties", required = true, namespace = Descriptor.NS)
    private PropertiesContent properties;

    public ContentSynchronisation() {
        this.statistics = new StatisticsContent();
        this.properties = new PropertiesContent();
    }

    /**
     * Enables delegation when SmartRepo discovered
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Disables delegation
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Checks whether delegation is enabled on container level
     *
     * @return boolean
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns delegate representing statistics
     *
     * @return {@link StatisticsContent}
     */
    public StatisticsContent getStatistics() {
        return statistics;
    }

    /**
     * Returns delegate representing properties
     *
     * @return {@link PropertiesContent}
     */
    public PropertiesContent getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContentSynchronisation that = (ContentSynchronisation) o;

        if (enabled != that.enabled) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        if (statistics != null ? !statistics.equals(that.statistics) : that.statistics != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (statistics != null ? statistics.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}
