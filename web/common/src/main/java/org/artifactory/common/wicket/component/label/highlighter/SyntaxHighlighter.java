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

package org.artifactory.common.wicket.component.label.highlighter;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import static java.lang.String.format;
import static org.artifactory.common.wicket.util.JavaScriptUtils.jsFunctionCall;

/**
 * @author Yoav Aharoni
 */
public class SyntaxHighlighter extends WebComponent implements IHeaderContributor {
    private static final ResourceReference CORE_JS_REFERENCE = new JavaScriptResourceReference(SyntaxHighlighter.class,
            "resources/scripts/shCore.js");
    private static final ResourceReference CORE_CSS_REFERENCE = new CssResourceReference(SyntaxHighlighter.class,
            "resources/styles/shCore.css");
    private static final ResourceReference JAVASCRIPT_REFERENCE = new JavaScriptResourceReference(
            SyntaxHighlighter.class,
            "SyntaxHighlighter.js");
    private static final ResourceReference CLIPBOARD_SWF_REFERENCE = new PackageResourceReference(
            SyntaxHighlighter.class,
            "resources/scripts/clipboard.swf");

    private Theme theme = Theme.Default;
    private Syntax syntax = Syntax.plain;
    private boolean gutter = false;
    private boolean toolbar = true;
    private boolean autoLinks = true;
    private boolean wrapLines = false;

    public SyntaxHighlighter(String id) {
        this(id, (IModel) null);
    }

    public SyntaxHighlighter(String id, String code) {
        this(id, Model.of(code));
    }

    public SyntaxHighlighter(String id, IModel model) {
        super(id, model);
    }

    public SyntaxHighlighter(String id, String code, Syntax syntax) {
        this(id, code);
        if (syntax != null) {
            this.syntax = syntax;
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        // add css
        response.renderCSSReference(CORE_CSS_REFERENCE);
        response.renderCSSReference(theme.getCssReference());

        // add javascript
        response.renderJavaScriptReference(CORE_JS_REFERENCE);
        response.renderJavaScriptReference(syntax.getJsReference());
        response.renderJavaScriptReference(JAVASCRIPT_REFERENCE);

        response.renderOnDomReadyJavaScript(jsFunctionCall("SyntaxHighlighter.byId",
                getMarkupId(),
                urlFor(CLIPBOARD_SWF_REFERENCE, new PageParameters()),
                syntax.getBrush(),
                gutter, toolbar, autoLinks, wrapLines));
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public SyntaxHighlighter setSyntax(Syntax syntax) {
        this.syntax = syntax;
        return this;
    }

    public Theme getTheme() {
        return theme;
    }

    public SyntaxHighlighter setTheme(Theme theme) {
        this.theme = theme;
        return this;
    }

    public boolean isAutoLinksEnabled() {
        return autoLinks;
    }

    public SyntaxHighlighter setAutoLinksEnabled(boolean autoLinks) {
        this.autoLinks = autoLinks;
        return this;
    }

    public boolean isLineNumbersEnabled() {
        return gutter;
    }

    public SyntaxHighlighter setLineNumbersEnabled(boolean gutter) {
        this.gutter = gutter;
        return this;
    }

    public boolean isToolbarEnabled() {
        return toolbar;
    }

    public SyntaxHighlighter setToolbarEnabled(boolean toolbar) {
        this.toolbar = toolbar;
        return this;
    }

    public boolean isWrapLinesEnabled() {
        return wrapLines;
    }

    public SyntaxHighlighter setWrapLinesEnabled(boolean wrapLines) {
        this.wrapLines = wrapLines;
        return this;
    }

    private CharSequence getHtmlMarkup() {
        final String code = getDefaultModelObjectAsString();

        final StringBuilder markup = new StringBuilder();
        markup.append("<pre id=\"code-").append(getMarkupId()).append("\">");

        markup.append(code);

        markup.append("</pre>");
        return markup;
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        final CharSequence markup = getHtmlMarkup();
        replaceComponentTagBody(markupStream, openTag, markup);
    }

    public enum Theme {
        Default("shThemeDefault.css"),
        Django("shThemeDjango.css"),
        Emacs("shThemeEmacs.css"),
        FadeToGrey("shThemeFadeToGrey.css"),
        Midnight("shThemeMidnight.css"),
        RDark("shThemeRDark.css ");

        private ResourceReference cssReference;

        Theme(String cssFile) {
            final String cssPath = format("resources/styles/%s", cssFile);
            cssReference = new CssResourceReference(SyntaxHighlighter.class, cssPath);
        }

        ResourceReference getCssReference() {
            return cssReference;
        }
    }
}
