package org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang.StringUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.license.LicensesAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.license.LicenseInfo;
import org.artifactory.api.license.ModuleLicenseModel;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.build.BuildRun;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.ui.rest.model.utils.predefinevalues.PreDefineValues;
import org.artifactory.ui.utils.DateUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ChangeBuildLicenseService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ChangeBuildLicenseService.class);

    @Autowired
    BuildService buildService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String id = request.getQueryParamByKey("id");
            String repoKey = request.getQueryParamByKey("repoKey");
            String path = request.getQueryParamByKey("path");
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            // get license-repo map
            Build build = getBuild(name, buildNumber, buildStarted, response);
            RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
            Multimap<RepoPath, ModuleLicenseModel> repoPathLicenseMultimap = getRepoPathLicenseModuleModelMultimap(build);
            Map<String, LicenseInfo> currentValues = getCurrentValues(id, repoPath, repoPathLicenseMultimap);
            PreDefineValues preDefineValues = getLicenseValues(repoPath, currentValues);
            response.iModel(preDefineValues);
        } catch (ParseException e) {
            log.error(e.toString());
        }

    }

    /**
     * get license preDefine values
     *
     * @param repoPath - repo path
     * @return - pre define values
     */
    private PreDefineValues getLicenseValues(RepoPath repoPath, Map<String, LicenseInfo> currentValues) {
        PreDefineValues values = new PreDefineValues();
        String name = "artifactory.licenses";
        Map<String, Property> propertyItemMap = createPropertyItemMap(repoPath);
        if (!propertyItemMap.isEmpty()) {
            List<PredefinedValue> predefinedValues = propertyItemMap.get(name).getPredefinedValues();
            List<String> listOfPredefineValuesAsString = new ArrayList<>();
            List<String> selectedValues = new ArrayList<>();
            predefinedValues.forEach(predefinedValue -> {
                if (predefinedValue.isDefaultValue() || currentValues.get(predefinedValue.getValue()) != null) {
                    selectedValues.add(predefinedValue.getValue());
                } else {
                    listOfPredefineValuesAsString.add(predefinedValue.getValue());
                }
            });
            values.setSelectedValues(selectedValues);
            values.setPredefinedValues(listOfPredefineValuesAsString);
        }
        return values;
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
     * Get all licenses that are currently on the models for a specific id and repo path
     *
     * @param id       The id of the model
     * @param repoPath The repo path of the model
     * @return The current values (licenses) for a specific id and repo path.
     */
    private Map<String, LicenseInfo> getCurrentValues(String id, RepoPath repoPath, Multimap<RepoPath, ModuleLicenseModel> LicenseMap) {
        List<LicenseInfo> licenseInfos = Lists.newArrayList();
        Map<String, LicenseInfo> licenseMap = new HashMap<>();
        Iterable<ModuleLicenseModel> modelsWithSameId =
                Iterables.filter(LicenseMap.get(repoPath), new SameIdPredicate(id));
        for (ModuleLicenseModel moduleLicenseModel : modelsWithSameId) {
            LicenseInfo licenseInfo = moduleLicenseModel.getLicense();
            if (licenseInfo.isValidLicense()) {
                licenseInfos.add(licenseInfo);
                licenseMap.put(licenseInfo.getName(), licenseInfo);
            }
        }
        return licenseMap;
    }

    private static class SameIdPredicate implements Predicate<ModuleLicenseModel> {

        private String id;

        private SameIdPredicate(String id) {
            this.id = id;
        }

        @Override
        public boolean apply(@Nonnull ModuleLicenseModel input) {
            return input.getId().equals(id);
        }
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

    /**
     * create property map by repo path
     *
     * @param repoPath - repo path
     * @return map of properties
     */
    private Map<String, Property> createPropertyItemMap(RepoPath repoPath) {
        Map<String, Property> propertyItemMap = new HashMap<>();
        LocalRepoDescriptor descriptor = repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey());
        List<PropertySet> propertySets = new ArrayList<>(descriptor.getPropertySets());
        for (PropertySet propertySet : propertySets) {
            List<Property> propertyList = propertySet.getProperties();
            for (Property property : propertyList) {
                propertyItemMap.put(propertySet.getName() + "." + property.getName(), property);
            }
        }
        return propertyItemMap;
    }
}
