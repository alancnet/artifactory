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

package org.artifactory.common.wicket.component.modal.panel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.modal.HasModalHandler;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.panel.feedback.aggregated.AggregateFeedbackPanel;
import org.artifactory.common.wicket.event.EventBus;
import org.artifactory.common.wicket.event.Listener;
import org.artifactory.common.wicket.model.Titled;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public class BaseModalPanel<E extends Serializable> extends Panel implements Titled, HasModalHandler {
    public static final String MODAL_ID = ModalHandler.CONTENT_ID;
    protected static final String TITLE_KEY = "panel.title";

    private EventBus eventBus = new EventBus();

    private int minimalWidth = 100;
    private int minimalHeight = 50;
    private int initialWidth = 600;
    private int initialHeight = 0;

    protected String title;

    public BaseModalPanel() {
        super(MODAL_ID);
    }

    public BaseModalPanel(IModel model) {
        super(MODAL_ID, model);
    }

    public BaseModalPanel(E entity) {
        super(MODAL_ID, new CompoundPropertyModel(entity));
    }

    private ModalHandler modalHandler;

    {
        setOutputMarkupId(true);

        // add modalHandler
        modalHandler = new ModalHandler("modalHandler");
        add(modalHandler);

        AggregateFeedbackPanel feedback = new AggregateFeedbackPanel("feedback");
        feedback.add(new JavascriptEvent("onshow", "ModalHandler.onError();"));
        feedback.add(new JavascriptEvent("ondestroy", "ModalHandler.onError();"));
        feedback.addMessagesSource(this);
        add(feedback);

        ModalHandler.getInstanceFor(this);
    }

    @Override
    public ModalHandler getModalHandler() {
        return modalHandler;
    }

    public boolean isResizable() {
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public E getPanelModelObject() {
        return (E) getDefaultModelObject();
    }

    public void setPanelModelObject(E object) {
        setDefaultModelObject(object);
    }

    @SuppressWarnings({"unchecked"})
    public IModel<E> getPanelModel() {
        return (IModel<E>) getDefaultModel();
    }

    public void setPanelModel(IModel<E> object) {
        setDefaultModel(object);
    }

    public StringResourceModel getResourceModel(String key) {
        return new StringResourceModel(key, this, null, "??" + key + "??");
    }

    public String getResourceString(String key) {
        return getString(key, null, "??" + key + "??");
    }

    @Override
    public String getTitle() {
        return title == null ? getResourceString(TITLE_KEY) : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void onShow(AjaxRequestTarget target) {
        eventBus.fire(ShowEvent.INSTANCE);
    }

    public void addShowListener(Listener<ShowEvent> listener) {
        eventBus.addListener(ShowEvent.class, listener);
    }

    public int getMinimalWidth() {
        return minimalWidth;
    }

    public void setMinimalWidth(int minimalWidth) {
        this.minimalWidth = minimalWidth;
    }

    public int getMinimalHeight() {
        return minimalHeight;
    }

    public void setMinimalHeight(int minimalHeight) {
        this.minimalHeight = minimalHeight;
    }

    public int getInitialWidth() {
        return initialWidth;
    }

    public void setInitialWidth(int initialWidth) {
        this.initialWidth = initialWidth;
    }

    public int getInitialHeight() {
        return initialHeight;
    }

    public void setInitialHeight(int initialHeight) {
        this.initialHeight = initialHeight;
    }

    public void setWidth(int width) {
        setMinimalWidth(width);
        setInitialWidth(width);
    }

    /**
     * Sets the height of the content (not including the caption)
     *
     * @param height Height of the content in pixels
     */
    public void setHeight(int height) {
        setMinimalHeight(height);
        setInitialHeight(height);
    }

    public String getCookieName() {
        return getClass().getSimpleName();
    }

    public void close(AjaxRequestTarget target) {
        ModalHandler modalHandler = ModalHandler.getInstanceFor(this);
        modalHandler.close(target);
        modalHandler.setContent(new WebMarkupContainer(MODAL_ID));
    }

    /**
     * onClose event handler. Override onClose to run your code upon closing the modal panel.
     *
     * @param target AjaxRequestTarget
     */
    public void onClose(AjaxRequestTarget target) {
    }

    public void onCloseButtonClicked(AjaxRequestTarget target) {
    }

    /**
     * Bind modal panel height to a given component.
     *
     * @param component
     */
    public void bindHeightTo(Component component) {
        component.setOutputMarkupId(true);
        bindHeightTo(component.getMarkupId());
    }

    /**
     * Bind modal panel height to an html element with a given.
     *
     * @param markupId
     */
    public void bindHeightTo(final String markupId) {
        addShowListener(new Listener<ShowEvent>() {
            @Override
            public void onEvent(ShowEvent event) {
                ModalHandler.bindHeightTo(markupId);
            }
        });
    }

    public void setDefaultFocusField(final Component defaultFocusField) {
        defaultFocusField.setOutputMarkupId(true);
        addShowListener(new Listener<ShowEvent>() {
            @Override
            public void onEvent(ShowEvent event) {
                AjaxRequestTarget.get().focusComponent(defaultFocusField);
            }
        });
    }

    public static class ShowEvent {
        public static final ShowEvent INSTANCE = new ShowEvent();
    }
}
