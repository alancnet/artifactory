package org.artifactory.sapi.common;

import java.util.concurrent.Callable;

/**
 * Date: 8/5/11
 * Time: 4:31 PM
 *
 * @author Fred Simon
 */
public interface ArtifactorySession {
    void save();

    void logout();

    void addLogoutListener(Callable callable);
}
