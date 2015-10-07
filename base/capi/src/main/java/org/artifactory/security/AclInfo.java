package org.artifactory.security;

import org.artifactory.common.Info;

import java.util.Set;

/**
 * Date: 8/2/11
 * Time: 10:29 AM
 *
 * @author Fred Simon
 */
public interface AclInfo extends Info {
    PermissionTargetInfo getPermissionTarget();

    Set<AceInfo> getAces();

    String getUpdatedBy();
}
