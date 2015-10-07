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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.wicket.behavior.SubmitOnceBehavior;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import static org.artifactory.util.Files.removeFile;

/**
 * @author Chen  Keinan
 */
public class LogoFileUploadForm extends FileUploadForm {
    @SpringBean
    private CentralConfigService centralConfig;

    public LogoFileUploadForm(String name, UploadListener listener) {
        super(name, ContextHelper.get().getArtifactoryHome().getTempUploadDir().getAbsolutePath(), listener);
        add(new SubmitOnceBehavior());
        //Set maximum upload size
        int uploadMaxSizeMb = centralConfig.getDescriptor().getFileUploadMaxSizeMb();
        if (uploadMaxSizeMb > 0) {
            setMaxSize(Bytes.megabytes(uploadMaxSizeMb));
        }
    }

    @Override
    protected void onSubmit() {
        final FileUpload upload = fileUploadField.getFileUpload();
        if (upload != null) {
            //Create a new file
            final String clientFileName = FilenameUtils.getName(upload.getClientFileName());
            uploadedFile = new File(tempUploadsDir, clientFileName);
            //Check new file, delete if it already existed
            if (!removeFile(uploadedFile)) {
                error("File " + uploadedFile + " already exists and cannot be deleted !!");
                uploadedFile = null;
                return;
            }
            boolean isFakeImage;
            try {
                //Save to a new file
                FileUtils.forceMkdir(tempUploadsDir);
                uploadedFile.createNewFile();
                upload.writeTo(uploadedFile);
                isFakeImage = isImageFake();
                if (!isFakeImage) {
                    listener.onFileSaved(uploadedFile);
                } else {
                    removeUploadedFile();
                    error("The Uploaded File Contain a Non Valid Image Format");
                }

            } catch (Exception e) {
                listener.onException();
                removeUploadedFile();
                throw new IllegalStateException(
                        "Unable to write file to '" + tempUploadsDir.getAbsolutePath() + "'.", e);
            } finally {
                upload.closeStreams();
            }
        }
    }

    /**
     * check if the image has fake format , its not a real image
     * this check done to eliminate security issue
     *
     * @return
     * @throws IOException
     */
    private boolean isImageFake() throws Exception {
        boolean isFakeImage = false;
        ImageInputStream imageInputStream = null;
        try {
            Path path = Paths.get(uploadedFile.getCanonicalPath());
            byte[] data = Files.readAllBytes(path);
            imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
            Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) {
                isFakeImage = true;
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (imageInputStream != null) {
                try {
                    imageInputStream.close();
                } catch (IOException e) {
                    throw new IOException(e);
                }
            }
        }
        return isFakeImage;
    }
}
