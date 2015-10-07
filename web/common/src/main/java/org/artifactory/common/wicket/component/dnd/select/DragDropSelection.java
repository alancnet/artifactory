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

package org.artifactory.common.wicket.component.dnd.select;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.dnd.list.DragDropList;
import org.artifactory.common.wicket.component.links.SimpleTitledLink;
import org.artifactory.common.wicket.component.template.HtmlTemplate;
import org.artifactory.common.wicket.contributor.ResourcePackage;
import org.artifactory.common.wicket.resources.basewidget.BaseWidget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * @author Yoav Aharoni
 */
public class DragDropSelection<T extends Serializable> extends FormComponentPanel<T> {
    private IModel<? extends List<? extends T>> choicesModel;
    private IChoiceRenderer<T> renderer;
    private List<T> unselectedItems;

    public DragDropSelection(final String id, final List<? extends T> choices) {
        this(id, new WildcardListModel<>(choices), new ChoiceRenderer<T>());
    }

    public DragDropSelection(final String id, IModel<T> model, final List<? extends T> choices) {
        this(id, model, new WildcardListModel<>(choices), new ChoiceRenderer<T>());
    }

    public DragDropSelection(final String id, final IModel<? extends List<? extends T>> choicesModel,
            IChoiceRenderer<T> renderer) {
        super(id);
        this.choicesModel = choicesModel;
        this.renderer = renderer;
        init();
    }

    public DragDropSelection(final String id, IModel<T> model, final IModel<? extends List<? extends T>> choicesModel,
            final IChoiceRenderer<T> renderer) {
        super(id, model);
        this.choicesModel = choicesModel;
        this.renderer = renderer;
        init();
    }

    protected void init() {
        ResourcePackage resourcePackage = ResourcePackage.forJavaScript(DragDropSelection.class);
        resourcePackage.dependsOn(new BaseWidget());
        add(resourcePackage);

        add(new CssClass(new PropertyModel(this, "cssClass")));
        setOutputMarkupId(true);

        BaseDragDropList sourceList = new SourceDragDropList("sourceList");
        add(sourceList);

        BaseDragDropList targetList = new TargetDragDropList("targetList");
        add(targetList);

        HiddenField selectionField = new SelectionField("selection");
        add(selectionField);

        SimpleTitledLink addLink = new SimpleTitledLink("addLink", ">>");
        add(addLink);

        SimpleTitledLink removeLink = new SimpleTitledLink("removeLink", "<<");
        add(removeLink);

        SimpleTitledLink addAllLink = new SimpleTitledLink("addAllLink", ">>>");
        add(addAllLink);

        SimpleTitledLink removeAllLink = new SimpleTitledLink("removeAllLink", "<<<");
        add(removeAllLink);

        add(new Label("sourceTitle", new TitleModel("selection.source")));
        add(new Label("targetTitle", new TitleModel("selection.target")));

        // add init script
        HtmlTemplate template = new HtmlTemplate("initScript") {
            @Override
            public boolean isVisible() {
                return super.isVisible() && isScriptRendered();
            }
        };
        template.setParameter("widgetClassName", new PropertyModel(this, "widgetClassName"));
        template.setParameter("panelId", new PropertyModel(this, "markupId"));
        template.setParameter("sourceListId", new PropertyModel(sourceList, "markupId"));
        template.setParameter("targetListId", new PropertyModel(targetList, "markupId"));
        template.setParameter("addLinkId", new PropertyModel(addLink, "markupId"));
        template.setParameter("removeLinkId", new PropertyModel(removeLink, "markupId"));
        template.setParameter("addAllLinkId", new PropertyModel(addAllLink, "markupId"));
        template.setParameter("removeAllLinkId", new PropertyModel(removeAllLink, "markupId"));
        template.setParameter("textFieldId", new PropertyModel(selectionField, "markupId"));
        add(template);
    }

    public String getCssClass() {
        if (isEnabled()) {
            return "dnd-selection";
        } else {
            return "dnd-selection disabled";
        }
    }

    protected boolean isScriptRendered() {
        return isEnabled();
    }

    public String getWidgetClassName() {
        return "artifactory.DragDropSelection";
    }

    protected String getDndValue(ListItem item) {
        return getMarkupId();
    }

    protected String getAcceptedSourceTypes() {
        return getMarkupId();
    }

    protected String getAcceptedTargetTypes() {
        return getMarkupId();
    }

    protected void onOrderChanged(AjaxRequestTarget target) {
    }

    protected Behavior newOnOrderChangeEventBehavior(String event) {
        return new OnOrderChangedEventBehavior(event);
    }

