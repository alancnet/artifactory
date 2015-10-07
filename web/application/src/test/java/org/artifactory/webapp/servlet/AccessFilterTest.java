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

package org.artifactory.webapp.servlet;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.webapp.servlet.authentication.ArtifactoryBasicAuthenticationEntryPoint;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static org.easymock.EasyMock.*;

/**
 * @author Yoav Luft
 */
@Test
public class AccessFilterTest {

    public void testAuthenticationChallenge() throws IOException, ServletException {
        ArtifactoryHome.bind(new ArtifactoryHome(new File("./target/test/testAuthenticationChallenge")));
        ArtifactoryHome.get().getArtifactoryProperties().setProperty(
                ConstantValues.locksTimeoutSecs.getPropertyName(), "10");
        ArtifactoryBasicAuthenticationEntryPoint authenticationEntryPoint =
                new ArtifactoryBasicAuthenticationEntryPoint();
        HttpServletRequest request = createMock(HttpServletRequest.class);

        HttpServletResponse response = createMock(HttpServletResponse.class);
        response.addHeader("WWW-Authenticate", "Basic realm=\"Artifactory Realm\"");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(401);
        PrintWriter printWriter = createMock(PrintWriter.class);
        printWriter.write(anyObject(String.class));
        expect(response.getWriter()).andReturn(printWriter);
        expect(request.getRequestURI()).andReturn("testuri");
        expect(request.getHeader("Request-Agent")).andStubReturn("xx");
        replay(request, response, printWriter);
        authenticationEntryPoint.commence(request, response,
                new InsufficientAuthenticationException("Authentication required"));
        verify(response);
    }
}
