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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;

/**
 * @author Yoav Aharoni
 */
public class AjaxCheckboxColumn<T> extends StyledCheckboxColumn<T> {
    public AjaxCheckboxColumn(String title, String expression, String sortProperty) {
        super(title, expression, sortProperty);
    }

    @Override
    protected FormComponent<Boolean> newCheckBox(String id, IModel<Boolean> model, final T rowObject) {
        final FormComponent<Boolean> checkbox = super.newCheckBox(id, model, rowObject);

        checkbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Boolean checked = checkbox.getModelObject();
                AjaxCheckboxColumn.this.onUpdate(checkbox, rowObject, checked, target);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return AjaxCheckboxColumn.this.getAjaxCallDecorator();
            }
        });
        return checkbox;
    }

    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return null;
    }

    /**
     * Called when the checkbox is updated (checked/unchecked).
     *
     * @param checkbox  The updated checkbox.
     * @param rowObject The affected row model.
     * @param value     True if the checkbox is checked.
     * @param target    The ajax target (the table container is added by default).
     */
    protected void onUpdate(FormComponent checkbox, T rowObject, boolean value, AjaxRequestTarget target) {
    }
}
