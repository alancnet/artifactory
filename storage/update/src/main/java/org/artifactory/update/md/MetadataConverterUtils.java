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

package org.artifactory.update.md;

import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @author freds
 * @date Nov 11, 2008
 */
public abstract class MetadataConverterUtils {
    private static final String CREATED_BY = "createdBy";
    private static final String MODIFIED_BY = "modifiedBy";
    private static final String REPO_PATH = "repoPath";
    private static final String REPO_KEY = "repoKey";
    private static final String PATH = "path";
    private static final String NAME = "name";
    private static final String EXTENSION = "extension";
    private static final String REL_PATH = "relPath";

    private static final String[] EXTENSION_FIELDS = {"lastUpdated", MODIFIED_BY, CREATED_BY, "sha1", "md5"};

    private MetadataConverterUtils() {
        // utility class
    }

    public static List<Element> extractExtensionFields(Element rootElement) {
        String modifiedBy = rootElement.getChildText(MODIFIED_BY);
        List<Element> toMove = new ArrayList<>(EXTENSION_FIELDS.length);
        for (String tagName : EXTENSION_FIELDS) {
            Element element = rootElement.getChild(tagName);
            if (element != null) {
                toMove.add(element);
                rootElement.removeChild(tagName);
            } else {
                if (CREATED_BY.equals(tagName)) {
                    toMove.add(new Element(CREATED_BY).setText(modifiedBy));
                }
            }
        }
        return toMove;
    }

    public static void addNewContent(Element rootElement, RepoPath repoPath, List<Element> toMove) {
        rootElement.addContent(new Element(NAME).setText(repoPath.getName()));
        rootElement.addContent(new Element(REPO_PATH).
                addContent(new Element(REPO_KEY).setText(repoPath.getRepoKey())).
                addContent(new Element(PATH).setText(repoPath.getPath())));
        rootElement.addContent(new Element(EXTENSION).addContent(toMove));
    }

    public static RepoPath extractRepoPath(Element rootElement) {
        String repoKey = rootElement.getChildText(REPO_KEY);
        String relPath = rootElement.getChildText(REL_PATH);
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, relPath);
        rootElement.removeChild(REPO_KEY);
        rootElement.removeChild(REL_PATH);
        return repoPath;
    }

    public static String convertString(MetadataConverter converter, String xmlContent) {
        Document doc = XmlUtils.parse(xmlContent);
        converter.convert(doc);
        return XmlUtils.outputString(doc);
    }
}
