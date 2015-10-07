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

package org.artifactory.common.wicket.component.confirm;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.artifactory.common.wicket.util.WicketUtils;

import static org.artifactory.common.wicket.util.JavaScriptUtils.jsFunctionCall;

/**
 * @author Yoav Aharoni
 */
public class AjaxConfirm {
    private static final AjaxConfirm INSTANCE = new AjaxConfirm();

    private AjaxConfirm() {
    }

    public static AjaxConfirm get() {
        return INSTANCE;
    }

    public void confirm(ConfirmDialog dialog) {
        final ConfirmAjaxBehavior eventBehavior = new ConfirmAjaxBehavior(dialog);
        WicketUtils.getPage().add(eventBehavior);
        final AjaxRequestTarget target = AjaxRequestTarget.get();
        target.appendJavaScript(eventBehavior.getConfirmScript());
    }

    private static class ConfirmAjaxBehavior extends AjaxEventBehavior {
        private final ConfirmDialog dialog;

        private ConfirmAjaxBehavior(ConfirmDialog dialog) {
            super("wicket:confirm");
            this.dialog = dialog;
        }

        @Override
        protected void onEvent(AjaxRequestTarget target) {
            String approvedString = WicketUtils.getParameter("confirm");
            boolean approved = StringUtils.isEmpty(approvedString) || Boolean.valueOf(approvedString);
            dialog.onConfirm(approved, target);
            getComponent().remove(this);
        }

        public String getConfirmScript() {
            return getCallbackScript().toString();
        }

        @Override
        protected CharSequence generateCallbackScript(CharSequence partialCall) {
            return "var ok=" + jsFunctionCall("confirm", dialog.getMessage()) + ";" +
                    super.generateCallbackScript(partialCall + "+'&confirm=' + ok");
        }
    }
}
