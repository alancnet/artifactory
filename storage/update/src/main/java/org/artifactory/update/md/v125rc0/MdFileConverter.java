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

package org.artifactory.update.md.v125rc0;

import org.artifactory.repo.RepoPath;
import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataConverterUtils;
import org.artifactory.update.md.MetadataType;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

/**
 * @author freds
 * @date Nov 11, 2008
 */
public class MdFileConverter implements MetadataConverter {
    @Override
    public String getNewMetadataName() {
        return "artifactory-file";
    }

    @Override
    public MetadataType getSupportedMetadataType() {
        return MetadataType.file;
    }

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        rootElement.setName(getNewMetadataName());
        RepoPath repoPath = MetadataConverterUtils.extractRepoPath(rootElement);
        List<Element> toMove = MetadataConverterUtils.extractExtensionFields(rootElement);
        MetadataConverterUtils.addNewContent(rootElement, repoPath, toMove);
        // Not used anymore
        rootElement.removeChild(MdFolderConverter.ARTIFACTORY_NAME);
        rootElement.removeChild("xmlAware");
        rootElement.removeChild("downloadCount");
    }
}
