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

package org.artifactory.common.wicket.component.panel.sortedlist;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.links.SimpleTitledLink;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.component.table.columns.panel.links.LinksColumnPanel;
import org.artifactory.common.wicket.component.template.HtmlTemplate;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.model.DelegatedModel;
import org.artifactory.common.wicket.resources.basewidget.BaseWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Yoav Aharoni
 */
public abstract class OrderedListPanel<T extends Serializable> extends TitledPanel {
    private static final Logger log = LoggerFactory.getLogger(OrderedListPanel.class);

    @SuppressWarnings({"unchecked"})
    protected OrderedListPanel(String id, List<T> list) {
        this(id, new Model((Serializable) list));
    }

    protected OrderedListPanel(String id, IModel<List<T>> listModel) {
        super(id, listModel);
        init();
    }

    protected void init() {
        ResourcePackage resourcePackage = ResourcePackage.forJavaScript(OrderedListPanel.class);
        resourcePackage.dependsOn(new BaseWidget());
        add(resourcePackage);

        add(new CssClass("list-panel ordered-list-panel"));

        // add moveUp/Down links
        SimpleTitledLink upLink = new SimpleTitledLink("moveUpLink", "Up");
        add(upLink);

        SimpleTitledLink downLink = new SimpleTitledLink("moveDownLink", "Down");
        add(downLink);

        // add new item link
        add(new ModalShowLink("newItemLink", "New") {
            @Override
            protected BaseModalPanel getModelPanel() {
                return newCreateItemPanel();
            }
        });

        //Add import link
        add(getImportLink("importItemLink"));

        //add save repo list order
        Component saveOrderLink = getSaveOrderLink("saveOrderLink");
        add(saveOrderLink);

        //add cancel order button
        Component cancelOrderLink = getCancelOrderLink("cancelOrderLink");
        add(cancelOrderLink);
        // add items container
        WebMarkupContainer container = new WebMarkupContainer("items");
        container.setOutputMarkupId(true);
        container.add(new AttributeModifier("dojoType", "artifactory.dnd.Source"));
        container.add(new AttributeAppender("accept", new DndTypeModel(), ","));
        add(container);

        // add ListView
        ListView listView = new ListView<T>("item", new DelegatedModel<List<T>>(this)) {
            @Override
            protected void populateItem(ListItem<T> item) {
                OrderedListPanel.this.populateItem(item);
                item.add(new CssClass(item.getIndex() % 2 == 0 ? "even" : "odd"));
                item.add(new JavascriptEvent("onclick", ""));
            }
        };
        container.add(listView);

        // add hidden text field
        HiddenField textField = new IndicesHiddenField("listIndices");
        textField.setOutputMarkupId(true);
        textField.add(newOnOrderChangeEventBehavior("onOrderChanged"));
        add(textField);

        // add init script
        HtmlTemplate template = new HtmlTemplate("initScript");
        template.setOutputMarkupId(true);
        template.setParameter("listId", new PropertyModel(container, "markupId"));
        template.setParameter("textFieldId", new PropertyModel(textField, "markupId"));
        template.setParameter("upLinkId", new PropertyModel(upLink, "markupId"));
        template.setParameter("downLinkId", new PropertyModel(downLink, "markupId"));
        add(template);
    }

    private Component getCancelOrderLink(String id) {
        SimpleTitledLink cancelOrder = new SimpleTitledLink(id, "Reset");
        cancelOrder.add(new AjaxEventBehavior("onclick") {

            @Override
            protected void onEvent(AjaxRequestTarget target) {
                resetListOrder(target);
            }
        });
        return cancelOrder;
    }

    private Component getSaveOrderLink(String id) {
        SimpleTitledLink saveOrder = new SimpleTitledLink(id, "Save");
        saveOrder.add(new AjaxEventBehavior("onclick") {

            @Override
            protected void onEvent(AjaxRequestTarget target) {
                saveItems(target);
            }
        });
        return saveOrder;
    }

    protected Component getImportLink(String id) {
        WebMarkupContainer child = new WebMarkupContainer(id);
        child.setVisible(false);
        return child;
    }

    @SuppressWarnings({"RefusedBequest"})
    @Override
    public String getTitle() {
        return getString(getId() + ".title", null, getId() + ".title");
    }

    protected abstract void saveItems(AjaxRequestTarget target);

    protected abstract void resetListOrder(AjaxRequestTarget target);

    protected abstract String getItemDisplayValue(T itemObject);

    protected abstract BaseModalPanel newCreateItemPanel();

    protected abstract List<? extends AbstractLink> getItemActions(T itemObject, String linkId);

    protected void populateItem(ListItem<T> item) {
        item.add(new AttributeModifier("dndType", new DndTypeModel()));
        item.add(new CssClass("dojoDndItem"));

        T itemObject = item.getModelObject();
        item.add(new Label("name", getItemDisplayValue(itemObject)));

        LinksColumnPanel linksPanel = new LinksColumnPanel("actions");
        item.add(linksPanel);
        List<? extends AbstractLink> links = getItemActions(itemObject, "link");
        for (AbstractLink link : links) {
            linksPanel.addLink(link);
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<T> getList() {
        return (List<T>) getDefaultModelObject();
    }

    protected void onOrderChanged(AjaxRequestTarget target) {
        target.appendJavaScript(format("dojo.byId('%s')._panel.resetIndices();", get("items").getMarkupId()));
    }

    public void refresh(AjaxRequestTarget target) {
        target.add(get("items"));
        target.add(get("title"));
        target.add(get("listIndices"));
        target.add(get("moveDownLink"));
        target.add(get("moveUpLink"));
        target.add(get("initScript"));
        target.prependJavaScript("LinksColumn.hideCurrent();");
    }

    protected Behavior newOnOrderChangeEventBehavior(String event) {
        return new OnOrderChangedEventBehavior(event);
    }

    private class DndTypeModel extends AbstractReadOnlyModel {
        @Override
        public Object getObject() {
            return "dnd-" + getMarkupId();
        }
    }

    private class IndicesHiddenField extends HiddenField<T> {
        private IndicesHiddenField(String id) {
            super(id, Model.<T>of());
        }

        @Override
        public void updateModel() {
            int[] indices = createIndicesList(getConvertedInput());
            if (indices == null) {
                return;
            }

            // reorder list according to indices
            List<T> newList = new ArrayList<>(indices.length);
            List<T> choices = getList();
            for (int index : indices) {
                newList.add(choices.get(index));
            }
            OrderedListPanel.this.setDefaultModelObject(newList);
        }

        /**
         * @param value hidden indices field value
         * @return indices reorder list or null if indices order hasn't changed.
         */
        private int[] createIndicesList(Object value) {
            if (value == null) {
                return null;
            }

            String indicesString = value.toString();
            String[] strings = indicesString.split(",");

            // sanity
            if (strings.length != getList().size()) {
                log.error("Indices list size mismatch model list size (reorder ignored).");
                return null;
            }

            // check if order changed and create indices array
            int[] indices = new int[strings.length];
            boolean changed = false;
            for (int i = 0; i < strings.length; i++) {
                String indexString = strings[i];
                Integer index = Integer.valueOf(indexString);
                changed = changed || index != i;
                indices[i] = index;
            }

            if (!changed) {
                return null;
            }
            return indices;
        }

    }

    public class OnOrderChangedEventBehavior extends AjaxFormComponentUpdatingBehavior {
        private OnOrderChangedEventBehavior(String event) {
            super(event);
        }

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            onOrderChanged(target);
        }
    }
}