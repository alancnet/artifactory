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

package org.artifactory.common.wicket.component.table.columns.checkbox;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.PropertyResolver;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.table.SortableTable;
import org.artifactory.common.wicket.component.table.columns.panel.checkbox.CheckboxPanel;

import java.util.Iterator;

import static org.artifactory.common.wicket.component.table.columns.panel.checkbox.CheckboxPanel.CHECKBOX_ID;

/**
 * @author Yoav Aharoni
 */
public class SelectAllCheckboxColumn<T> extends AjaxCheckboxColumn<T> {
    private IModel<Boolean> selectAllModel = new Model<>(false);
    private StyledCheckbox selectAllCheckbox;

    public SelectAllCheckboxColumn(String title, String expression, String sortProperty) {
        super(title, expression, sortProperty);
    }

    @Override
    public Component getHeader(String componentId) {
        CheckboxPanel panel = new CheckboxPanel(componentId);
        selectAllCheckbox = new StyledCheckbox(CHECKBOX_ID, getSelectAllModel());
        selectAllCheckbox.setTitle(getDisplayModel().getObject());
        selectAllCheckbox.add(new AttributeModifier("title", "Select All"));
        selectAllCheckbox.setOutputMarkupId(true);
        selectAllCheckbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @SuppressWarnings({"unchecked"})
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                SortableTable<T> table = (SortableTable<T>) selectAllCheckbox.findParent(DataTable.class);
                selectAll(table, target);
                onSelectAllUpdate(target);
            }
        });
        panel.add(selectAllCheckbox);
        return panel;
    }

    @SuppressWarnings({"unchecked"})
    private void selectAll(SortableTable<T> table, AjaxRequestTarget target) {
        ISortableDataProvider<T> dataProvider = table.getSortableDataProvider();
        Iterator<T> iterator = (Iterator<T>) dataProvider.iterator(0, dataProvider.size());
        while (iterator.hasNext()) {
            T rowObject = iterator.next();
            if (canChangeItemSelectionState(rowObject)) {
                PropertyResolver.setValue(getExpression(), rowObject, isSelectAll(), null);
            }
        }
        onSelectAll(iterator, target);
        target.add(table);
    }

    /**
     * Indicates whether the given row item should respond to state changes from the select all column checkbox
     *
     * @param rowItem Row item to check
     * @return True if the row item should be affected
     */
    protected boolean canChangeItemSelectionState(T rowItem) {
        return true;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void onSelectAll(Iterator<T> iterator, AjaxRequestTarget target) {
    }

    @Override
    protected void onUpdate(FormComponent checkbox, T rowObject, boolean value, AjaxRequestTarget target) {
        super.onUpdate(checkbox, rowObject, value, target);
        setSelectAll(false);
        target.add(selectAllCheckbox);
    }

    /**
     * Called after the select all checkbox was updated
     *
     * @param target Request target
     */
    protected void onSelectAllUpdate(AjaxRequestTarget target) {
    }

    public IModel<Boolean> getSelectAllModel() {
        return selectAllModel;
    }

    public void setSelectAllModel(IModel<Boolean> selectAllModel) {
        this.selectAllModel = selectAllModel;
    }

    public boolean isSelectAll() {
        return getSelectAllModel().getObject();
    }

    public void setSelectAll(boolean selectAll) {
        getSelectAllModel().setObject(selectAll);
    }
}
