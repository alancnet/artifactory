package org.artifactory.ui.rest.service.builds.bintray;

import com.google.common.collect.Lists;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.builds.BintrayModel;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBintrayVersionsService implements RestService {

    @Autowired
    BintrayService bintrayService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());
        String repoKey = request.getQueryParamByKey("key");
        String packageId = request.getQueryParamByKey("id");
        // get packages versions
        BintrayModel bintrayModel = getPackagesVersions(response, headersMap, repoKey, packageId);
        response.iModel(bintrayModel);

    }

    /**
     * return list of version on given package id and repoKey
     *
     * @param artifactoryResponse -encapsulate data related to reposne
     * @param headersMap          - headers map
     * @param repoKey             - repo key
     * @param packageId           - packages id
     * @return - list of version
     */
    private BintrayModel getPackagesVersions(RestResponse artifactoryResponse, Map<String, String> headersMap, String repoKey, String packageId) {
        List<String> packageVersions = getPackageVersions(artifactoryResponse, repoKey, packageId, headersMap);
        BintrayModel bintrayModel = new BintrayModel();
        bintrayModel.setBinTrayVersions(packageVersions);
        return bintrayModel;
    }

    /**
     * return list of bintray packages
     *
     * @param response   - encapsulate data related re response
     * @param repoKey    - repository key
     * @param packageId  - package id
     * @param headersMap - request headers map
     * @return - list of package versions
     */
    private List<String> getPackageVersions(RestResponse response, String repoKey, String packageId,
                                            Map<String, String> headersMap) {
        List<String> packageVersions = Lists.newArrayList();
        try {
            packageVersions = bintrayService.getVersions(repoKey, packageId, headersMap);
        } catch (IOException e) {
            response.error("Connection failed with exception: " + e.getMessage());
        } catch (BintrayException e) {
            response.error("Could not retrieve versions list for Repository '" + repoKey + "' and Package '" + packageId + "'");
        }

        return packageVersions;
    }
}
