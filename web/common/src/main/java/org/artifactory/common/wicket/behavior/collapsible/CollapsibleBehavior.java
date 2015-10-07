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

package org.artifactory.common.wicket.behavior.collapsible;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.request.cycle.RequestCycle;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.template.TemplateBehavior;
import org.artifactory.common.wicket.behavior.template.loadingstrategy.InterpolatedTemplateStrategy;
import org.artifactory.common.wicket.util.CookieUtils;

/**
 * @author Yoav Aharoni
 */
public class CollapsibleBehavior extends TemplateBehavior {
    private boolean expanded;
    private boolean resizeModal;
    private CollapsibleBehavior.ExpandAjaxBehavior expandEvent;
    private String persistenceCookie;

    public CollapsibleBehavior() {
        super(CollapsibleBehavior.class);
        getResourcePackage().addJavaScript();
    }

    @Override
    public void bind(Component component) {
        super.bind(component);
        if (expandEvent != null) {
            component.add(expandEvent);
        }
    }

    protected void onChange(final AjaxRequestTarget target) {
    }

    public boolean isExpanded() {
        return expanded;
    }

    public CollapsibleBehavior setExpanded(boolean expanded) {
        this.expanded = expanded;
        return this;
    }

    public CollapsibleBehavior setResizeModal(boolean resizeModal) {
        this.resizeModal = resizeModal;
        return this;
    }

    @WicketProperty
    public boolean isResizeModal() {
        return resizeModal;
    }

    @WicketProperty
    public String getCssClass() {
        return expanded ? "expanded" : "collapsed";
    }

    public CollapsibleBehavior setUseAjax(boolean useAjax) {
        setTemplateStrategy(new InterpolatedTemplateStrategy());
        expandEvent = useAjax ? new ExpandAjaxBehavior() : null;
        return this;
    }

    public String getAjaxCallback() {
        if (expandEvent != null) {
            return expandEvent.getCallFunction();
        }
        return "null";
    }

    public CollapsibleBehavior setPersistenceCookie(String cookieName) {
        this.persistenceCookie = cookieName;
        return this;
    }

    public String getPersistenceCookie() {
        return persistenceCookie;
    }

    @Override
    public void beforeRender(Component component) {
        restoreState();
        super.beforeRender(component);
    }

    private void restoreState() {
        if (persistenceCookie != null) {
            String value = CookieUtils.getCookie(persistenceCookie);
            expanded = StringUtils.isNotEmpty(value);
        }
    }

    private class ExpandAjaxBehavior extends AjaxEventBehavior {
        private ExpandAjaxBehavior() {
            super("wicket:expand");
        }

        @Override
        protected void onEvent(AjaxRequestTarget target) {
            String expandedString = RequestCycle.get().getRequest().getRequestParameters().getParameterValue(
                    "expanded").toString();
            boolean expanded = StringUtils.isEmpty(expandedString) || Boolean.valueOf(expandedString);
            setExpanded(expanded);
            onChange(target);
        }

        public String getCallFunction() {
            return String.format("function(expanded) {%s}", getCallbackScript());
        }

        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator() {
            return new NoAjaxIndicatorDecorator();
        }

        @Override
        protected CharSequence generateCallbackScript(CharSequence partialCall) {
            return super.generateCallbackScript(partialCall + "+'&expanded=' + expanded");
        }
    }
}
