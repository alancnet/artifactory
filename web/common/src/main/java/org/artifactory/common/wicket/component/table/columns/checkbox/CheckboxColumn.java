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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.table.columns.panel.checkbox.CheckboxPanel;

import static org.artifactory.common.wicket.component.table.columns.panel.checkbox.CheckboxPanel.CHECKBOX_ID;

/**
 * @author Yoav Aharoni
 */
public class CheckboxColumn<T> extends AbstractColumn<T> {
    private String expression;

    public CheckboxColumn(String title, String expression, String sortProperty) {
        super(Model.of(title), sortProperty);
        this.expression = expression;
    }

    @Override
    public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
        T rowObject = rowModel.getObject();

        CheckboxPanel panel = new CheckboxPanel(componentId, rowModel);
        cellItem.add(new CssClass("CheckboxColumn"));
        cellItem.add(panel);

        IModel<Boolean> model = newPropertyModel(rowObject);
        FormComponent checkBox = newCheckBox(CHECKBOX_ID, model, rowObject);
        panel.add(checkBox);

        boolean enabled = isEnabled(rowObject);
        checkBox.setEnabled(enabled);
    }

    protected FormComponent newCheckBox(String id, IModel<Boolean> model, T rowObject) {
        return new CheckBox(id, model);
    }

    protected IModel<Boolean> newPropertyModel(T rowObject) {
        return new PropertyModel<>(rowObject, expression);
    }

    protected boolean isEnabled(T rowObject) {
        return true;
    }

    protected final String getExpression() {
        return expression;
    }
}