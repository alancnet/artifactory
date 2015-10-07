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

package org.artifactory.webapp.wicket.page.home.settings.modal.download;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * Ajax download redirecting behavior
 *
 * @author Noam Y. Tenne
 */
public abstract class AjaxSettingsDownloadBehavior extends AbstractAjaxBehavior {
    private final String fileName;

    public AjaxSettingsDownloadBehavior(String fileName) {
        this.fileName = fileName;
    }

    public void initiate(AjaxRequestTarget target) {
        CharSequence url = getCallbackUrl();

        target.appendJavaScript("window.location.href='" + url + "'");
    }

    @Override
    public void onRequest() {
        RequestCycle.get().scheduleRequestHandlerAfterCurrent(
                new ResourceStreamRequestHandler(getResourceStream())
                        .setFileName(fileName)
                        .setContentDisposition(ContentDisposition.ATTACHMENT));
    }

    protected abstract StringResourceStream getResourceStream();
}
