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
import org.artifactory.descriptor.repo.HttpRepoDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Statistics delegation context
 *
* Created by Michael Pasternak on 8/12/15.
*/
@XmlType(name = "StatisticsContent", propOrder = {"enabled"},
        namespace = Descriptor.NS)
public class StatisticsContent implements Descriptor {

    public StatisticsContent() {
        super();
    }

    @XmlElement(name = "enabled", required = true, namespace = Descriptor.NS)
    private boolean enabled = false;

    /**
     * Checks whether delegation is enabled
     *
     * @return boolean
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Disables delegation
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Enables delegation
     */
    public void enable() {
        this.enabled = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatisticsContent that = (StatisticsContent) o;

        if (enabled != that.enabled) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (enabled ? 1 : 0);
    }
}
