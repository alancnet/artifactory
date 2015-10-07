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

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.spring.InternalArtifactoryContext;
import org.easymock.EasyMock;

/**
 * @author yoavl
 */
public class MockUtils {

    /**
     * Remember to call EasyMock.replay() on the context!
     */
    public static InternalArtifactoryContext getThreadBoundedMockContext() {
        InternalArtifactoryContext context = EasyMock.createMock(InternalArtifactoryContext.class);
        EasyMock.expect(context.isReady()).andReturn(true).anyTimes();
        EasyMock.expect(context.getClassLoader()).andReturn(
                Thread.currentThread().getContextClassLoader()).anyTimes();
        ArtifactoryContextThreadBinder.bind(context);
        return context;
    }
}
