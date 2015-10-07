package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.build.BuildRun;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.utils.DateUtils;
import org.artifactory.util.CollectionUtils;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildLicensesService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(BuildLicensesService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            Boolean authFind = Boolean.valueOf(request.getQueryParamByKey("autoFind"));
            Build build = getBuild(name, buildNumber, buildStarted, response);
            // fetch license
            Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseModuleModel = getRepoPathLicenseModuleModelMultimap(build, authFind);
            if (repoPathLicenseModuleModel != null && !repoPathLicenseModuleModel.isEmpty()) {
                Collection<ModuleLicenseModel> values = repoPathLicenseModuleModel.values();
                // fetch published modules
                Set<ModuleLicenseModel> publishedModules = getPublishedModulesFromModelList(values, build.getModules());
                // filter published modules from licenses
                publishedModules.forEach(published -> values.remove(published));
                // fetch build license summary
                Set<String> scopes = getScopeMapping(values);
                BuildLicenseModel buildLicenseModel = new BuildLicenseModel(values, publishedModules, scopes);
                response.iModel(buildLicenseModel);
                // get scopes
            }
        } catch (ParseException e) {
            log.error(e.toString());
            response.error("error with retrieving build licenses");
            return;
        }
    }

    /**
     * get license with repo path data
     *
     * @param build - license build
     * @return multi map with repo path and license
     */
    private Multimap<RepoPath, ModuleLicenseModel> getRepoPathLicenseModuleModelMultimap(Build build, boolean autoFind) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
        Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseMultimap = licensesAddon.
                populateLicenseInfoSynchronously(build, autoFind);
        return repoPathLicenseMultimap;
    }

    /**
     * get build info
     *
     * @param buildName    - build name
     * @param buildNumber  - build number
     * @param buildStarted - build date
     * @param response     - encapsulate data related to request
     * @return
     */
    private Build getBuild(String buildName, String buildNumber, String buildStarted, RestResponse response) {
        boolean buildStartedSupplied = StringUtils.isNotBlank(buildStarted);
        try {
            Build build = null;
            if (buildStartedSupplied) {
                BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
                if (buildRun != null) {
                    build = buildService.getBuild(buildRun);
                }
            } else {
                //Take the latest build of the specified number
                build = buildService.getLatestBuildByNameAndNumber(buildName, buildNumber);
            }
            if (build == null) {
                StringBuilder builder = new StringBuilder().append("Could not find build '").append(buildName).
                        append("' #").append(buildNumber);
                if (buildStartedSupplied) {
                    builder.append(" that started at ").append(buildStarted);
                }
                throwNotFoundError(response, builder.toString());
            }
            return build;
        } catch (RepositoryRuntimeException e) {
            String errorMessage = new StringBuilder().append("Error locating latest build for '").append(buildName).
                    append("' #").append(buildNumber).append(": ").append(e.getMessage()).toString();
            throwInternalError(errorMessage, response);
        }
        //Should not happen
        return null;
    }

    /**
     * Throws a 404 AbortWithHttpErrorCodeException with the given message
     *
     * @param errorMessage Message to display in the error
     */
    private void throwNotFoundError(RestResponse response, String errorMessage) {
        log.error(errorMessage);
        response.error(errorMessage);
    }

    /**
     * return not found error
     *
     * @param errorMessage
     * @param response
     */
    private void throwInternalError(String errorMessage, RestResponse response) {
        response.error(errorMessage);
        response.responseCode(HttpServletResponse.SC_NOT_FOUND);
    }

    public static Set<String> getScopeMapping(Collection<ModuleLicenseModel> models) {
        Set<String> scopeSet = new HashSet();
        for (ModuleLicenseModel model : models) {
            scopeSet.addAll(model.getScopes().stream().collect(Collectors.toList()));
        }
        return scopeSet;
    }

    /**
     * Returns all models that relate to a dependency which is also a published module
     *
     * @param models  models to filter
     * @param modules build modules to filter by
     */
    public static Set<ModuleLicenseModel> getPublishedModulesFromModelList(Collection<ModuleLicenseModel> models,
                                                                           final Collection<Module> modules) {
        if (CollectionUtils.isNullOrEmpty(models) || CollectionUtils.isNullOrEmpty(modules)) {
            return Sets.newHashSet();
        }
        return Sets.newHashSet(Iterables.filter(models, new PublishedModuleFilterPredicate(modules)));
    }


    private static class PublishedModuleFilterPredicate implements Predicate<ModuleLicenseModel> {
        private Set<Artifact> moduleArtifacts = Sets.newHashSet();

        private PublishedModuleFilterPredicate(Collection<Module> modules) {
            for (Module module : modules) {
                if (CollectionUtils.notNullOrEmpty(module.getArtifacts())) {
                    moduleArtifacts.addAll(module.getArtifacts());
                }
            }
        }

        @Override
        public boolean apply(@Nonnull ModuleLicenseModel input) {
            // filter published artifacts based on the checksum
            for (Artifact artifact : moduleArtifacts) {
                if (StringUtils.isNotBlank(artifact.getSha1()) && artifact.getSha1().equals(input.getSha1())) {
                    return true;
                } else if (StringUtils.isNotBlank(artifact.getMd5()) && artifact.getMd5().equals(input.getMd5())) {
                    return true;
                }
            }
            return false;
        }
    }
}
