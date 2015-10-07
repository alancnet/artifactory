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

package org.artifactory.webapp.wicket.page.deploy.step1;

import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.panel.upload.UploadListener;
import org.artifactory.webapp.wicket.page.deploy.step2.DeployArtifactPanel;
import org.artifactory.webapp.wicket.panel.upload.DefaultFileUploadForm;

import java.io.File;

/**
 * @author Yoav Aharoni
 */
public class UploadArtifactPanel extends TitledPanel implements UploadListener {
    public UploadArtifactPanel() {
        super("deployArtifactPanel");

        add(new ArtifactUploadForm(this));
    }

    @Override
    public void onException() {
        error("Error occurred while uploading file.");
    }

    @Override
    public void onFileSaved(File file) {
        replaceWith(new DeployArtifactPanel(getId(), file));
    }

    @Override
    public void info(String message) {
        super.info(message);
    }

    private static class ArtifactUploadForm extends DefaultFileUploadForm {
        private ArtifactUploadForm(UploadArtifactPanel listener) {
            super("uploadForm", listener);
        }

        @Override
        protected void cleanupOnDetach() {
        }
    }
}
