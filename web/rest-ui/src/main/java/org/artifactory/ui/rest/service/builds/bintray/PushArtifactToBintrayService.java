package org.artifactory.ui.rest.service.builds.bintray;

import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BintrayModel;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.governance.AbstractBuildService;
import org.artifactory.ui.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PushArtifactToBintrayService extends AbstractBuildService {
    private static final Logger log = LoggerFactory.getLogger(PushArtifactToBintrayService.class);

    @Autowired
    BintrayService bintrayService;

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());
            BintrayModel bintrayModel = (BintrayModel) request.getImodel();
            String repoKey = request.getQueryParamByKey("repoKey");
            String path = request.getQueryParamByKey("path");
            RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
            ItemInfo itemInfo = repositoryService.getItemInfo(repoPath);
            BintrayParams bintrayParams = getBintrayParams(bintrayModel);
            push(headersMap, response, itemInfo, bintrayParams);
        } catch (Exception e) {
            response.error(e.getMessage());
            log.error(e.toString());
        }
    }


    /**
     * get bintray params
     *
     * @param bintrayModel - bintray model
     * @return - bintray params
     */
    private BintrayParams getBintrayParams(BintrayModel bintrayModel) {
        return bintrayModel.getBintrayParams();
    }


    /**
     * push to bintray sync
     *
     * @param headersMap   - headers map
     * @param response     - encapsulate data related to request
     * @param itemInfo        - Item info
     * @param bintrayModel - bintray model
     */
    private void push(Map<String, String> headersMap, RestResponse response, ItemInfo itemInfo, BintrayParams bintrayModel) {
        try {

            BasicStatusHolder statusHolder = bintrayService.pushArtifact(itemInfo, bintrayModel, headersMap);
            if (statusHolder.hasErrors()) {
                response.error(statusHolder.getLastError().getMessage());
            } else if (statusHolder.getWarnings().size() != 0) {
                response.error(statusHolder.getWarnings().get(0).getMessage());
            } else {
                String successMessages = "Successfully pushed '" + itemInfo.getRelPath() + "' to ";
                String versionFilesPathUrl = bintrayService.getVersionFilesUrl(bintrayModel);
                response.info(successMessages);
                response.url(versionFilesPathUrl);
            }
        } catch (IOException e) {
            response.error("Connection failed with exception: " + e.getMessage());
        }
    }
}
