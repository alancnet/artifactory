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

package org.artifactory.repo.webdav;

import com.google.common.collect.Sets;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.webdav.WebdavService;
import org.artifactory.request.ArtifactoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Service class to handle webdav protocol.<p/> Webdav RFCc at: <a href="http://www.ietf.org/rfc/rfc2518.txt">rfc2518</a>,
 * <a href="http://www.ietf.org/rfc/rfc4918.txt">rfc4918</a>.
 *
 * @author Yossi Shaul
 */
@Service
public class WebdavServiceImpl implements WebdavService {

    /**
     * Default depth is infinity. And it is limited no purpose to 3 level deep.
     */
    public static final int INFINITY = 3;

    /**
     * Default namespace.
     */
    public static final String DEFAULT_NAMESPACE = "DAV:";
    public static final String APACHE_NAMESPACE = "http://apache.org/dav/props/";
    public static final String DEFAULT_NS_ABBRV = "D";
    public static final String PROP_NS_ABBRV1 = "lp1";
    public static final String PROP_NS_ABBRV2 = "lp2";

    @Autowired
    private Collection<WebdavMethod> handlers;

    @Override
    public boolean handleRequest(String methodName, ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        for (WebdavMethod method : handlers) {
            if (method.canHandle(methodName)) {
                method.handle(request, response);
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> supportedMethods() {
        HashSet<String> names = Sets.newHashSet();
        for (WebdavMethod method : handlers) {
            names.add(method.getName());
        }
        return names;
    }
}
