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

package org.artifactory.common.wicket.component.deletable.label;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.CssClass;

/**
 * @author Yoav Aharoni
 */
public abstract class DeletableLabel extends Panel {
    private boolean labelClickable = false;
    private boolean labelDeletable = false;

    public DeletableLabel(String id, String text) {
        this(id, Model.of(text));
    }

    public DeletableLabel(String id, IModel model) {
        super(id, model);

        add(new CssClass(new AbstractReadOnlyModel() {
            @Override
            public Object getObject() {
                return isLabelDeletable() ? "deletable" : "deletable undeletable";
            }
        }));

        Label label = new Label("label", new DeletegeModel());
        label.add(new AjaxEventBehavior("onclick") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                onLabelClicked(target);
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("onmouseover", "DeletableLabel.setClass(this, 'overlabel')");
                tag.put("onmouseout", "DeletableLabel.setClass(this, '')");
            }

            @Override
            public boolean isEnabled(Component component) {
                return super.isEnabled(component) && isLabelClickable();
            }
        });
        add(label);

        add(new AjaxLink("link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                onDeleteClicked(target);
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("onmouseover", "DeletableLabel.setClass(this, 'overlink')");
                tag.put("onmouseout", "DeletableLabel.setClass(this, '')");
            }

        });
    }

    public void onDeleteClicked(AjaxRequestTarget target) {
    }

    public void onLabelClicked(AjaxRequestTarget target) {
    }

    public boolean isLabelClickable() {
        return labelClickable;
    }

    public void setLabelClickable(boolean labelClickable) {
        this.labelClickable = labelClickable;
    }

    public boolean isLabelDeletable() {
        return labelDeletable;
    }

    public void setLabelDeletable(boolean labelDeletable) {
        this.labelDeletable = labelDeletable;
    }

    private class DeletegeModel extends AbstractReadOnlyModel {
        @Override
        public Object getObject() {
            return DeletableLabel.this.getDefaultModelObject();
        }
    }
}
