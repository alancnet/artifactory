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

package org.artifactory.webapp.wicket.panel.advanced;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.PlaceHolder;
import org.artifactory.common.wicket.component.panel.titled.TitleLabel;
import org.artifactory.common.wicket.model.Titled;

/**
 * @author Eli Givoni
 */
public class WhiteTitlePanel extends Panel implements Titled {
    protected static final String TITLE_KEY = "panel.title";

    public WhiteTitlePanel(String id) {
        super(id);
        init();
    }

    public WhiteTitlePanel(String id, IModel model) {
        super(id, model);
        init();
    }


    private void init() {
        setOutputMarkupId(true);
        add(new TitleLabel(this));
        add(newToolbar("tool"));
    }

    @Override
    public String getTitle() {
        return getString(TITLE_KEY, null);
    }

    protected Component newToolbar(String id) {
        return new PlaceHolder(id);
    }
}
