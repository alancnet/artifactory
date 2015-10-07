package org.artifactory.schedule.mbean;

import org.artifactory.schedule.ArtifactoryConcurrentExecutor;

/**
 * MBean wrapper for {@link org.artifactory.schedule.ArtifactoryConcurrentExecutor}
 *
 * @author mamo
 */
public class ManagedExecutor implements ManagedExecutorMBean {

    private final ArtifactoryConcurrentExecutor artifactoryConcurrentExecutor;

    public ManagedExecutor(ArtifactoryConcurrentExecutor artifactoryConcurrentExecutor) {
        this.artifactoryConcurrentExecutor = artifactoryConcurrentExecutor;
    }

    @Override
    public int getActiveCount() {
        return artifactoryConcurrentExecutor.getActiveCount();
    }

    @Override
    public long getCompletedTaskCount() {
        return artifactoryConcurrentExecutor.getCompletedTaskCount();
    }

    @Override
    public int getCorePoolSize() {
        return artifactoryConcurrentExecutor.getCorePoolSize();
    }

    @Override
    public int getLargestPoolSize() {
        return artifactoryConcurrentExecutor.getLargestPoolSize();
    }

    @Override
    public int getMaximumPoolSize() {
        return artifactoryConcurrentExecutor.getMaximumPoolSize();
    }

    @Override
    public long getTaskCount() {
        return artifactoryConcurrentExecutor.getTaskCount();
    }
}
