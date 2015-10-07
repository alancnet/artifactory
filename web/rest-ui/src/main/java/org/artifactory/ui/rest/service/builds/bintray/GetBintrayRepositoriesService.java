package org.artifactory.ui.rest.service.builds.bintray;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.Repo;
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
public class GetBintrayRepositoriesService implements RestService {

    @Autowired
    BintrayService bintrayService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());
        /// get bintray repositories
        BintrayModel bintrayModel = getBintrayRepositories(response, headersMap);
        // update response with model
        response.iModel(bintrayModel);
    }

    /**
     * get BintrayRepositoriesModel
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param headersMap          - request headers map
     * @return - list of repositories
     */
    private BintrayModel getBintrayRepositories(RestResponse artifactoryResponse, Map<String, String> headersMap) {
        List<String> bintrayRepos = getBintrayRepos(artifactoryResponse, headersMap);
        BintrayModel bintrayModel = new BintrayModel();
        bintrayModel.setBinTrayRepositories(bintrayRepos);
        return bintrayModel;
    }


    /**
     * get Bintray repositories
     *
     * @param response - encapsulate data related to response
     * @return - list of repositories
     */
    private List<String> getBintrayRepos(RestResponse response, Map<String, String> headersMap) {
        List<String> repos = Lists.newArrayList();
        try {
            List<Repo> reposToDeploy = bintrayService.getReposToDeploy(headersMap);
            repos = Lists.newArrayList(Iterables.transform(reposToDeploy, new Function<Repo, String>() {
                @Override
                public String apply(Repo repo) {
                    return repo.getOwner() + "/" + repo.getName();
                }
            }));
        } catch (IOException e) {
            response.error("Connection failed with exception: " + e.getMessage());

        } catch (BintrayException e) {
            response.error("Could not retrieve repositories list from Bintray.");
        } catch (Exception e) {
            response.error(e.getMessage().toString());
        }
        return repos;
    }
}
