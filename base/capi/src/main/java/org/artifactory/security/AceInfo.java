package org.artifactory.security;

import org.artifactory.common.Info;

import java.util.Set;

/**
 * Date: 8/2/11
 * Time: 10:23 AM
 *
 * @author Fred Simon
 */
public interface AceInfo extends Info {
    String getPrincipal();

    boolean isGroup();

    int getMask();

    boolean canManage();

    boolean canDelete();

    boolean canDeploy();

    boolean canAnnotate();

    boolean canRead();

    Set<String> getPermissionsAsString();
}
