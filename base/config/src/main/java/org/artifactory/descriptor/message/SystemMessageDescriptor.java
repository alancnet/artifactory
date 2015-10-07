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

package org.artifactory.descriptor.message;

import org.artifactory.descriptor.Descriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Descriptor for the system message
 *
 * @author Dan Feldman
 */
@XmlType(name = "SystemMessageType", propOrder = {"enabled", "title", "titleColor", "message", "showOnAllPages"},
        namespace = Descriptor.NS)
public class SystemMessageDescriptor implements Descriptor {

    @XmlElement
    private boolean enabled;

    @XmlElement
    private String title;

    @XmlElement
    private String titleColor = "#429f46";

    @XmlElement
    private String message;

    @XmlElement
    private boolean showOnAllPages;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isShowOnAllPages() {
        return showOnAllPages;
    }

    public void setShowOnAllPages(boolean showOnAllPages) {
        this.showOnAllPages = showOnAllPages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemMessageDescriptor)) {
            return false;
        }

        SystemMessageDescriptor that = (SystemMessageDescriptor) o;

        if (isEnabled() != that.isEnabled()) {
            return false;
        }
        if (isShowOnAllPages() != that.isShowOnAllPages()) {
            return false;
        }
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) {
            return false;
        }
        if (getTitleColor() != null ? !getTitleColor().equals(that.getTitleColor()) : that.getTitleColor() != null) {
            return false;
        }
        return !(getMessage() != null ? !getMessage().equals(that.getMessage()) : that.getMessage() != null);

    }

    @Override
    public int hashCode() {
        int result = (isEnabled() ? 1 : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getTitleColor() != null ? getTitleColor().hashCode() : 0);
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        result = 31 * result + (isShowOnAllPages() ? 1 : 0);
        return result;
    }
}
