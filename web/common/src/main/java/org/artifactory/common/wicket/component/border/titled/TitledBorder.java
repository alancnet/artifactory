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

package org.artifactory.common.wicket.component.border.titled;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.model.IModel;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.PlaceHolder;
import org.artifactory.common.wicket.component.panel.titled.TitleLabel;
import org.artifactory.common.wicket.model.Titled;

/**
 * @author Yoav Aharoni
 */
public class TitledBorder extends Border implements Titled {
    private String cssPrefix;

    public TitledBorder(String id) {
        this(id, "border");
    }

    public TitledBorder(String id, IModel model) {
        this(id, model, "border");
    }

    public TitledBorder(String id, String cssPrefix) {
        this(id, null, cssPrefix);
    }

    public TitledBorder(String id, IModel model, String cssPrefix) {
        super(id, model);
        this.cssPrefix = cssPrefix;
        init();
    }

    private void init() {
        setOutputMarkupId(true);
        add(new CssClass(cssPrefix + "-wrapper"));

        // building the borders
        WebMarkupContainer border = createBorder("top");
        addToBorder(border);
        border = addBorder(border, "left");
        border = addBorder(border, "right");
        border = addBorder(border, "top-left");
        border = addBorder(border, "top-right");
        border = addBorder(border, "bottom");
        border = addBorder(border, "bottom-left");
        border = addBorder(border, "bottom-right");

        HeaderContainer headerContainer = new HeaderContainer();
        headerContainer.add(new CssClass(cssPrefix + "-title"));
        border.add(headerContainer);

        TitleLabel titleLabel = new TitleLabel(this);
        headerContainer.add(titleLabel);
        headerContainer.add(newToolbar("tool"));
    }

    private WebMarkupContainer addBorder(WebMarkupContainer container, String cssClass) {
        WebMarkupContainer border = createBorder(cssClass);
        container.add(border);
        return border;
    }

    private WebMarkupContainer createBorder(String cssClass) {
        WebMarkupContainer border = new WebMarkupContainer("border");
        border.add(new AttributeModifier("class", getCssPrefix() + "-" + cssClass));
        return border;
    }

    protected String getCssPrefix() {
        return cssPrefix;
    }

    @Override
    public String getTitle() {
        return getString(getId(), null, "");
    }

    protected Component newToolbar(String id) {
        return new PlaceHolder(id);
    }

    private class HeaderContainer extends WebMarkupContainer {
        public HeaderContainer() {
            super("title");
        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            Component tool = get("tool");
            setVisible(StringUtils.isNotEmpty(getTitle()) || ((tool != null) && !(tool instanceof PlaceHolder)));
        }
    }
}
