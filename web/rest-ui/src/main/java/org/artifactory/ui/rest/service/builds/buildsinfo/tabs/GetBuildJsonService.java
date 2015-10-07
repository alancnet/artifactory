package org.artifactory.ui.rest.service.builds.buildsinfo.tabs;

import org.artifactory.api.build.BuildService;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.ViewArtifact;
import org.artifactory.ui.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetBuildJsonService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetBuildJsonService.class);

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String buildName = request.getPathParamByKey("name");
            String buildNumber = request.getPathParamByKey("number");
            String buildStarted = DateUtils.formatBuildDate(Long.parseLong(request.getPathParamByKey("date")));
            BuildRun buildRun = buildService.getBuildRun(buildName, buildNumber, buildStarted);
            String buildJson = buildService.getBuildAsJson(buildRun);
            ViewArtifact buildJsonModel = new ViewArtifact();
            buildJsonModel.setFileContent(buildJson);
            response.iModel(buildJsonModel);
        } catch (ParseException e) {
            log.error(e.toString());
        }
    }
}
