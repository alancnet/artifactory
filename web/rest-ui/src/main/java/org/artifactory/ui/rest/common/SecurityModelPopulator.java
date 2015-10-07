package org.artifactory.ui.rest.common;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.artifactory.descriptor.security.sso.CrowdSettings;
import org.artifactory.descriptor.security.sso.SamlSettings;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.sapi.security.SecurityConstants;
import org.artifactory.security.AceInfo;
import org.artifactory.security.AclInfo;
import org.artifactory.security.ArtifactoryPermission;
import org.artifactory.security.GroupInfo;
import org.artifactory.security.PermissionTargetConfigurationImpl;
import org.artifactory.security.PermissionTargetInfo;
import org.artifactory.security.PrincipalConfigurationImpl;
import org.artifactory.security.UserGroupInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.ui.rest.model.admin.security.crowdsso.CrowdIntegration;
import org.artifactory.ui.rest.model.admin.security.group.Group;
import org.artifactory.ui.rest.model.admin.security.saml.Saml;
import org.artifactory.ui.rest.model.admin.security.user.BaseUser;
import org.artifactory.ui.rest.model.empty.EmptyModel;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public class SecurityModelPopulator {

        @Nonnull
        public static BaseUser getUserConfiguration(@Nonnull UserInfo user,DateTimeFormatter dateFormatter) {
            BaseUser userConfiguration = new BaseUser();
            userConfiguration.setInternalPasswordDisabled(!user.isAdmin() && user.hasInvalidPassword());
            long lastLoginTimeMillis = user.getLastLoginTimeMillis();
            if (lastLoginTimeMillis > 0) {
                userConfiguration.setLastLoggedIn(dateFormatter.print(lastLoginTimeMillis));
            }
            userConfiguration.setRealm(user.getRealm());
            userConfiguration.setAdmin(user.isAdmin());
            userConfiguration.setEmail(user.getEmail());
            userConfiguration.setName(user.getUsername());
            userConfiguration.setProfileUpdatable(user.isUpdatableProfile());
            if (!("internal".equals(user.getRealm()) || "system".equals(
                    user.getRealm()) || user.getRealm() == null || user.getRealm().isEmpty() || user.isAnonymous())) {
                userConfiguration.setExternalRealmLink("Check external status");
            }
            Set<UserGroupInfo> groups = user.getGroups();
            if ((groups != null) && !groups.isEmpty()) {
                userConfiguration.setGroups(Sets.newHashSet(Iterables.transform(groups,
                                new Function<UserGroupInfo, String>() {
                                    @Override
                                    public String apply(@Nullable UserGroupInfo input) {
                                        if (input == null) {
                                            return null;
                                        }
                                        return input.getGroupName();
                                    }
                                })
                ));
            }
            return userConfiguration;
        }

        @Nonnull
        public static Group getGroupConfiguration(@Nonnull GroupInfo group) {
            Group groupConfiguration = new Group();
            groupConfiguration.setDescription(group.getDescription());
            groupConfiguration.setAutoJoin(group.isNewUserDefault());
            groupConfiguration.setName(group.getGroupName());
            groupConfiguration.setRealm(group.getRealm());
            groupConfiguration.setRealmAttributes(group.getRealmAttributes());
            groupConfiguration.setExternal(group.getRealm() != null && !SecurityConstants.DEFAULT_REALM.equals(group.getRealm()));
            return groupConfiguration;
        }

        @Nonnull
        public static PermissionTargetConfigurationImpl getPermissionTargetConfiguration(@Nonnull AclInfo acl) {
            PermissionTargetConfigurationImpl permissionTargetConfiguration = new PermissionTargetConfigurationImpl();
            PermissionTargetInfo permissionTarget = acl.getPermissionTarget();
            permissionTargetConfiguration.setName(permissionTarget.getName());
            permissionTargetConfiguration.setIncludesPattern(permissionTarget.getIncludesPattern());
            permissionTargetConfiguration.setExcludesPattern(permissionTarget.getExcludesPattern());
            permissionTargetConfiguration.setRepositories(permissionTarget.getRepoKeys());

            Set<AceInfo> aces = acl.getAces();
            Map<String, Set<String>> users = Maps.newHashMap();
            Map<String, Set<String>> groups = Maps.newHashMap();

            for (AceInfo ace : aces) {
                String principal = ace.getPrincipal();
                Set<String> permissionsAsString = ace.getPermissionsAsString();
                if (ace.isGroup()) {
                    groups.put(principal, permissionsAsString);
                } else {
                    users.put(principal, permissionsAsString);
                }
            }

            PrincipalConfigurationImpl principalConfiguration = new PrincipalConfigurationImpl();
            if (!users.isEmpty()) {
                principalConfiguration.setUsers(users);
            }
            if (!groups.isEmpty()) {
                principalConfiguration.setGroups(groups);
            }
            permissionTargetConfiguration.setPrincipals(principalConfiguration);
            return permissionTargetConfiguration;
        }

        public static Set<String> getPermissionsAsString(boolean canRead, boolean canAnnotate, boolean canDeploy,
                boolean canDelete, boolean canAdmin) {

            Set<String> permissionsAsString = Sets.newHashSet();
            if (canRead) {
                appendPermissionString(permissionsAsString, ArtifactoryPermission.READ);
            }
            if (canAnnotate) {
                appendPermissionString(permissionsAsString, ArtifactoryPermission.ANNOTATE);
            }
            if (canDeploy) {
                appendPermissionString(permissionsAsString, ArtifactoryPermission.DEPLOY);
            }
            if (canDelete) {
                appendPermissionString(permissionsAsString, ArtifactoryPermission.DELETE);
            }
            if (canAdmin) {
                appendPermissionString(permissionsAsString, ArtifactoryPermission.MANAGE);
            }
            return permissionsAsString;
        }

        private static void appendPermissionString(Set<String> permissionsAsString, ArtifactoryPermission permission) {
            permissionsAsString.add(permission.getString());
        }

    /**
     * populate samlSettings descriptor data to Saml model
     *
     * @param samlSettings - blackDuck  descriptor
     * @return licenseInfo model
     */
    public static RestModel populateSamlInfo(SamlSettings samlSettings) {
        if (samlSettings != null) {
            return new Saml(samlSettings);
        }
        return new EmptyModel();
    }

    @Nonnull
    public static CrowdIntegration getCrowdConfiguration(@Nonnull CrowdSettings crowdSettings) {
        CrowdIntegration crowdIntegration = new CrowdIntegration(crowdSettings);
        return crowdIntegration;
    }
}
