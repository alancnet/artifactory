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

package org.artifactory.api.context;

import org.artifactory.log.logback.LogbackContextSelector;

/**
 * @author yoavl
 */
public abstract class ArtifactoryContextThreadBinder {

    private static final InheritableThreadLocal<ArtifactoryContext> current =
            new InheritableThreadLocal<>();

    static ArtifactoryContext getArtifactoryContext() {
        return current.get();
    }

    public static void bind(ArtifactoryContext context) {
        current.set(context);
        LogbackContextSelector.bind();
    }

    public static void unbind() {
        LogbackContextSelector.unbind();
        current.remove();
    }
}
