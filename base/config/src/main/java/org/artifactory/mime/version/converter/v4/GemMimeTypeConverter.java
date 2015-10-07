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

package org.artifactory.mime.version.converter.v4;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;

/**
 * Adds the following entries to the mimetypes file if they don't exist:
 * <pre>
 *    <mimetype type="application/x-rubygems" extensions="gem" css="gem"/>
 *    <mimetype type="application/x-ruby-marshal" extensions="rz" css="ruby-marshal"/>
 * </pre>
 *
 * @author Yossi Shaul
 */
public class GemMimeTypeConverter extends MimeTypeConverterBase {
    @Override
    public void convert(Document doc) {
        MimeType gemType = new MimeTypeBuilder("application/x-rubygems").extensions("gem").css("gem").build();
        addIfNotExist(doc, gemType);

        MimeType rzType = new MimeTypeBuilder("application/x-ruby-marshal").extensions("rz").build();
        addIfNotExist(doc, rzType);
    }

}
