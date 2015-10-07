package org.artifactory.common.wicket.component.table.masterdetail;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.toolbar.emptyrow.EmptyRowToolbar;
import org.artifactory.common.wicket.util.ListPropertySorter;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.beans.support.SortDefinition;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Yoav Aharoni
 */
public abstract class MasterDetailTable<M extends Serializable, D extends Serializable> extends SortableTable {
    private Set<M> openedItems = new HashSet<>();

    @SuppressWarnings({"unchecked"})
    public MasterDetailTable(String id, List<IColumn> columns, List<M> masterList, String defaultSortProp,
            int rowsPerPage) {
        super(id, addSpaceColumns(columns), new MasterDataProvider(masterList, defaultSortProp), rowsPerPage);

        add(new CssClass("groupable-table grouped"));
        setItemReuseStrategy(new MasterDetailItemsStrategy(this));
        addBottomToolbar(new EmptyRowToolbar(this));
    }

    protected boolean isMasterExpanded(M masterObject) {
        return openedItems.contains(masterObject);
    }

    @SuppressWarnings({"unchecked"})
    protected Item newMasterRow(IModel rowModel, int index) {
        // add row item
        Item rowItem = new Item("master" + index, index, null);
        rowItem.setRenderBodyOnly(true);

        // add cell item
        Item cellItem = new Item("cells", 0, null);
        cellItem.setRenderBodyOnly(true);
        rowItem.add(cellItem);

        // add master panel
        final M masterObject = ((MasterDetailEntry<M, D>) rowModel.getObject()).getMaster();
        cellItem.add(new MasterDetailRowPanel<>("cell", masterObject, this));
        return rowItem;
    }

    @SuppressWarnings({"unchecked"})
    protected void onMasterToggle(MasterDetailRowPanel row, AjaxRequestTarget target) {
        final M m = (M) row.getDefaultModelObject();
        if (openedItems.contains(m)) {
            openedItems.remove(m);
        } else {
            openedItems.add(m);
        }
        target.add(MasterDetailTable.this);
    }

    protected abstract String getMasterLabel(M masterObject);

    protected abstract List<D> getDetails(M masterObject);


    private static class SpaceColumn extends AbstractColumn {
        private String cssClass;

        public SpaceColumn(String cssClass) {
            super(Model.of(""));
            this.cssClass = cssClass;
        }

        @Override
        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
            cellItem.add(new Label(componentId, "."));
        }

        @Override
        public String getCssClass() {
            return cssClass;
        }
    }

    private static class MasterDataProvider<M extends Serializable, D extends Serializable>
            extends SortableDataProvider {
        private List<?> list;

        private MasterDataProvider(List<?> list, String defaultSortProp) {
            this.list = list;
            setSort(defaultSortProp, SortOrder.DESCENDING);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public Iterator iterator(int first, int count) {
            final SortParam sortParam = getSort();
            if (sortParam != null && sortParam.getProperty().startsWith("master.")) {
                ListPropertySorter.sort(list, sortParam);
                final String property = sortParam.getProperty().substring(7);
                final SortDefinition sortDefinition = new MutableSortDefinition(property, true,
                        sortParam.isAscending());
                Collections.sort(list, new PropertyComparator(sortDefinition));
            }
            List<?> result = list.subList(first, first + count);
            return result.iterator();
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public IModel model(Object object) {
            return new Model(new MasterDetailEntry<M, D>((M) object, null));
        }
    }

    private static List<IColumn> addSpaceColumns(List<IColumn> columns) {
        columns.add(0, new SpaceColumn("first-cell"));
        columns.add(new SpaceColumn("last-cell"));
        return columns;
    }
}
