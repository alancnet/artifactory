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

package org.artifactory.update.md.v130beta3;

import org.artifactory.repo.RepoPath;
import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataConverterUtils;
import org.artifactory.update.md.MetadataType;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

/**
 * @author freds
 * @date Nov 9, 2008
 */
public class ArtifactoryFolderConverter implements MetadataConverter {
    public static final String ARTIFACTORY_FOLDER = "artifactory.folder";

    @Override
    public String getNewMetadataName() {
        return "artifactory-folder";
    }

    @Override
    public MetadataType getSupportedMetadataType() {
        return MetadataType.folder;
    }

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        rootElement.setName(getNewMetadataName());
        RepoPath repoPath = MetadataConverterUtils.extractRepoPath(rootElement);
        List<Element> toMove = MetadataConverterUtils.extractExtensionFields(rootElement);
        MetadataConverterUtils.addNewContent(rootElement, repoPath, toMove);
    }
}
