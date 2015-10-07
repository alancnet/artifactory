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

import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.panel.fieldset.FieldSetPanel;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.util.NumberFormatter;

/**
 * A panel to display the file store summary.
 *
 * @author Yossi Shaul
 */
public class BinariesSummaryPanel extends FieldSetPanel {

    public BinariesSummaryPanel(String id, StorageSummaryInfo fileStoreSummary) {
        super(id);

        add(new LabeledValue("binariesCount", NumberFormatter.formatLong(
                fileStoreSummary.getBinariesInfo().getBinariesCount())));
        add(new LabeledValue("binariesSize", StorageUnit.toReadableString(
                fileStoreSummary.getBinariesInfo().getBinariesSize())));
        add(new LabeledValue("optimization", NumberFormatter.formatPercentage(fileStoreSummary.getOptimization())));
        add(new LabeledValue("artifactsSize", StorageUnit.toReadableString(fileStoreSummary.getTotalSize())));
        add(new LabeledValue("itemsCount", NumberFormatter.formatLong((fileStoreSummary.getTotalItems()))));
        add(new LabeledValue("filesCount", NumberFormatter.formatLong((fileStoreSummary.getTotalFiles()))));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new HelpBubble("binariesCount.help", getString("binariesCount.help")));
        add(new HelpBubble("binariesSize.help", getString("binariesSize.help")));
        add(new HelpBubble("optimization.help", getString("optimization.help")));
        add(new HelpBubble("artifactsSize.help", getString("artifactsSize.help")));
        add(new HelpBubble("itemsCount.help", getString("itemsCount.help")));
        add(new HelpBubble("filesCount.help", getString("filesCount.help")));
    }

    @Override
    public String getTitle() {
        return "Binaries";
    }
}
