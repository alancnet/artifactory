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

package org.artifactory.common.wicket.component.image;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author Eli Givoni
 */
public class ExternalImage extends WebMarkupContainer {
    public ExternalImage(String id, IModel urlModel) {
        super(id, urlModel);
        setOutputMarkupId(true);
    }

    public ExternalImage(String id, String url) {
        super(id, Model.of(url));
        setOutputMarkupId(true);
    }

    public ExternalImage(String id) {
        this(id, (String) null);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible() && StringUtils.isNotEmpty(getDefaultModelObjectAsString());
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "img");
        tag.put("src", getDefaultModelObjectAsString());
    }
}