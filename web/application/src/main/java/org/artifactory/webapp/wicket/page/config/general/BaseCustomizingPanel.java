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

package org.artifactory.webapp.wicket.page.config.general;

import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;

import java.io.File;

/**
 * @author Yoav Aharoni
 */
public class BaseCustomizingPanel extends TitledPanel {
    public BaseCustomizingPanel(String id, IModel model) {
        super(id, model);
    }

    public void cleanup() {
    }

    public File getUploadedFile() {
        return null;
    }

    public boolean shouldDeleteLogo() {
        return false;
    }
}
