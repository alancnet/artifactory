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

package org.artifactory.repo.webdav.methods;

import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.repo.webdav.WebdavMethod;
import org.artifactory.repo.webdav.WebdavStatus;
import org.artifactory.request.ArtifactoryRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Yoav Luft
 */
@Component
public class UnlockMethod implements WebdavMethod {
    @Override
    public boolean canHandle(String method) {
        return getName().equalsIgnoreCase(method);
    }

    @Override
    public void handle(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        response.setStatus(WebdavStatus.SC_NO_CONTENT);
    }

    @Override
    public String getName() {
        return "unlock";
    }
}
