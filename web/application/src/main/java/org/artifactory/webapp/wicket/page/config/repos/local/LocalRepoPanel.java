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

package org.artifactory.webapp.wicket.page.config.repos.local;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.addon.replication.ReplicationAddon;
import org.artifactory.addon.wicket.PropertiesWebAddon;
import org.artifactory.addon.wicket.ReplicationWebAddon;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.webapp.wicket.page.config.repos.CachingDescriptorHelper;
import org.artifactory.webapp.wicket.page.config.repos.RepoConfigCreateUpdatePanel;
import org.artifactory.webapp.wicket.util.CronUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Local repository configuration panel.
 *
 * @author Yossi Shaul
 */
public class LocalRepoPanel extends RepoConfigCreateUpdatePanel<LocalRepoDescriptor> {

    private LocalReplicationDescriptor replicationDescriptor;

    public LocalRepoPanel(CreateUpdateAction action, LocalRepoDescriptor repoDescriptor,
            CachingDescriptorHelper cachingDescriptorHelper) {
        super(action, repoDescriptor, cachingDescriptorHelper);
        add(new CssClass("local-repo-config"));
    }

    @Override
    protected List<ITab> getConfigurationTabs() {
        List<ITab> tabList = Lists.newArrayList();

        tabList.add(new AbstractTab(Model.of("Basic Settings")) {

            @Override
            public Panel getPanel(String panelId) {
                return new LocalRepoBasicPanel(panelId, getRepoDescriptor(), isCreate());
            }
        });

        List<PropertySet> propertySets = getCachingDescriptorHelper().getModelMutableDescriptor().getPropertySets();
        PropertiesWebAddon propertiesWebAddon = addons.addonByType(PropertiesWebAddon.class);
        tabList.add(propertiesWebAddon.getRepoConfigPropertySetsTab("Property Sets", entity, propertySets));

        MutableCentralConfigDescriptor mutableDescriptor = cachingDescriptorHelper.getModelMutableDescriptor();
        replicationDescriptor = mutableDescriptor.getEnabledLocalReplication(entity.getKey());
        if (replicationDescriptor == null) {
            replicationDescriptor = new LocalReplicationDescriptor();
            replicationDescriptor.setRepoKey(entity.getKey());
        }
        ReplicationWebAddon replicationWebAddon = addons.addonByType(ReplicationWebAddon.class);
        tabList.add(replicationWebAddon.getLocalRepoReplicationPanel("Replication", entity, replicationDescriptor,
                mutableDescriptor, action));

        tabList.add(new AbstractTab(Model.of("Packages")) {
            @Override
            public Panel getPanel(String panelId) {
                return new LocalRepoPackagesPanel(panelId, entity, isCreate());
            }
        });

        return tabList;
    }

    @Override
    public void addAndSaveDescriptor(LocalRepoDescriptor repoDescriptor) {
        CachingDescriptorHelper helper = getCachingDescriptorHelper();
        MutableCentralConfigDescriptor mccd = helper.getModelMutableDescriptor();
        mccd.addLocalRepository(repoDescriptor);
        if (replicationDescriptor.isEnabled()) {
            if (StringUtils.isBlank(replicationDescriptor.getRepoKey())) {
                replicationDescriptor.setRepoKey(repoDescriptor.getKey());
            }
            mccd.addLocalReplication(replicationDescriptor);
        }
        helper.syncAndSaveLocalRepositories();
    }

    /**
     * update repo replication related property following to repo data update or deletion
     */
    @Override
    public void updateRepositoryReplicationProperties() {
        Set<String> propsKeys = getRepoReplicationPropsKeysFromDB();
        List<String> localRepoList = getLocalReplicationPropertiesKeysList();
        List<String> propsToBeRemoved = getUnusedPropsToDeleteList(localRepoList, propsKeys);
        deleteUnusedPropertiesFromDB(propsToBeRemoved);
    }

    /**
     * check if licensed for HA then return appropriate key properties
     *
     * @return if HA License return unique key with url , else none multi push basic key
     */
    private List<String> getLocalReplicationPropertiesKeysList() {
        List<String> localRepoList;
        if (addons.isHaLicensed()) {
            localRepoList = getUniqueMultiPushKeyProperties();
        } else {
            localRepoList = getNoneMultiPushUniqueList();
        }
        return localRepoList;
    }

    /**
     * return the basic non Multi Push Unique Key properties name For Replication
     *
     * @return list of non Multi Push Key properties name
     */
    private List<String> getNoneMultiPushUniqueList() {
        List<String> noneMultiPushUniqueKeyList = new ArrayList<>();
        noneMultiPushUniqueKeyList.add("artifactory.replication." + replicationDescriptor.getRepoKey()
                + ReplicationAddon.PROP_REPLICATION_STARTED_SUFFIX);
        noneMultiPushUniqueKeyList.add("artifactory.replication." + replicationDescriptor.getRepoKey()
                + ReplicationAddon.PROP_REPLICATION_FINISHED_SUFFIX);
        noneMultiPushUniqueKeyList.add("artifactory.replication." + replicationDescriptor.getRepoKey()
                + ReplicationAddon.PROP_REPLICATION_RESULT_SUFFIX);
        return noneMultiPushUniqueKeyList;
    }

