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

package org.artifactory.common.wicket.component.dnd.select.sorted;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.dnd.select.DragDropSelection;
import org.artifactory.common.wicket.contributor.ResourcePackage;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class SortedDragDropSelection<T extends Serializable> extends DragDropSelection<T> {

    public SortedDragDropSelection(String id, List<T> choices) {
        super(id, choices);
    }

    public SortedDragDropSelection(String id, IModel<T> model, List<T> choices) {
        super(id, model, choices);
    }

    {
        add(ResourcePackage.forJavaScript(SortedDragDropSelection.class));
    }

    @Override
    public String getWidgetClassName() {
        return isEnabled() ? "artifactory.SortedDragDropSelection" : "artifactory.DisabledSortedDragDropSelection";
    }

    @Override
    protected Behavior newOnOrderChangeEventBehavior(String event) {
        // no ajax notification
        return new JavascriptEvent(event, "");
    }

    @Override
    protected boolean isScriptRendered() {
        return true;
    }
}
