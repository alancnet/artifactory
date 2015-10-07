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

package org.artifactory.mime.version.converter.v3;

import org.artifactory.mime.MimeType;
import org.artifactory.mime.MimeTypeBuilder;
import org.artifactory.mime.version.converter.MimeTypeConverterBase;
import org.jdom2.Document;

/**
 * Adds the following entries to the mimetypes file if they don't exist:
 * <pre>
 *   <mimetype type="application/x-nupkg" extensions="nupkg" archive="true" index="true" css="nupkg"/>
 *   <mimetype type="application/x-nuspec+xml" extensions="nuspec" viewable="true" syntax="xml" css="xml"/>
 * </pre>
 *
 * @author Yossi Shaul
 */
public class NuPkgMimeTypeConverter extends MimeTypeConverterBase {
    @Override
    public void convert(Document doc) {

        MimeType nupkg = new MimeTypeBuilder("application/x-nupkg")
                .extensions("nupkg").css("nupkg").archive(true).index(true).build();
        addIfNotExist(doc, nupkg);

        MimeType nupspec = new MimeTypeBuilder("application/x-nuspec+xml")
                .extensions("nuspec").css("xml").syntax("xml").viewable(true).build();
        addIfNotExist(doc, nupspec);
    }
}
