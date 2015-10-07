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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.util.AjaxUtils;

/**
 * AjaxButton can get it's text from resourceKey or from markup
 *
 * @author Yoav Aharoni
 */
public abstract class TitledAjaxLink extends BaseTitledLink implements IAjaxLink, ILinkListener {

    protected TitledAjaxLink(String id) {
        super(id);
    }

    protected TitledAjaxLink(String id, IModel titleModel) {
        super(id, titleModel);
    }

    protected TitledAjaxLink(String id, String title) {
        super(id, title);
    }

    {
        add(new AjaxEventBehavior("onclick") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                onClick(target);
                AjaxUtils.refreshFeedback(target);
            }

            @SuppressWarnings({"RefusedBequest"})
            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new CancelEventIfNoAjaxDecorator(TitledAjaxLink.this.getAjaxCallDecorator());
            }
        });
    }

    @Override
    protected CharSequence getURL() {
        return urlFor(INTERFACE);
    }

    @Override
    public void onLinkClicked() {
        AjaxRequestTarget dummyAjaxRequestTarget = new AjaxRequestTarget(getPage());
        onClick(dummyAjaxRequestTarget);
    }

    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return null;
    }
}
