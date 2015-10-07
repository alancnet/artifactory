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

package org.artifactory.config;

import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.repo.interceptor.Interceptors;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.springframework.stereotype.Service;

/**
 * Central config interceptors chain manager.
 *
 * @author Yossi Shaul
 */
@Service
@Reloadable(beanClass = ConfigurationChangesInterceptors.class, initAfter = DbService.class)
public class ConfigurationChangesInterceptorsImpl extends Interceptors<ConfigurationChangesInterceptor>
        implements ConfigurationChangesInterceptors {

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        for (ConfigurationChangesInterceptor interceptor : this) {
            interceptor.onBeforeSave(newDescriptor);
        }
    }
}