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

package org.artifactory.webapp.wicket.page.config.advanced.storage;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.storage.FileStoreStorageSummary;
import org.artifactory.storage.StorageService;
import org.artifactory.storage.StorageSummaryInfo;

/**
 * A panel displaying a summary of the storage information.
 *
 * @author Yossi Shaul
 */
public class StorageSummaryPanel extends TitledPanel {

    @SpringBean
    private StorageService storageService;

    public StorageSummaryPanel(String id) {
        super(id);

        StorageSummaryInfo storageSummary = storageService.getStorageSummaryInfo();
        add(new BinariesSummaryPanel("binariesSummary", storageSummary));

        CoreAddons coreAddons = ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class);
        if (!coreAddons.isAol()) {
            FileStoreStorageSummary fileStoreSummary = storageService.getFileStoreStorageSummary();
            add(new FileStoreSummaryPanel("fileStoreSummary", fileStoreSummary));
        } else {
            add(new WebMarkupContainer("fileStoreSummary"));
        }

        add(new StorageSummaryTable("storageSummaryPanel", storageSummary));
    }
}
