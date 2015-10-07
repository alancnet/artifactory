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

package org.artifactory.layout;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.LayoutsCoreAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.springframework.stereotype.Component;

/**
 * @author Noam Y. Tenne
 */
@Component
public class LayoutConfigurationChangesInterceptor implements ConfigurationChangesInterceptor {

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LayoutsCoreAddon layoutsCoreAddon = addonsManager.addonByType(LayoutsCoreAddon.class);
        layoutsCoreAddon.assertLayoutConfigurationsBeforeSave(newDescriptor);
    }
}
