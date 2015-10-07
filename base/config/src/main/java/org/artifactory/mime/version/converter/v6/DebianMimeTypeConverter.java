/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.mime.version.converter.v6;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;

/**
 * Adds the following entries to the mimetypes file if they don't exist:
 * <pre>
 *         <mimetype type="application/x-debian-package" extensions="deb" css="deb"/>
 * </pre>
 *
 * @author Yossi Shaul
 */
public class DebianMimeTypeConverter extends MimeTypeConverterBase {
    @Override
    public void convert(Document doc) {
        MimeType json = new MimeTypeBuilder("application/x-debian-package").extensions("deb").css("deb")
                .build();
        addIfNotExist(doc, json);
    }
}
