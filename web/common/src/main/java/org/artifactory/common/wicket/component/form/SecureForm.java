/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.common.wicket.component.form;

import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.artifactory.security.AccessLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

/**
 * Form that uses a random UUID token in order to prevent CSRF attacks.
 * <p><b>NOTE!</b> that you do not need to alter your HTML files, the hidden field is rendered for you!
 *
 * @author Shay Yaakov
 */
public class SecureForm<T> extends Form<T> {
    private static final Logger log = LoggerFactory.getLogger(SecureForm.class);

    private static final String TOKEN_NAME = "SECURE_FORM_TOKEN";

    public SecureForm(final String id) {
        super(id);
    }

    public SecureForm(final String id, final IModel<T> model) {
        super(id, model);
    }

    @Override
    public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        // render the hidden field
        if (isRootForm()) {
            AppendingStringBuffer buffer = new AppendingStringBuffer(
                    "<div style=\"display:none\"><input type=\"hidden\" name=\"");
            buffer.append(TOKEN_NAME)
                    .append("\" id=\"")
                    .append(TOKEN_NAME)
                    .append("\" value=\"")
                    .append(getToken())
                    .append("\" /></div>");
            getResponse().write(buffer);
        }

        // do the rest of the processing
        super.onComponentTagBody(markupStream, openTag);
    }

    @Override
    protected void onValidate() {
        // Check the random id in the hidden field. This guards against CSRF attacks.
        StringValue requestToken = getRequest().getPostParameters().getParameterValue(TOKEN_NAME);
        if (!requestToken.equals(StringValue.valueOf(getToken()))) {
            String message = "Attempted unauthorized form submission";
            log.warn(message);
            AccessLogger.unauthorizedFormSubmit(message);
            throw new UnauthorizedActionException(this, new Action("submit without CSRF token"));
        }

        super.onValidate();
    }

    public Object getToken() {
        Serializable token = getSession().getAttribute(TOKEN_NAME);
        if (token == null) {
            //Generate a unique ID for CSRF form submissions
            token = UUID.randomUUID().toString();
            getSession().setAttribute(TOKEN_NAME, token);
        }

        return token;
    }
}