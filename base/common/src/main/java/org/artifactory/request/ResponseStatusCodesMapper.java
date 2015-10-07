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

package org.artifactory.request;

import org.apache.http.HttpStatus;
import org.artifactory.api.repo.exception.ItemNotFoundRuntimeException;
import org.artifactory.api.repo.exception.RepoRejectException;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.util.DoesNotExistException;

/**
 * Maps different exceptions to their appropriate HttpStatus code
 *
 * @author Shay Yaakov
 */
public class ResponseStatusCodesMapper {

    public int getStatusCode(Throwable e) {
        if (e instanceof BadPomException) {
            return HttpStatus.SC_CONFLICT;
        } else if (e instanceof DoesNotExistException) {
            return HttpStatus.SC_NOT_FOUND;
        } else if (e instanceof ItemNotFoundRuntimeException) {
            return HttpStatus.SC_BAD_REQUEST;
        } else if (e instanceof RepoRejectException) {
            return ((RepoRejectException) e).getErrorCode();
        } else if (e instanceof IllegalArgumentException) {
            return HttpStatus.SC_BAD_REQUEST;
        }

        return HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }
}