    @SuppressWarnings({"unchecked"})
    protected void populateItem(ListItem item) {
        T itemObject = (T) item.getDefaultModelObject();
        List<T> choices = (List<T>) choicesModel.getObject();
        int index = choices.indexOf(itemObject);
        item.add(new AttributeModifier("idx", index));
    }

    protected Collection<T> createNewSelectionCollection(int length) {
        return new ArrayList<>(length);
    }

    @Override
    public void updateModel() {
        // do nothing, model is updated by TargetSelectionModel
    }

    public IModel getChoices() {
        return choicesModel;
    }

    public void setChoices(IModel<? extends List<? extends T>> choices) {
        choicesModel = choices;
    }

    @SuppressWarnings({"unchecked"})
    public void setChoices(List<? extends T> choices) {
        choicesModel = new Model((Serializable) choices);
    }

    public IChoiceRenderer<T> getChoiceRenderer() {
        return renderer;
    }

    public void setChoiceRenderer(IChoiceRenderer<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    protected void onBeforeRender() {
        updateSourceList();
        super.onBeforeRender();
    }

    @SuppressWarnings({"unchecked"})
    private void updateSourceList() {
        unselectedItems = new ArrayList<>(choicesModel.getObject());
        Collection<T> selected = (Collection<T>) getDefaultModelObject();
        if (isNotEmpty(selected)) {
            unselectedItems.removeAll(selected);
        }
    }

    protected String getSortValue(ListItem item) {
        return null;
    }

    private class SourceListModel extends AbstractReadOnlyModel<List<? extends T>> {
        @Override
        public List<? extends T> getObject() {
            return unselectedItems;
        }
    }

    private class TargetListModel extends AbstractReadOnlyModel<List<? extends T>> {
        @SuppressWarnings({"unchecked"})
        @Override
        public List<? extends T> getObject() {
            Collection<T> selected = (Collection<T>) getDefaultModelObject();
            if (selected instanceof List) {
                return (List<? extends T>) selected;
            }
            if (isEmpty(selected)) {
                return Collections.emptyList();
            }
            return new ArrayList<>(selected);
        }
    }

    private class TitleModel extends AbstractReadOnlyModel {
        private String key;

        private TitleModel(String key) {
            this.key = key;
        }

        @Override
        public Object getObject() {
            String globalTitle = getString(key, null, key);
            return getString(getMarkupId() + "." + key, null, globalTitle);
        }
    }

    private abstract class BaseDragDropList extends DragDropList<T> {
        private BaseDragDropList(String id, IModel<? extends List<? extends T>> listModel) {
            super(id, listModel, getChoiceRenderer());
            setOutputMarkupId(true);
        }

        @Override
        protected String getSortValue(ListItem item) {
            return DragDropSelection.this.getSortValue(item);
        }

        @Override
        public String getDndValue(ListItem item) {
            return DragDropSelection.this.getDndValue(item);
        }

        @Override
        protected void populateItem(ListItem<T> item) {
            super.populateItem(item);
            DragDropSelection.this.populateItem(item);
        }
    }

    private class SourceDragDropList extends BaseDragDropList {
        private SourceDragDropList(String id) {
            super(id, new SourceListModel());
        }

        @Override
        public String getAcceptedDndTypes() {
            return getAcceptedSourceTypes();
        }
    }

    private class TargetDragDropList extends BaseDragDropList {
        private TargetDragDropList(String id) {
            super(id, new TargetListModel());
        }

        @Override
        public String getAcceptedDndTypes() {
            return getAcceptedTargetTypes();
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

    private class TargetSelectionModel extends Model<String> {
        private TargetSelectionModel() {
            // make sure model will notify change for empty selection
            super("-");
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public void setObject(String selectionString) {
            super.setObject(selectionString);
            if ("-".equals(selectionString)) {
                return;
            }

            if (selectionString == null) {
                setDefaultModelObject(createNewSelectionCollection(0));
                return;
            }

            String[] selectedIndices = selectionString.split(",");

            // get newSelection list
            Collection<T> newSelection = createNewSelectionCollection(selectedIndices.length);

            // fill newSelection
            List<T> choices = (List<T>) choicesModel.getObject();
            for (String index : selectedIndices) {
                Integer intIndex = Integer.valueOf(index);
                if (intIndex != -1) {
                    newSelection.add(choices.get(intIndex));
                }
            }
            setDefaultModelObject(newSelection);
        }
    }

    private class SelectionField extends HiddenField<String> {
        private SelectionField(String id) {
            super(id, new TargetSelectionModel());
            setOutputMarkupId(true);
            add(newOnOrderChangeEventBehavior("onOrderChanged"));
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && DragDropSelection.this.isEnabled();
        }
    }
}
