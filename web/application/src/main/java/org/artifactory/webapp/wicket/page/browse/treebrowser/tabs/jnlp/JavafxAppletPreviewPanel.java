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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.jnlp;

import org.apache.wicket.markup.html.basic.Label;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.web.ui.skins.GreenModalPageSkin;
import org.artifactory.webapp.wicket.util.JnlpUtils;

/**
 * Displays an applet jnlp code snippet and preview.
 *
 * @author Yossi Shaul
 */
public class JavafxAppletPreviewPanel extends BaseModalPanel {
    public JavafxAppletPreviewPanel(JnlpUtils.AppletInfo appletInfo) {
        setTitle(appletInfo.getAppletName() + " Preview");
        add(new GreenModalPageSkin());
        setWidth(Math.max(appletInfo.getWidth() + 40, 300));
        setHeight(Math.max(appletInfo.getHeight() + 20, 300));

        // embbed the script for execution in the page
        add(new Label("executableScript", appletInfo.getScriptSnippet()).setEscapeModelStrings(false));
    }
}
