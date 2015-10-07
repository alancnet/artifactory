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

import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;

public class TitledPageLink extends BaseTitledLink {
    private final Class<? extends Page> pageClass;

    public TitledPageLink(String id, String title, Class<? extends Page> pageClass) {
        super(id, title);
        this.pageClass = pageClass;
    }

    @Override
    protected CharSequence getURL() {
        return urlFor(pageClass, null);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        addOnClickOpenScript(tag);
    }
}
