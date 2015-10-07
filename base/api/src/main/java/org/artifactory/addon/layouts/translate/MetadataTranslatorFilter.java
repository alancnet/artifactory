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

package org.artifactory.addon.layouts.translate;

import org.artifactory.mime.NamingUtils;

/**
 * Filters metadata names
 *
 * @author Noam Y. Tenne
 */
public class MetadataTranslatorFilter implements TranslatorFilter {

    @Override
    public boolean filterRequired(String path) {
        return NamingUtils.isMetadata(path);
    }

    @Override
    public String getFilteredContent(String path) {
        return NamingUtils.getMetadataName(path);
    }

    @Override
    public String stripPath(String path) {
        return NamingUtils.stripMetadataFromPath(path);
    }

    @Override
    public String applyFilteredContent(String strippedPath, String filteredContent) {
        return NamingUtils.getMetadataPath(strippedPath, filteredContent);
    }
}
