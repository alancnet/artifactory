package org.artifactory.storage.spring;

import org.artifactory.api.context.ContextHelper;

/**
 * Date: 8/4/11
 * Time: 6:05 PM
 *
 * @author Fred Simon
 */
public abstract class StorageContextHelper {
    private StorageContextHelper() {
    }

    public static ArtifactoryStorageContext get() {
        return (ArtifactoryStorageContext) ContextHelper.get();
    }
}
