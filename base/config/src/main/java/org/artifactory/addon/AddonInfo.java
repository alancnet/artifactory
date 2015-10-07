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

package org.artifactory.addon;

import java.io.Serializable;
import java.util.Properties;

/**
 * Contains the information of an installed addon
 *
 * @author Noam Y. Tenne
 */
public class AddonInfo implements Comparable<AddonInfo>, Serializable {

    private String addonName;
    private String addonDisplayName;
    private String addonPath;
    private AddonState addonState = AddonState.INACTIVATED;
    private Properties addonProperties;
    private long displayOrdinal;

    /**
     * Main constructor
     *
     * @param addonName        Name of addon
     * @param addonDisplayName Display ame of addon
     * @param addonPath        Path of addon xml file
     * @param addonState       State of addon
     * @param addonProperties  Addon properties
     * @param displayOrdinal   The display-order weight of the addon
     */
    public AddonInfo(String addonName, String addonDisplayName, String addonPath, AddonState addonState,
            Properties addonProperties, long displayOrdinal) {
        this.addonName = addonName;
        this.addonDisplayName = addonDisplayName;
        this.addonPath = addonPath;
        this.addonState = addonState;
        this.displayOrdinal = displayOrdinal;
        this.addonProperties = addonProperties != null ? addonProperties : new Properties();
    }

    /**
     * Returns the name of the addon
     *
     * @return Addon name
     */
    public String getAddonName() {
        return addonName;
    }

    /**
     * Returns the displayable name of the addon
     *
     * @return Addon display name
     */
    public String getAddonDisplayName() {
        return addonDisplayName;
    }

    /**
     * Returns the path of the addon
     *
     * @return Addon path
     */
    public String getAddonPath() {
        return addonPath;
    }

    /**
     * Returns the state of the addon
     *
     * @return Addon state
     */
    public AddonState getAddonState() {
        if (addonState == null) {   // sanity
            addonState = AddonState.INACTIVATED;
        }
        return addonState;
    }

    /**
     * Sets the state of the addon
     *
     * @param addonState State to assign to addon
     */
    public void setAddonState(AddonState addonState) {
        this.addonState = addonState;
    }

    /**
     * Returns the addon's properties object
     *
     * @return Addon properties
     */
    public Properties getAddonProperties() {
        return addonProperties;
    }

    /**
     * Returns the addon property that corresponds to the given key
     *
     * @param propertyKey Key of property to retrieve
     * @return Property value if key was found. Null if not
     */
    public String getAddonProperty(String propertyKey) {
        return addonProperties.getProperty(propertyKey);
    }

    /**
     * Returns the addon's display-order weight
     *
     * @return Weight
     */
    public long getDisplayOrdinal() {
        return displayOrdinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddonInfo)) {
            return false;
        }

        AddonInfo addonInfo = (AddonInfo) o;

        if (displayOrdinal != addonInfo.displayOrdinal) {
            return false;
        }
        if (addonName != null ? !addonName.equals(addonInfo.addonName) : addonInfo.addonName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = addonName != null ? addonName.hashCode() : 0;
        result = 31 * result + (int) (displayOrdinal ^ (displayOrdinal >>> 32));
        return result;
    }

    @Override
    public int compareTo(AddonInfo o) {
        if (getDisplayOrdinal() < o.getDisplayOrdinal()) {
            return -1;
        } else if (getDisplayOrdinal() > o.getDisplayOrdinal()) {
            return 1;
        }
        return 0;
    }
}