    /**
     * @return Unique Multi Push Replication Keys (with url)
     */
    private List<String> getUniqueMultiPushKeyProperties() {
        return cachingDescriptorHelper.getModelMutableDescriptor().
                getLocalReplicationsUniqueKeyForProperty(replicationDescriptor.getRepoKey());
    }

    /**
     * @return props key of specific repo from DB
     */
    private Set<String> getRepoReplicationPropsKeysFromDB() {
        PropertiesWebAddon propertiesWebAddon = addons.addonByType(PropertiesWebAddon.class);
        Properties propsServiceProperties = propertiesWebAddon.getProperties(replicationDescriptor.getRepoPath());
        return propsServiceProperties.keySet();
    }

    /**
     * remove unused replication properties due to repo replication updated (delete / url change)
     *
     * @param propsToBeRemoved
     */
    private void deleteUnusedPropertiesFromDB(List<String> propsToBeRemoved) {
        PropertiesWebAddon propertiesWebAddon = addons.addonByType(PropertiesWebAddon.class);
        for (String propToDelete : propsToBeRemoved) {
            propertiesWebAddon.removeProperties(replicationDescriptor.getRepoPath(), propToDelete);
        }
    }

    /**
     * compare replication property based updated descriptor replication list
     * and return list if property to be removed f not needed
     *
     * @param localRepoList - update descriptor replication list after update
     * @param propsKeys     property Set from DB
     * @return list of unused property to be removed
     */
    private List<String> getUnusedPropsToDeleteList(List<String> localRepoList, Set<String> propsKeys) {
        List<String> propsToBeRemoved = new ArrayList<>();
        for (String propKey : propsKeys) {
            String propsPartialKey = propKey.replace("artifactory.replication." + replicationDescriptor.getRepoKey(),
                    "")
                    .replace(ReplicationAddon.PROP_REPLICATION_STARTED_SUFFIX, "")
                    .replace(ReplicationAddon.PROP_REPLICATION_FINISHED_SUFFIX, "")
                    .replace(ReplicationAddon.PROP_REPLICATION_RESULT_SUFFIX, "");

            if (!localRepoList.contains(propsPartialKey) && !propsToBeRemoved.contains(propKey)) {
                propsToBeRemoved.add("artifactory.replication." + replicationDescriptor.getRepoKey()
                        + propsPartialKey + ReplicationAddon.PROP_REPLICATION_STARTED_SUFFIX);
                propsToBeRemoved.add("artifactory.replication." + replicationDescriptor.getRepoKey()
                        + propsPartialKey + ReplicationAddon.PROP_REPLICATION_FINISHED_SUFFIX);
                propsToBeRemoved.add("artifactory.replication." + replicationDescriptor.getRepoKey()
                        + propsPartialKey + ReplicationAddon.PROP_REPLICATION_RESULT_SUFFIX);
            }
        }
        return propsToBeRemoved;
    }

    @Override
    public void saveEditDescriptor(LocalRepoDescriptor repoDescriptor) {
        CachingDescriptorHelper helper = getCachingDescriptorHelper();
        MutableCentralConfigDescriptor mccd = helper.getModelMutableDescriptor();
        //update the model being saved
        Map<String, LocalRepoDescriptor> localRepositoriesMap = mccd.getLocalRepositoriesMap();
        if (localRepositoriesMap.containsKey(repoDescriptor.getKey())) {
            localRepositoriesMap.put(repoDescriptor.getKey(), repoDescriptor);
        }
        if (replicationDescriptor.isEnabled() && !mccd.isLocalReplicationExists(replicationDescriptor)) {
            if (StringUtils.isBlank(replicationDescriptor.getRepoKey())) {
                replicationDescriptor.setRepoKey(repoDescriptor.getKey());
            }
        }
        helper.syncAndSaveLocalRepositories();
    }

    @Override
    protected boolean validate(LocalRepoDescriptor repoDescriptor) {
        if (!isCronExpValid(cachingDescriptorHelper)) {
            error("Invalid cron expression");
            return false;
        }
        return true;
    }

    /**
     * validate cron exp for replication validation
     *
     * @return
     */
    private boolean isCronExpValid(CachingDescriptorHelper cachingDescriptorHelper) {
        List<LocalReplicationDescriptor> localReplicationDescriptors = cachingDescriptorHelper.getModelMutableDescriptor().getLocalReplications();
        if (localReplicationDescriptors != null && localReplicationDescriptors.size() != 0 && replicationDescriptor.getRepoKey() != null) {
            for (LocalReplicationDescriptor localReplicationDescriptor : localReplicationDescriptors) {
                if ((replicationDescriptor.getRepoKey()).equals(localReplicationDescriptor.getRepoKey())) {
                    String cronExp = localReplicationDescriptor.getCronExp();
                    if (StringUtils.isBlank(cronExp) || !CronUtils.isValid(cronExp)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
