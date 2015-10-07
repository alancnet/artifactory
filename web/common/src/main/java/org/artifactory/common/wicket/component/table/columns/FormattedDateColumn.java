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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.artifactory.api.config.CentralConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Extension of the property column meant for date columns to be formatted according to the pattern that is defined
 * in the artifactory.config.xml
 *
 * @author Tomer Cohen
 * @see org.artifactory.api.config.CentralConfigService#getDateFormatter()
 */
public class FormattedDateColumn<T> extends PropertyColumn<T> {
    private static final Logger log = LoggerFactory.getLogger(FormattedDateColumn.class);

    private CentralConfigService centralConfigService;
    private String originalFormat;

    public FormattedDateColumn(IModel<String> displayModel, String sortProperty, String propertyExpression,
            CentralConfigService centralConfigService, String originalFormat) {
        super(displayModel, sortProperty, propertyExpression);
        this.centralConfigService = centralConfigService;
        this.originalFormat = originalFormat;
    }

    @Override
    protected IModel<String> createLabelModel(IModel<T> embeddedModel) {
        IModel<String> model = (IModel<String>) super.createLabelModel(embeddedModel);
        String formattedDate = model.getObject();
        if (StringUtils.isNotBlank(originalFormat)) {
            try {
                DateFormat simpleDateFormat = new SimpleDateFormat(originalFormat);
                long dateAsLong = simpleDateFormat.parse(formattedDate).getTime();
                formattedDate = centralConfigService.getDateFormatter().print(dateAsLong);
            } catch (ParseException e) {
                log.warn("Failed to parse date {} using format: {}", formattedDate, originalFormat);
            }
        }
        return Model.of(formattedDate);
    }
}
