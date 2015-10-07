package org.artifactory.storage.spring;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.schedule.TaskService;
import org.artifactory.storage.binstore.service.BinaryStore;

/**
 * Date: 8/4/11 Time: 6:01 PM
 *
 * @author Fred Simon
 */
public interface ArtifactoryStorageContext extends ArtifactoryContext {
    BinaryStore getBinaryStore();

    boolean isReady();

    TaskService getTaskService();
}
