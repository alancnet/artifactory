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

package org.artifactory.common.wicket.behavior.border;

import org.artifactory.common.wicket.behavior.template.TemplateBehavior;

/**
 * @author Yoav Aharoni
 */
public class TitledBorderBehavior extends TemplateBehavior {
    private String cssPrefix;
    private String title;
    private String cssClass;

    public TitledBorderBehavior() {
        this("");
    }

    public TitledBorderBehavior(String title) {
        this("border", title);
    }

    public TitledBorderBehavior(String cssPrefix, String title) {
        super(TitledBorderBehavior.class);
        this.cssPrefix = cssPrefix;
        this.title = title;
    }

    public String getCssPrefix() {
        return cssPrefix;
    }

    public void setCssPrefix(String cssPrefix) {
        this.cssPrefix = cssPrefix;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCssClass() {
        return cssClass;
    }

    /**
     * Additional CSS class for the border top level DIV element
     *
     * @param cssClass
     */
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
