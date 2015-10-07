package org.artifactory.ui.rest.service.builds.bintray;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.Repo;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
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
public class GetBintrayArtifactService implements RestService {

    @Autowired
    BintrayService bintrayService;

    @Autowired
    RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());

        String path = request.getQueryParamByKey("path");
        String repoKey = request.getQueryParamByKey("repoKey");
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        BintrayModel bintrayModel = initBintrayParam(path, repoPath);
        // get bintray repositories
        bintrayModel.setBinTrayRepositories(getBintrayRepositories(response, headersMap));
        // update response with model
        response.iModel(bintrayModel);
    }

    /**
     * init bintray param
     *
     * @param path     - artifact path
     * @param repoPath - repo path
     * @return bintray model
     */
    private BintrayModel initBintrayParam(String path, RepoPath repoPath) {
        BintrayParams bintrayParam = bintrayService.createParamsFromProperties(repoPath);

        if (StringUtils.isBlank(bintrayParam.getPath())) {
            bintrayParam.setPath(path);
        }

        if (StringUtils.isBlank(bintrayParam.getPackageId())) {
            ModuleInfo moduleInfo = repoService.getItemModuleInfo(repoPath);
            if (moduleInfo.isValid()) {
                bintrayParam.setPackageId(moduleInfo.getPrettyModuleId());
                bintrayParam.setVersion(moduleInfo.getBaseRevision());
            }
        }
        BintrayModel bintrayModel = new BintrayModel();
        bintrayModel.setBintrayParams(bintrayParam);
        return bintrayModel;
    }

    /**
     * get BintrayRepositoriesModel
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param headersMap          - request headers map
     * @return - list of repositories
     */
    private List<String> getBintrayRepositories(RestResponse artifactoryResponse, Map<String, String> headersMap) {
        return getBintrayRepos(artifactoryResponse, headersMap);
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
