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

package org.artifactory.repo.interceptor;

import com.google.common.collect.Multiset;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.PropertiesAddon;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.property.Property;
import org.artifactory.md.Properties;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.sapi.fs.VfsItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Interceptor for adding properties as a post copy or post move operations.
 *
 * @author Tomer Cohen
 */
public class AddPropertyAfterMoveOrCopyInterceptor extends StorageInterceptorAdapter {
    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void afterCopy(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        super.afterCopy(sourceItem, targetItem, statusHolder, properties);
        PropertiesAddon propertiesAddon = addonsManager.addonByType(PropertiesAddon.class);
        Multiset<String> keys = properties.keys();
        for (String key : keys) {
            Set<String> valuesForKey = properties.get(key);
            Property property = new Property();
            property.setName(key);
            String[] values = new String[valuesForKey.size()];
            valuesForKey.toArray(values);
            propertiesAddon.addProperty(targetItem.getRepoPath(), null, property, values);
        }
    }

    @Override
    public void afterMove(VfsItem sourceItem, VfsItem targetItem, MutableStatusHolder statusHolder,
            Properties properties) {
        super.afterMove(sourceItem, targetItem, statusHolder, properties);
        PropertiesAddon propertiesAddon = addonsManager.addonByType(PropertiesAddon.class);
        Multiset<String> keys = properties.keys();
        for (String key : keys) {
            Set<String> valuesForKey = properties.get(key);
            Property property = new Property();
            property.setName(key);
            String[] values = new String[valuesForKey.size()];
            valuesForKey.toArray(values);
            propertiesAddon.addProperty(targetItem.getRepoPath(), null, property, values);
        }
    }
}
