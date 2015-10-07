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

package org.artifactory.common.wicket.component.links;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.model.Titled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.MissingResourceException;

/**
 * @author Yoav Aharoni
 */
public class BaseTitledLink extends AbstractLink implements Titled {
    private static final Logger LOG = LoggerFactory.getLogger(BaseTitledLink.class);
    private boolean wasOpenCloseTag;
    private boolean styled;

    public BaseTitledLink(String id) {
        this(id, id);
    }

    public BaseTitledLink(String id, IModel titleModel) {
        super(id, titleModel);
    }

    public BaseTitledLink(String id, String title) {
        super(id);
        setDefaultModel(Model.of(title));
    }

    protected void addOnClickOpenScript(ComponentTag tag) {
        if (isEnabled() && !"a".equalsIgnoreCase(tag.getName())) {
            tag.put("onclick", getOpenScript());
        }
    }

    protected String getOpenScript() {
        return "window.location.href='" + getURL() + "';";
    }

    public boolean isStyled() {
        return styled;
    }

    public void setStyled(boolean styled) {
        this.styled = styled;
        if (styled) {
            add(new CssClass("styled"));
        }
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        if (tag.isOpenClose()) {
            wasOpenCloseTag = true;
            tag.setType(XmlTag.TagType.OPEN);
        }

        super.onComponentTag(tag);

        tag.put("class", getCssClass(tag));
        if ("input".equalsIgnoreCase(tag.getName())) {
            tag.put("value", getTitle());
        }

        if (isEnabled()) {
            // is a <a> or other tag
            if ("a".equalsIgnoreCase(tag.getName())) {
                tag.put("href", Strings.replaceAll(getURL(), "&", "&amp;"));
            }
        } else {
            disableLink(tag);
        }
    }

    protected CharSequence getURL() {
        return "#";
    }

    @SuppressWarnings({"RefusedBequest"})
    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        if (!"input".equalsIgnoreCase(openTag.getName())) {
            String html = getInnerHtml(openTag);
            replaceBody(markupStream, openTag, html);
        }
    }

    @Override
    public String getTitle() {
        try {
            if (getDefaultModelObjectAsString() == null) {
                LOG.error(getClass().getSimpleName()
                        + " title model is null, using id instead.");

                return "??" + getId() + "??";
            }

            return getDefaultModelObjectAsString();

        } catch (MissingResourceException e) {
            LOG.error(getClass().getSimpleName()
                    + " can't find text resource, using id instead { " + e.getMessage() + " }.");

            return "??" + getId() + "??";
        }
    }

    protected String getInnerHtml(ComponentTag tag) {
        if (styled || "button".equalsIgnoreCase(tag.getName())) {
            return "<span class='button-center'><span class='button-left'><span class='button-right'>"
                    + getTitle() + "</span></span></span>";
        }
        return getDefaultModelObjectAsString();
    }

    protected String getCssClass(ComponentTag tag) {
        String oldCssClass = StringUtils.defaultString(tag.getAttributes().getString("class"));
        if (!isEnabled()) {
            return oldCssClass + " button " + oldCssClass + "-disabled button-disabled";
        }
        return oldCssClass + " button";
    }

    protected final void replaceBody(MarkupStream markupStream, ComponentTag openTag, String html) {
        replaceComponentTagBody(markupStream, openTag, html);

        if (!wasOpenCloseTag) {
            markupStream.skipRawMarkup();
            if (!markupStream.get().closes(openTag)) {
                throw new MarkupException("close tag not found for tag: " + openTag.toString() +
                        ". Component: " + toString());
            }
        }
    }
}

