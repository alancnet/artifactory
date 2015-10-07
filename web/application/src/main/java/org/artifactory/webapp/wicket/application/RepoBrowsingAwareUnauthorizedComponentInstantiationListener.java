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
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.request.http.WebRequest;
import org.artifactory.webapp.servlet.RequestUtils;

/**
 * @author Yoav Aharoni
 */
public class RepoBrowsingAwareUnauthorizedComponentInstantiationListener
        implements IUnauthorizedComponentInstantiationListener {
    private IUnauthorizedComponentInstantiationListener delegate;

    public RepoBrowsingAwareUnauthorizedComponentInstantiationListener(
            IUnauthorizedComponentInstantiationListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onUnauthorizedInstantiation(Component component) {
        WebRequest webRequest = (WebRequest) component.getRequest();
        RequestUtils.removeRepoPath(webRequest, true);
        delegate.onUnauthorizedInstantiation(component);
    }

}
