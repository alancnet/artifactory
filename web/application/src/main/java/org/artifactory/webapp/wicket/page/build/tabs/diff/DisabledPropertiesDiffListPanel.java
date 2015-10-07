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

package org.artifactory.webapp.wicket.page.build.tabs.diff;

import com.google.common.collect.Lists;
import org.artifactory.webapp.wicket.page.build.actionable.BuildsDiffActionableItem;
import org.jfrog.build.api.Build;

import java.util.List;

/**
 * @author Shay Yaakov
 */
public class DisabledPropertiesDiffListPanel extends BasePropertiesDiffListPanel {

    public DisabledPropertiesDiffListPanel(String id) {
        super(id, null);
        addTable();
    }

    @Override
    protected List<BuildsDiffActionableItem> getProperties(Build selectedBuild) {
        return Lists.newArrayList();
    }
}
