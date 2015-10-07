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

package org.artifactory.webapp.wicket.page.security.profile;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.artifactory.security.MutableUserInfo;

/**
 * Event payload for transferring between profile page components.
 *
 * @author Shay Yaakov
 */
public class ProfileEvent {
    private final AjaxRequestTarget target;
    private MutableUserInfo mutableUser;

    public ProfileEvent(AjaxRequestTarget target, MutableUserInfo mutableUser) {
        this.target = target;
        this.mutableUser = mutableUser;
    }

    public AjaxRequestTarget getTarget() {
        return target;
    }

    public MutableUserInfo getMutableUser() {
        return mutableUser;
    }
}
