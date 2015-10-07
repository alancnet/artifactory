package org.artifactory.storage.db.aql.service;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.OssAddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.aql.model.AqlPermissionProvider;
import org.artifactory.repo.RepoPath;

/**
 * @author Gidi Shabat
 */
public class AqlPermissionProviderImpl implements AqlPermissionProvider {

    private AuthorizationService authorizationService;
    private AddonsManager addonsManager;

    public AuthorizationService getAuthorizationProvider() {
        if (authorizationService == null) {
            authorizationService = ContextHelper.get().getAuthorizationService();
        }
        return authorizationService;
    }

    public AddonsManager getAddonsManager() {
        if (addonsManager == null) {
            addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        }
        return addonsManager;
    }

    @Override
    public boolean canRead(RepoPath repoPath) {
        return getAuthorizationProvider().canRead(repoPath);
    }

    @Override
    public boolean isAdmin() {
        return getAuthorizationProvider().isAdmin();
    }

    @Override
    public boolean isOss() {
        return getAddonsManager() instanceof OssAddonsManager;
    }
}