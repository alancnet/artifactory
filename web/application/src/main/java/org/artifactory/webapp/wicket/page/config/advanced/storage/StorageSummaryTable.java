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

import com.google.common.collect.Lists;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.artifactory.api.repo.storage.RepoStorageSummaryInfo;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.label.tooltip.TooltipLabel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.FormattedLongPropertyColumn;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.artifactory.storage.StorageSummaryInfo;
import org.artifactory.util.NumberFormatter;

import java.util.Iterator;
import java.util.List;

import static org.artifactory.api.repo.storage.RepoStorageSummaryInfo.RepositoryType;

/**
 * A panel holding the table with storage info per repository.
 *
 * @author Yossi Shaul
 */
public class StorageSummaryTable extends TitledPanel {
    private final StorageSummaryInfo storageSummary;
    private final int numberOfRowsInTable = 200;

    public StorageSummaryTable(String id, StorageSummaryInfo storageSummary) {
        super(id);
        this.storageSummary = storageSummary;
        setOutputMarkupId(true);
        List<IColumn<RepoStorageSummaryInfo>> columns = getColumns();
        SortableTable<RepoStorageSummaryInfo> table = new SortableTable<>(
                "storageSummaryTable", columns, new StorageSummarySortableDataProvider(), numberOfRowsInTable);
        add(table);
        add(new CssClass("storage-table"));
    }

    @Override
    public String getTitle() {
        return "Repositories";
    }

    public List<IColumn<RepoStorageSummaryInfo>> getColumns() {
        List<IColumn<RepoStorageSummaryInfo>> columns = Lists.newArrayList();
        columns.add(createRepoTypeColumn());
        columns.add(new PropertyColumn<RepoStorageSummaryInfo>(Model.of("Repository Key"), "repoKey", "repoKey"));
        columns.add(new AbstractColumn<RepoStorageSummaryInfo>(Model.of("Percentage"), "usedSpace") {
            @Override
            public void populateItem(Item<ICellPopulator<RepoStorageSummaryInfo>> item, String componentId,
                    IModel<RepoStorageSummaryInfo> rowModel) {
                item.add(new CssClass("right"));
                double fraction = (double) rowModel.getObject().getUsedSpace() / storageSummary.getTotalSize();
                String percentage = NumberFormatter.formatPercentage(fraction);
                item.add(new Label(componentId, Model.of(percentage)));
            }
        });
        columns.add(new PropertyColumn<RepoStorageSummaryInfo>(Model.of("Used Space"), "usedSpace", "usedSpace") {
            @Override
            protected IModel<?> createLabelModel(IModel<RepoStorageSummaryInfo> rowModel) {
                return Model.of(StorageUnit.toReadableString(rowModel.getObject().getUsedSpace()));
            }

            @Override
            public void populateItem(Item<ICellPopulator<RepoStorageSummaryInfo>> item, String componentId,
                    IModel<RepoStorageSummaryInfo> rowModel) {
                item.add(new CssClass("right"));
                super.populateItem(item, componentId, rowModel);
            }
        });
        columns.add(new FormattedLongPropertyColumn<RepoStorageSummaryInfo>(
                Model.of("Items"), "itemsCount", "itemsCount"));
        columns.add(new FormattedLongPropertyColumn<RepoStorageSummaryInfo>(
                Model.of("Folders"), "foldersCount", "foldersCount"));
        columns.add(new FormattedLongPropertyColumn<RepoStorageSummaryInfo>(
                Model.of("Files"), "filesCount", "filesCount"));
        return columns;
    }

    private PropertyColumn<RepoStorageSummaryInfo> createRepoTypeColumn() {
        return new PropertyColumn<RepoStorageSummaryInfo>(Model.of("Type"), "repoType", "repoType") {
            @Override
            public void populateItem(Item<ICellPopulator<RepoStorageSummaryInfo>> item, String componentId,
                    IModel<RepoStorageSummaryInfo> model) {
                final RepoStorageSummaryInfo info = model.getObject();
                switch (info.getRepoType()) {
                    case LOCAL:
                        item.add(new TooltipLabel(componentId, "", 30) {
                            @Override
                            protected void onBeforeRender() {
                                super.onBeforeRender();
                                setTooltip(Strings.capitalize(info.getRepoType().toString().toLowerCase()));
                                add(new CssClass("icon"));
                                add(new CssClass("repository"));
                            }
                        });
                        break;
                    case CACHE:
                        item.add(new TooltipLabel(componentId, "", 30) {
                            @Override
                            protected void onBeforeRender() {
                                super.onBeforeRender();
                                setTooltip(Strings.capitalize(info.getRepoType().toString().toLowerCase()));
                                add(new CssClass("icon"));
                                add(new CssClass("repository-cache"));
                            }
                        });
                        break;
                    case REMOTE:
                        item.add(new TooltipLabel(componentId, "", 30) {
                            @Override
                            protected void onBeforeRender() {
                                super.onBeforeRender();
                                setTooltip(Strings.capitalize(info.getRepoType().toString().toLowerCase()));
                                add(new CssClass("icon"));
                                add(new CssClass("repository-cache"));
                            }
                        });
                        break;
                    case VIRTUAL:
                        item.add(new TooltipLabel(componentId, "", 30) {
                            @Override
                            protected void onBeforeRender() {
                                super.onBeforeRender();
                                setTooltip(Strings.capitalize(info.getRepoType().toString().toLowerCase()));
                                add(new CssClass("icon"));
                                add(new CssClass("repository-virtual"));
                            }
                        });
                        break;
                    case BROKEN:
                        item.add(new TooltipLabel(componentId, "", 30) {
                            @Override
                            protected void onBeforeRender() {
                                super.onBeforeRender();
                                setTooltip("The repository was deleted but its storage was not cleaned.");
                                add(new CssClass("icon"));
                                add(new CssClass("WarnColumn"));
                                add(new CssClass("warn"));
                            }
                        });
                        break;
                    case NA:
                        // Empty cell
                        item.add(new Label(componentId));
                        break;
                }
            }
        };
    }

    private class StorageSummarySortableDataProvider extends SortableDataProvider<RepoStorageSummaryInfo> {
        private final List<RepoStorageSummaryInfo> repoSummaries;
        private final RepoStorageSummaryInfo totalStorageSummary;

        private StorageSummarySortableDataProvider() {
            repoSummaries = Lists.newArrayList((storageSummary.getRepoStorageSummaries()));

            totalStorageSummary = new RepoStorageSummaryInfo("TOTAL",
                    RepositoryType.NA, storageSummary.getTotalFolders(), storageSummary.getTotalFiles(),
                    storageSummary.getTotalSize(), "n/a");

            setSort("usedSpace", SortOrder.DESCENDING);
        }

        @Override
        public Iterator<RepoStorageSummaryInfo> iterator(int first, int count) {
            ListPropertySorter.sort(repoSummaries, getSort());
            List<RepoStorageSummaryInfo> sorted;
            if (lastPage(first, count)) {
                // subtract the total line
                sorted = Lists.newArrayList(repoSummaries.subList(first, first + count - 1));
                // now add the total to the end
                sorted.add(totalStorageSummary);
            } else {
                sorted = Lists.newArrayList(repoSummaries.subList(first, first + count));
            }
            return sorted.iterator();
        }

        private boolean lastPage(int first, int count) {
            return repoSummaries.size() - first < numberOfRowsInTable;
        }

        @Override
        public int size() {
            return repoSummaries.size() + 1;    // for the total line
        }

        @Override
        public IModel<RepoStorageSummaryInfo> model(RepoStorageSummaryInfo rsi) {
            return new Model<>(rsi);
        }
    }
}
