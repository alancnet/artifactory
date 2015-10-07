/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.webapp;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.artifactory.api.web.WebappService;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;
import org.springframework.stereotype.Service;

/**
 * Implementation of services given by the webapp to lower level components.
 *
 * @author Yossi Shaul
 */
@Service
public class WebappServiceImpl implements WebappService {
    @Override
    public void rebuildSiteMap() {
        // get the first application key (in aol it will not work!)
        ArtifactoryApplication app = (ArtifactoryApplication) Application.get(
                Application.getApplicationKeys().iterator().next());
        // we must attach the application to the current thread in order to perform wicket operation
        ThreadContext.setApplication(app);
        try {
            app.rebuildSiteMap();
        } finally {
            ThreadContext.detach();
        }
    }
}
