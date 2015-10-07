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

package org.artifactory.webapp.wicket.page.search;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.artifactory.api.search.artifact.ArtifactSearchResult;
import org.artifactory.common.wicket.component.table.groupable.column.GroupableColumn;
import org.artifactory.webapp.wicket.page.search.actionable.ActionableSearchResult;

/**
 * Last modified groupable table column
 *
 * @author Shay Yaakov
 */
public class LastModifiedColumn extends GroupableColumn<ActionableSearchResult<ArtifactSearchResult>>
        implements IChoiceRenderer<ActionableSearchResult<ArtifactSearchResult>> {

    public LastModifiedColumn() {
        super(Model.of("Modified"), "searchResult.lastModified", "searchResult.lastModifiedString");
    }

    @Override
    public String getGroupProperty() {
        return "searchResult.lastModifiedDay";
    }

    @Override
    public Object getDisplayValue(ActionableSearchResult<ArtifactSearchResult> object) {
        return object.getSearchResult().getLastModifiedString();
    }

    @Override
    public String getIdValue(ActionableSearchResult<ArtifactSearchResult> object, int index) {
        return object.getSearchResult().getLastModifiedDay();
    }
}
