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

package org.artifactory.addon.wicket;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.artifactory.addon.Addon;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author Noam Y. Tenne
 */
public interface ReplicationWebAddon extends Addon {

    ITab getHttpRepoReplicationPanel(String tabTitle, HttpRepoDescriptor repoDescriptor,
            RemoteReplicationDescriptor replicationDescriptor, CreateUpdateAction action);

    ITab getLocalRepoReplicationPanel(String tabTitle, LocalRepoDescriptor entity,
            LocalReplicationDescriptor replicationDescriptor,
            MutableCentralConfigDescriptor mutableDescriptor, CreateUpdateAction action);

    MarkupContainer getLastReplicationStatusLabel(String id, RepoPath repoPath, boolean isCache);
}
