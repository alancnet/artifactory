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

package org.artifactory.common.wicket.panel.upload;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;

/**
 * @author Yoav Aharoni
 */
public class HideUploadProgressDecorator implements IAjaxCallDecorator {
    public static final String SHOW_SCRIPT = "document.getElementById('uploadProgress').style.display = '';";
    public static final String HIDE_SCRIPT = "document.getElementById('uploadProgress').style.display = 'none';";

    @Override
    public CharSequence decorateScript(Component c, CharSequence script) {
        return HIDE_SCRIPT + script;
    }

    @Override
    public CharSequence decorateOnSuccessScript(Component c, CharSequence script) {
        return SHOW_SCRIPT + script;
    }

    @Override
    public CharSequence decorateOnFailureScript(Component c, CharSequence script) {
        return SHOW_SCRIPT + script;
    }
}
