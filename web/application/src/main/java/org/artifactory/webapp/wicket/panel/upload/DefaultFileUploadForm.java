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

package org.artifactory.webapp.wicket.panel.upload;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.wicket.behavior.SubmitOnceBehavior;
import org.artifactory.common.wicket.panel.upload.FileUploadForm;
import org.artifactory.common.wicket.panel.upload.UploadListener;

/**
 * Default file upload form that takes the max upload size and temp upload directory from artifactory config.
 *
 * @author Yossi Shaul
 */
public class DefaultFileUploadForm extends FileUploadForm {

    @SpringBean
    private CentralConfigService centralConfig;

    public DefaultFileUploadForm(String name, UploadListener listener) {
        super(name, ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath(), listener);
        add(new SubmitOnceBehavior());

        //Set maximum upload size
        int uploadMaxSizeMb = centralConfig.getDescriptor().getFileUploadMaxSizeMb();
        if (uploadMaxSizeMb > 0) {
            setMaxSize(Bytes.megabytes(uploadMaxSizeMb));
        }
    }
}
