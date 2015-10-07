package org.artifactory.addon.nuget;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.ConstantValues;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.util.PathUtils;

import java.util.List;

/**
 * Shared authentication util for the NuGet Resource and the NuGet anonymous auth interceptor.
 *
 * @author Dan Feldman
 */
public class NuGetAuthUtil {

    public static boolean repoAllowsAnonymousRootGet(String repoKey) {
        boolean allow = false;
        String allowRootGet = ConstantValues.nuGetAllowRootGetWithAnon.getString();
        if ("true".equalsIgnoreCase(allowRootGet) || "all".equalsIgnoreCase(allowRootGet)) {
            allow = true;
        } else {
            List<String> repoKeys = PathUtils.delimitedListToStringList(allowRootGet, ",");
            for (String key : repoKeys) {
                if (key.contains("*")) {
                    if (repoKey.matches(key)) {
                        allow = true;
                    }
                } else if (key.equals(repoKey)) {
                    allow = true;
                }
            }
        }
        return allow && !forceAuthIsNeededForRepo(repoKey);
    }

    public static boolean forceAuthIsNeededForRepo(String repoKey) {
        //Check for nuget force auth on repo descriptor
        boolean forceAuth = false;
        //Need both due to RTFACT-4891
        RepositoryService repoService = ContextHelper.get().beanForType(RepositoryService.class);
        RepoDescriptor descriptor = repoService.repoDescriptorByKey(repoKey);
        RepoDescriptor virtual = repoService.virtualRepoDescriptorByKey(repoKey);
        if (descriptor != null) {
            forceAuth = descriptor.isForceNugetAuthentication();
        } else if (virtual != null) {
            forceAuth = virtual.isForceNugetAuthentication();
        }

        //Fallback to force auth system property if not set on descriptor
        if (!forceAuth && ConstantValues.nuGetRequireAuthentication.isSet()) {
            String requireAuth = ConstantValues.nuGetRequireAuthentication.getString();
            if ("true".equalsIgnoreCase(requireAuth) || "all".equalsIgnoreCase(requireAuth)) {
                forceAuth = true;
            } else {
                List<String> repoKeys = PathUtils.delimitedListToStringList(requireAuth, ",");
                for (String key : repoKeys) {
                    if (key.contains("*")) {
                        if (repoKey.matches(key)) {
                            forceAuth = true;
                            break;
                        }
                    } else if (key.equals(repoKey)) {
                        forceAuth = true;
                        break;
                    }
                }
            }
        }
        return forceAuth;
    }
}
