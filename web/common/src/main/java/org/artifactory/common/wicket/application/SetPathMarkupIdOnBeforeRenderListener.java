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

package org.artifactory.common.wicket.application;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.application.IComponentOnBeforeRenderListener;
import org.apache.wicket.util.string.Strings;

/**
 * @author Yoav Aharoni
 */
public class SetPathMarkupIdOnBeforeRenderListener implements IComponentOnBeforeRenderListener {
    @Override
    public void onBeforeRender(Component component) {
        String markupId = component.getMarkupId(false);
        if (markupId == null) {
            String id = getPath(component);
            if (id != null) {
                component.setMarkupId(id);
                component.setOutputMarkupId(true);
            }
        }
    }

    protected String getPath(Component component) {
        String path = component.getPageRelativePath();
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        // escape some noncompliant characters
        path = Strings.replaceAll(path, "_", "__").toString();
        path = path.replace('.', '_');
        path = path.replace('-', '_');
        path = path.replace(' ', '_');
        path = path.replace(':', '_');
        return path;
    }
}
