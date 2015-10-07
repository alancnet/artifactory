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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author yoava
 */

public abstract class ImprovedAutoCompleteBehavior<T> extends AutoCompleteBehavior<T> {

    private static final ResourceReference IMPROVED_AUTOCOMPLETE_JS = new JavaScriptResourceReference(
            ImprovedAutoCompleteBehavior.class, "improved-autocomplete.js");

    protected ImprovedAutoCompleteBehavior(IAutoCompleteRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.renderJavaScriptReference(IMPROVED_AUTOCOMPLETE_JS);
    }

}
