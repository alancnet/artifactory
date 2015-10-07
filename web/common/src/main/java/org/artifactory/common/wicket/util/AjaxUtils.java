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

package org.artifactory.common.wicket.util;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * @author Yoav Aharoni
 */
public abstract class AjaxUtils {
    private AjaxUtils() {
        // utility class
    }

    public static void refreshFeedback() {
        refreshFeedback(AjaxRequestTarget.get());
    }

    public static void refreshFeedback(final AjaxRequestTarget target) {
        Page page = WicketUtils.getPage();
        if (page == null || target == null) {
            return;
        }
        page.visitChildren(IFeedback.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component component, IVisit<Void> visit) {
                if (component.getOutputMarkupId()) {
                    target.add(component);
                }
            }
        });
    }

    public static void render(Component component, String markupId) {
        final String componentId = component.getMarkupId();
        AjaxRequestTarget.get().add(component, markupId);
        component.setMarkupId(componentId);
    }

}
