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

package org.artifactory.test.mock;

import org.apache.commons.io.IOUtils;
import org.artifactory.test.TestUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple mock server that just returns resources from the root path.
 *
 * @author Yossi Shaul
 */
public class SimpleMockServer {

    private Server server;
    private Handler handler;

    public SimpleMockServer() {
        this(new SimpleMockHandler());
    }

    public SimpleMockServer(Handler handler) {
        this.handler = handler;
    }

    public void start() {
        try {
            this.server = new Server(0);
            server.setHandler(handler);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setHandler(Handler handler) {
        server.setHandler(handler);
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        Connector connector = server.getConnectors()[0];
        return connector.getLocalPort() <= 0 ? connector.getPort() : connector.getLocalPort();
    }

    public String getBaseUrl() {
        Connector connector = server.getConnectors()[0];
        return "http://" + TestUtils.extractHost(connector.getHost()) + ":" + getPort() + "/";
    }

    private static class SimpleMockHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            InputStream resource = SimpleMockServer.class.getResourceAsStream(target);
            if (resource == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                IOUtils.copy(resource, response.getOutputStream());
            }
            baseRequest.setHandled(true);
        }
    }
}
