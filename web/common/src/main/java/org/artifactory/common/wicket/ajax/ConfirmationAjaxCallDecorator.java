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

package org.artifactory.common.wicket.ajax;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxPreprocessingCallDecorator;

import static org.artifactory.common.wicket.util.JavaScriptUtils.jsFunctionCall;

/**
 * @author Yoav Aharoni
 */
public class ConfirmationAjaxCallDecorator extends AjaxPreprocessingCallDecorator {
    private String message;

    /**
     * Create the confirmation callback.
     *
     * @param delegate A call delegate (might be null)
     * @param message  The cpnfirmation message to display
     */
    public ConfirmationAjaxCallDecorator(IAjaxCallDecorator delegate, String message) {
        super(delegate);
        this.message = message;
    }

    public ConfirmationAjaxCallDecorator(String message) {
        this(null, message);
    }

    @Override
    public CharSequence decorateScript(Component c, CharSequence script) {
        if (StringUtils.isEmpty(message)) {
            return script;
        }
        return "if (!" + jsFunctionCall("confirm", message) + ") return false;" +
                script;

    }
}
