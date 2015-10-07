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

package org.artifactory.api.search.artifact;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.search.SearchResultBase;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * @author Yoav Landman
 */
public class ArtifactSearchResult extends SearchResultBase {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd")
            .withLocale(Locale.ENGLISH);

    public ArtifactSearchResult(org.artifactory.fs.ItemInfo itemInfo) {
        super(itemInfo);
    }

    public String getLastModifiedDay() {
        return DAY_FORMAT.print(getLastModified());
    }

    // Used by Wicket
    @SuppressWarnings("UnusedDeclaration")
    public String getLastModifiedString() {
        long lastModified = getItemInfo().getLastModified();
        return ContextHelper.get().getCentralConfig().format(lastModified);
    }

    public long getLastModified() {
        return getItemInfo().getLastModified();
    }
}