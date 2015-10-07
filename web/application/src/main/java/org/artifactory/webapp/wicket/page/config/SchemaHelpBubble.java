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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.descriptor.Descriptor;

/**
 * Schema help bubble is a help bubble that uses schema help model which retrieves the help message from the artifactory
 * schema.
 *
 * @author Yossi Shaul
 */
public class SchemaHelpBubble extends HelpBubble {
    private static final String HTML_PREFIX = "(HTML)";
    private String property;

    /**
     * This constructor will remove the .help from the id for the property name.
     *
     * @param id The wicket id (must end with '.help')
     */
    public SchemaHelpBubble(String id) {
        this(id, id.substring(0, id.indexOf(".help")));
    }

    public SchemaHelpBubble(String id, String property) {
        super(id);
        this.property = property;
    }

    public SchemaHelpBubble(String id, SchemaHelpModel model) {
        super(id, model);
    }

    @Override
    protected IModel initModel() {
        IModel iModel = getParent().getInnermostModel();
        Descriptor descriptor = (Descriptor) iModel.getObject();
        return new SchemaHelpModel(descriptor, property);
    }

    @Override
    protected String getTooltipMarkup() {
        String html = getDefaultModelObjectAsString();
        if (StringUtils.isEmpty(html)) {
            return "";
        }
        if (html.startsWith(HTML_PREFIX)) {
            return html.substring(HTML_PREFIX.length());
        }
        return getDefaultModelObjectAsString().replaceAll("\n", "<br/>");
    }
}
