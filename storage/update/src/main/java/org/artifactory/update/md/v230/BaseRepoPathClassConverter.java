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

package org.artifactory.update.md.v230;

import org.artifactory.update.md.MetadataConverter;
import org.artifactory.update.md.MetadataType;
import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Converts file and folder info metadata created between versions 2.3.0-2.3.4.1.
 * They contained repo path tags which had the class attribute by mistake (caused by splitting the repo path to a class
 * and impl).
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseRepoPathClassConverter implements MetadataConverter {

    @Override
    public void convert(Document doc) {
        Element rootElement = doc.getRootElement();
        Element repoPath = rootElement.getChild("repoPath");
        if (repoPath != null) {
            if (repoPath.getAttribute("class") != null) {
                repoPath.removeAttribute("class");
            }
        }
    }

    public static class FileRepoPathClassConverter extends BaseRepoPathClassConverter {

        @Override
        public String getNewMetadataName() {
            return "artifactory-file";
        }

        @Override
        public MetadataType getSupportedMetadataType() {
            return MetadataType.file;
        }
    }

    public static class FolderRepoPathClassConverter extends BaseRepoPathClassConverter {

        @Override
        public String getNewMetadataName() {
            return "artifactory-folder";
        }

        @Override
        public MetadataType getSupportedMetadataType() {
            return MetadataType.folder;
        }
    }

    public static class WatchersRepoPathClassConverter extends BaseRepoPathClassConverter {

        @Override
        public String getNewMetadataName() {
            return "watchers";
        }

        @Override
        public MetadataType getSupportedMetadataType() {
            return MetadataType.watch;
        }
    }
}