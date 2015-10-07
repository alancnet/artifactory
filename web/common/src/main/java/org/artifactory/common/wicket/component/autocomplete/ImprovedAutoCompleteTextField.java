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

package org.artifactory.common.wicket.component.autocomplete;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.StringAutoCompleteRenderer;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.artifactory.common.wicket.behavior.CssClass;

import java.util.Iterator;

/**
 * @author Yoav Aharoni
 */
public abstract class ImprovedAutoCompleteTextField<T> extends AutoCompleteTextField<T> {
    public static final AutoCompleteSettings DEFAULT_SETTINGS =
            new AutoCompleteSettings().setShowListOnEmptyInput(true).setMaxHeightInPx(200);

    private static final ResourceReference AUTOCOMPLETE_JS = new JavaScriptResourceReference(
            ImprovedAutoCompleteBehavior.class, "improved-autocomplete.js");

    public ImprovedAutoCompleteTextField(String id, IModel<T> model, Class<T> type, AutoCompleteSettings settings) {
        this(id, model, type, StringAutoCompleteRenderer.<T>instance(), settings);
    }

    public ImprovedAutoCompleteTextField(String id, IModel<T> object, AutoCompleteSettings settings) {
        this(id, object, null, settings);
    }

    public ImprovedAutoCompleteTextField(String id, IModel<T> object) {
        this(id, object, null, DEFAULT_SETTINGS);
    }

    public ImprovedAutoCompleteTextField(String id, AutoCompleteSettings settings) {
        this(id, null, settings);
    }

    public ImprovedAutoCompleteTextField(String id) {
        this(id, null, DEFAULT_SETTINGS);
    }

    public ImprovedAutoCompleteTextField(String id, IAutoCompleteRenderer<T> renderer) {
        this(id, (IModel<T>) null, renderer);
    }

    public ImprovedAutoCompleteTextField(String id, Class<T> type, IAutoCompleteRenderer<T> renderer) {
        this(id, null, type, renderer, DEFAULT_SETTINGS);
    }

    public ImprovedAutoCompleteTextField(String id, IModel<T> model, IAutoCompleteRenderer<T> renderer) {
        this(id, model, null, renderer, DEFAULT_SETTINGS);
    }

    public ImprovedAutoCompleteTextField(String id, IModel<T> model, Class<T> type, IAutoCompleteRenderer<T> renderer,
            AutoCompleteSettings settings) {
        super(id, model, type, renderer, settings);

        add(new CssClass("text autocomplete"));
        add(new AttributeModifier("autocomplete", "off"));
    }

    @Override
    protected AutoCompleteBehavior<T> newAutoCompleteBehavior(IAutoCompleteRenderer<T> renderer,
            AutoCompleteSettings settings) {
        return new AutoCompleteBehavior<T>(renderer, settings) {

            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                super.renderHead(component, response);
                response.renderJavaScriptReference(AUTOCOMPLETE_JS);
            }

            @Override
            protected Iterator<T> getChoices(String input) {
                return ImprovedAutoCompleteTextField.this.getChoices(input);
            }
        };
    }
}
