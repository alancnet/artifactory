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

package org.artifactory.common.wicket.component.table.columns;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.Model;

/**
 * A {@link PropertyColumn} that adds constructors for string titles.
 *
 * @author Yossi Shaul
 */
public class TitlePropertyColumn<T> extends PropertyColumn<T> {
    public TitlePropertyColumn(String title, String propertyExpression) {
        super(Model.of(title), propertyExpression);
    }

    public TitlePropertyColumn(String title, String sortProperty, String propertyExpression) {
        super(Model.of(title), sortProperty, propertyExpression);
    }
}
