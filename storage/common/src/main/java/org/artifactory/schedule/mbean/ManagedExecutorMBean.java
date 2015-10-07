package org.artifactory.schedule.mbean;

/**
 * MBean wrapper for {@link org.artifactory.schedule.ArtifactoryConcurrentExecutor}
 *
 * @author mamo
 */
public interface ManagedExecutorMBean {

    int getActiveCount();

    long getCompletedTaskCount();

    int getCorePoolSize();

    int getLargestPoolSize();

    int getMaximumPoolSize();

    long getTaskCount();
}
