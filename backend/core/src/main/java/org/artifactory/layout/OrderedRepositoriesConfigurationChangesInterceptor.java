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

package org.artifactory.layout;

import org.artifactory.common.ConstantValues;
import org.artifactory.config.ConfigurationChangesInterceptor;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Fred Simon
 */
@Component
public class OrderedRepositoriesConfigurationChangesInterceptor implements ConfigurationChangesInterceptor {

    @Override
    public void onBeforeSave(CentralConfigDescriptor newDescriptor) {
        if (newDescriptor instanceof MutableCentralConfigDescriptor && ConstantValues.disableGlobalRepoAccess.getBoolean()) {
            // If global repo is disabled, all repository key names are ordered
            MutableCentralConfigDescriptor mutableCentralConfigDescriptor = (MutableCentralConfigDescriptor) newDescriptor;
            mutableCentralConfigDescriptor.setRemoteRepositoriesMap(sortMap(newDescriptor.getRemoteRepositoriesMap()));
            mutableCentralConfigDescriptor.setLocalRepositoriesMap(sortMap(newDescriptor.getLocalRepositoriesMap()));
            mutableCentralConfigDescriptor.setVirtualRepositoriesMap(
                    sortMap(newDescriptor.getVirtualRepositoriesMap()));
        }
    }

    private <T extends RepoDescriptor> Map<String, T> sortMap(Map<String, T> map) {
        String[] origKeys = map.keySet().toArray(new String[map.size()]);
        String[] orderedKeys = map.keySet().toArray(new String[map.size()]);
        Arrays.sort(orderedKeys);
        if (Arrays.equals(origKeys, orderedKeys)) {
            return map;
        } else {
            Map<String, T> result = new LinkedHashMap<>(map.size());
            for (String orderedKey : orderedKeys) {
                result.put(orderedKey, map.get(orderedKey));
            }
            return result;
        }
    }
}
