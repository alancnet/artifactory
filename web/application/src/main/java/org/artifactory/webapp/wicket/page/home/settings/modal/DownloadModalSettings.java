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

package org.artifactory.webapp.wicket.page.home.settings.modal;

import org.artifactory.common.wicket.component.label.highlighter.Syntax;

import java.io.Serializable;

/**
 * Settings param for editable/readOnly settings modal panel
 *
 * @author Shay Yaakov
 */
public class DownloadModalSettings implements Serializable {
    private String content;
    private String settingsMimeType;
    private String saveToFileName;
    private String downloadButtonTitle;
    private Syntax syntax;

    public DownloadModalSettings(String content, String settingsMimeType, String saveToFileName, Syntax syntax,
            String downloadButtonTitle) {
        this.content = content;
        this.settingsMimeType = settingsMimeType;
        this.saveToFileName = saveToFileName;
        this.syntax = syntax;
        this.downloadButtonTitle = downloadButtonTitle;
    }

    public String getContent() {
        return content;
    }

    public String getSettingsMimeType() {
        return settingsMimeType;
    }

    public String getSaveToFileName() {
        return saveToFileName;
    }

    public String getDownloadButtonTitle() {
        return downloadButtonTitle;
    }

    public Syntax getSyntax() {
        return syntax;
    }
}
