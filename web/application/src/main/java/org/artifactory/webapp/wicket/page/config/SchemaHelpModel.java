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

package org.artifactory.webapp.wicket.page.config;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.webapp.wicket.util.DescriptionExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This model extracts the help message from the artifactory schema using the descriptor and property.
 *
 * @author Yossi Shaul
 */
public class SchemaHelpModel extends AbstractReadOnlyModel {
    private static final Logger log = LoggerFactory.getLogger(SchemaHelpModel.class);

    private Descriptor descriptor;
    private String propertyName;

    public SchemaHelpModel(Descriptor descriptor, String propertyName) {
        this.descriptor = descriptor;
        this.propertyName = propertyName;
    }

    @Override
    public Object getObject() {
        DescriptionExtractor extractor = DescriptionExtractor.getInstance();
        String helpMessage;
        try {
            helpMessage = extractor.getDescription(descriptor, propertyName);
        } catch (Exception e) {
            // don't fail, just log and return an empty string
            log.error(String.format("Failed to extract help message for descriptor '%s' with property '%s': %s",
                    descriptor, propertyName, e.getMessage()));
            helpMessage = "";
        }
        return helpMessage;
    }
}
