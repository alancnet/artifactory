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
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.storage.FileStoreStorageSummary;
import org.artifactory.storage.StorageProperties;
import org.artifactory.util.NumberFormatter;

import java.io.File;

/**
 * A panel to display the file store summary.
 *
 * @author Yossi Shaul
 */
public class FileStoreSummaryPanel extends FieldSetPanel {
    public FileStoreSummaryPanel(String id, FileStoreStorageSummary fileStoreSummary) {
        super(id);

        add(new LabeledValue("storageType",
                fileStoreSummary.getBinariesStorageType().toString()));
        File binariesFolder = fileStoreSummary.getBinariesFolder();
        String storageDirLabel = binariesFolder != null ?
                binariesFolder.getAbsolutePath() : "Filesystem storage is not used";
        add(new LabeledValue("storageDirectory", storageDirLabel));
        if (fileStoreSummary.getCacheSize() > 0 || fileStoreSummary.getBinariesStorageType().equals(
                StorageProperties.BinaryProviderType.fullDb)) {
            add(new LabeledValue("cacheSize", StorageUnit.toReadableString(fileStoreSummary.getCacheSize())));
        } else {
            add(new WebMarkupContainer("cacheSize"));
            get("cacheSize").setEnabled(false);
            get("cacheSize").setVisible(false);
        }
        add(new LabeledValue("totalSpace",
                StorageUnit.toReadableString(fileStoreSummary.getTotalSpace())));
        add(new LabeledValue("usedSpace",
                StorageUnit.toReadableString(fileStoreSummary.getUsedSpace()) + " (" +
                        NumberFormatter.formatPercentage(fileStoreSummary.getUsedSpaceFraction()) + ")"
        ));
        add(new LabeledValue("freeSpace",
                StorageUnit.toReadableString(fileStoreSummary.getFreeSpace()) + " (" +
                        NumberFormatter.formatPercentage(fileStoreSummary.getFreeSpaceFraction()) + ")"
        ));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new HelpBubble("freeSpace.help", getString("freeSpace.help")));
        add(new HelpBubble("usedSpace.help", getString("usedSpace.help")));
        add(new HelpBubble("totalSpace.help", getString("totalSpace.help")));
        add(new HelpBubble("cacheSize.help", getString("cacheSize.help")));
        add(new HelpBubble("storageDirectory.help", getString("storageDirectory.help")));
        add(new HelpBubble("storageType.help", getString("storageType.help")));
    }

    @Override
    public String getTitle() {
        return "File Store";
    }
}
