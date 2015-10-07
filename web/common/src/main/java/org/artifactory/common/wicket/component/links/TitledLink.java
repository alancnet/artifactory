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

package org.artifactory.common.wicket.component.links;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;

/**
 * @author Yoav Aharoni
 */
public abstract class TitledLink extends BaseTitledLink implements ILinkListener {

    protected TitledLink(String id) {
        super(id);
    }

    protected TitledLink(String id, IModel titleModel) {
        super(id, titleModel);
    }

    protected TitledLink(String id, String title) {
        super(id, title);
    }

    public abstract void onClick();

    @Override
    public void onLinkClicked() {
        onClick();
    }

    @Override
    protected CharSequence getURL() {
        return urlFor(INTERFACE);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        addOnClickOpenScript(tag);
    }
}
