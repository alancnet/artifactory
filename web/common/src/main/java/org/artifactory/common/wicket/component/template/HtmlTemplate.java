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

package org.artifactory.common.wicket.component.template;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yoav Aharoni
 */
public class HtmlTemplate extends WebComponent {
    private Map<String, IModel> parametersMap;

    public HtmlTemplate(String id) {
        super(id);

        parametersMap = new HashMap<>();
        setEscapeModelStrings(false);
    }

    @SuppressWarnings({"RefusedBequest"})
    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        String rawHtml = readBodyMarkup(markupStream, openTag);
        String interpolatedHtml = ModelVariableInterpolator.interpolate(rawHtml, parametersMap);
        getResponse().write(interpolatedHtml);
    }

    public Map<String, IModel> getParametersMap() {
        return parametersMap;
    }

    public void setParameter(String key, String value) {
        parametersMap.put(key, Model.of(value));
    }

    public void setParameter(String key, IModel modelValue) {
        parametersMap.put(key, modelValue);
    }

    public static String readBodyMarkup(MarkupStream markupStream, ComponentTag openTag) {
        StringBuilder innerMarkup = new StringBuilder();
        while (markupStream.hasMore()) {
            if (markupStream.get().closes(openTag)) {
                return innerMarkup.toString();
            }
            innerMarkup.append(markupStream.get().toCharSequence().toString());
            markupStream.next();
        }
        throw new MarkupException(markupStream, "Expected close tag for " + openTag);
    }
}