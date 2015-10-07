package org.artifactory.ui.rest.service.builds.bintray;

import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.common.StatusEntry;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.rest.model.builds.BintrayModel;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.governance.AbstractBuildService;
import org.artifactory.ui.utils.DateUtils;
import org.artifactory.ui.utils.RequestUtils;
import org.jfrog.build.api.Build;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PushBuildToBintrayService extends AbstractBuildService {
    private static final Logger log = LoggerFactory.getLogger(PushBuildToBintrayService.class);

    @Autowired
    BintrayService bintrayService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            Map<String, String> headersMap = RequestUtils.getHeadersMap(request.getServletRequest());
            String name = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            BintrayModel bintrayModel = (BintrayModel) request.getImodel();
            boolean background = Boolean.valueOf(request.getQueryParamByKey("background"));
            BintrayParams bintrayParams = getBintrayParams(bintrayModel);
            Build build = getBuild(name, buildNumber, buildStarted, response);
            if (background) {
                // push async
                backgroundPush(headersMap, bintrayParams, build);
            } else {// push sync
                push(headersMap, response, build, bintrayParams);
            }
        } catch (Exception e) {
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
     * push build to bintray async
     *
     * @param headersMap    - headers map from request
     * @param bintrayParams - bintray params
     * @param build         - build
     */
    private void backgroundPush(Map<String, String> headersMap, BintrayParams bintrayParams, Build build) {
        bintrayService.executeAsyncPushBuild(build, bintrayParams, headersMap);
        String buildNameAndNumber = build.getName() + ":" + build.getNumber();
        String message = String.format(
                "Background Push of build '%s' to Bintray successfully scheduled to run.", buildNameAndNumber);
        log.info(message);
    }

    /**
     * push to bintray sync
     *
     * @param headersMap   - headers map
     * @param response     - encapsulate data related to request
     * @param build        - build
     * @param bintrayModel - bintray model
     */
    private void push(Map<String, String> headersMap, RestResponse response, Build build, BintrayParams bintrayModel) {
        try {
            BasicStatusHolder statusHolder = bintrayService.pushBuild(build, bintrayModel, headersMap);
            if (statusHolder.hasErrors()) {
                response.error(statusHolder.getLastError().getMessage());
            } else if (statusHolder.hasWarnings()) {
                List<StatusEntry> warnings = statusHolder.getWarnings();
                response.error(warnings.get(warnings.size() - 1).getMessage());
            } else {
                String buildNameAndNumber = build.getName() + ":" + build.getNumber();
                String versionFilesPathUrl = bintrayService.getVersionFilesUrl(bintrayModel);
                String successMessages = "Successfully pushed build '" + buildNameAndNumber + "' to ";
                response.info(successMessages);
                response.url(versionFilesPathUrl);
            }
        } catch (IOException e) {
            response.error("Connection failed with exception: " + e.getMessage());
        }
    }
}
