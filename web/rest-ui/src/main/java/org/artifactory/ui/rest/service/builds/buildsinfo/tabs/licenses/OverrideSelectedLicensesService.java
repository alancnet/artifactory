package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.build.BuildRun;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.ui.rest.model.builds.BuildLicenseModel;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OverrideSelectedLicensesService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(OverrideSelectedLicensesService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            BuildLicenseModel buildLicenseModel = (BuildLicenseModel) request.getImodel();
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            // get license-repo map
            Build build = getBuild(name, buildNumber, buildStarted, response);
            Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseMultimap = getRepoPathLicenseModuleModelMultimap(build);
            // update licenses
            updateLicenses(buildLicenseModel.getLicenses(), repoPathLicenseMultimap, response);
        } catch (ParseException e) {
            response.error("error updating licenses");
            log.error(e.toString());
        }
    }

    /**
     * Updates all licenses that had corresponding columns' 'override' checkbox checked.
     * Takes into account other columns relating to the same path so that other existing licenses properties (which were
     * not marked with override) are also saved on the path.
     */
    private void updateLicenses(Collection<ModuleLicenseModel> viewableModels, Multimap<RepoPath,
            ModuleLicenseModel> repoPathLicenseMultimap, RestResponse response) {

        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
        List<ModuleLicenseModel> moduleLicenseModels = new ArrayList<>();
        // fetch license to override
        fetchLicenseToOverride(viewableModels, repoPathLicenseMultimap, moduleLicenseModels);

        Multimap<RepoPath, LicenseInfo> licensesToWrite = HashMultimap.create();
        //Holds all licenses that will be overridden per path - to help filter what licenses to persist on that path
        Multimap<RepoPath, LicenseInfo> overriddenLicenses = HashMultimap.create();
        for (ModuleLicenseModel checkedLicenseModel : moduleLicenseModels) {
            RepoPath repoPath = InternalRepoPathFactory.create(checkedLicenseModel.getRepoKey(),checkedLicenseModel.getPath());
            checkedLicenseModel.setRepoPath(repoPath);
            RepoPath path = checkedLicenseModel.getRepoPath();
            licensesToWrite.put(path, checkedLicenseModel.getExtractedLicense());
            overriddenLicenses.put(path, checkedLicenseModel.getLicense());
        }

        //For each of the licenses that on this path check if it's already in the overridden map - if not, preserve it
        for (RepoPath path : licensesToWrite.keySet()) {
            //License already set on path differs from the license being overridden - preserve it.
            for (ModuleLicenseModel licenseExistingOnPath : repoPathLicenseMultimap.get(path)) {
            //License already set on path differs from the license being overridden - preserve it.
                if (!overriddenLicenses.get(path).contains(licenseExistingOnPath.getLicense())) {
                    licensesToWrite.put(path, licenseExistingOnPath.getLicense());
                }
            }
        }

        //Write all chosen licenses on each path
        boolean hadErrors = false;
        for (RepoPath path : licensesToWrite.keySet()) {
            if (!licensesAddon.setLicensePropsOnPath(path, Sets.newHashSet(licensesToWrite.get(path)))) {
                hadErrors = true;
            }
        }
        if (hadErrors) {
            response.error("Failed to set properties on some artifacts, check the log for more info");
        }
    }

    private void fetchLicenseToOverride(Collection<ModuleLicenseModel> viewableModels, Multimap<RepoPath,
            ModuleLicenseModel> repoPathLicenseMultimap, List<ModuleLicenseModel> moduleLicenseModels) {
        for (ModuleLicenseModel license : viewableModels) {
            RepoPath repoPath = InternalRepoPathFactory.create(license.getRepoKey(), license.getPath());
            Collection<ModuleLicenseModel> licenses = repoPathLicenseMultimap.get(repoPath);
            for (ModuleLicenseModel subLicense : licenses) {
                if (subLicense.getLicense().getName().equals(license.getLicense().getName())) {
                    subLicense.setExtractedLicense(license.getExtractedLicense());
                    moduleLicenseModels.add(subLicense);
                }
            }
        }
    }

    /**
     * get license with repo path data
     *
     * @param build - license build
     * @return multi map with repo path and license
     */
    private Multimap<RepoPath, ModuleLicenseModel> getRepoPathLicenseModuleModelMultimap(Build build) {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        LicensesAddon licensesAddon = addonsManager.addonByType(LicensesAddon.class);
        Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseMultimap = licensesAddon.
                populateLicenseInfoSynchronously(build, false);
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
}
