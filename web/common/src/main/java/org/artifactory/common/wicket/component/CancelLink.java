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

package org.artifactory.common.wicket.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;

/**
 * The cancel button is used to clear the input form data.
 *
 * @author Yossi Shaul
 */
public class CancelLink extends TitledAjaxLink {
    private Form form;

    public CancelLink(Form form) {
        this("cancel", form);
    }

    public CancelLink(String id, Form form) {
        this(id, form, "Cancel");
    }

    public CancelLink(String id, Form form, String caption) {
        super(id, caption);
        this.form = form;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        form.clearInput();
        if (form.getOutputMarkupId()) {
            target.add(form);
        }
    }
}
