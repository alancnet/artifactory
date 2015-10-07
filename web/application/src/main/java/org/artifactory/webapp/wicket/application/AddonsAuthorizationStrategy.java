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

package org.artifactory.webapp.wicket.application;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.WebApplicationAddon;

/**
 * This authorization strategy delegate calls to the addons service.
 *
 * @author Yossi Shaul
 */
public class AddonsAuthorizationStrategy implements IAuthorizationStrategy {

    @SpringBean
    private AddonsManager addonsManager;

    private WebApplicationAddon applicationAddon;

    @Override
    public boolean isInstantiationAuthorized(Class componentClass) {
        if (applicationAddon == null) {
            applicationAddon = addonsManager.addonByType(WebApplicationAddon.class);
        }
        return addonsManager.isInstantiationAuthorized(componentClass) &&
                applicationAddon.isInstantiationAuthorized(componentClass);
    }

    @Override
    public boolean isActionAuthorized(Component component, Action action) {
        return true;
    }
}
