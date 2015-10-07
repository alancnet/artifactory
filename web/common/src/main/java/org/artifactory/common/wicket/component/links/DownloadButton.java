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

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomer Cohen
 */
public abstract class DownloadButton extends TitledLink {
    @SuppressWarnings({"UnusedDeclaration"})
    private static final Logger log = LoggerFactory.getLogger(DownloadButton.class);

    public DownloadButton(String id) {
        super(id);
    }

    public DownloadButton(String id, String title) {
        super(id, title);
    }

    public DownloadButton(String id, IModel titleModel) {
        super(id, titleModel);
    }

    {
        setStyled(true);
    }

    @Override
    protected String getOpenScript() {
        return "window.open('" + getURL() + "');";
    }

    @Override
    public void onClick() {
        IResourceStream resourceStream = getResourceStream();
        RequestCycle requestCycle = getRequestCycle();
        WebResponse response = (WebResponse) requestCycle.getResponse();
        response.setHeader("Cache-Control", "no-store");

        requestCycle.scheduleRequestHandlerAfterCurrent(
                new ResourceStreamRequestHandler(resourceStream)
                        .setFileName(getFileName())
                        .setContentDisposition(ContentDisposition.ATTACHMENT));
    }

    protected abstract String getFileName();

    protected abstract StringResourceStream getResourceStream();
}
