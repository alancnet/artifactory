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

package org.artifactory.common.wicket.behavior.filteringselect;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.artifactory.common.wicket.behavior.template.TemplateBehavior;
import org.artifactory.common.wicket.resources.dojo.DojoPackage;

/**
 * Adding this behavior to DropDownChoice will convert it to Dojo FilteringSelect widget.<br/> <br/> <b>NOTE!</b> When
 * using this behavior will not be able add the dropdown to ajax target regularly, meaning <b>you can't do
 * <code>target.add(dropDown)</code></b>. Instead add a containing parent:
 * <code>target.add(anyParent)</code> or add <b>"-widget"</b> to target markup id like so:
 * <code>target.add(dropdown, dropdown.getMarkupId() <b>+ "-widget"</b>)</code>
 *
 * @author Yoav Aharoni
 */
public class FilteringSelectBehavior extends TemplateBehavior {
    public FilteringSelectBehavior() {
        super(FilteringSelectBehavior.class);
        getResourcePackage().dependsOn(new DojoPackage()).addJavaScript();
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        assertTagName(tag, "select");
        tag.put("dojoType", "dijit.form.FilteringSelect");
        tag.put("required", isRequired(component) ? "true" : "false");
    }

    protected boolean isRequired(Component component) {
        if (component instanceof FormComponent) {
            return ((FormComponent) component).isRequired();
        }

        return true;
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        component.setOutputMarkupId(true);
    }
}
