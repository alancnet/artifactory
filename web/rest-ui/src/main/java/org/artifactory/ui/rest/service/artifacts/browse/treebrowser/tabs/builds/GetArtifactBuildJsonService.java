package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.builds;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.build.ArtifactBuildAddon;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.builds.BuildJsonInfo;
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
public class GetArtifactBuildJsonService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetArtifactBuildJsonService.class);

    @Autowired
    private BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
          try {
              String startTime  = DateUtils.formatBuildDate(Long.parseLong(request.getQueryParamByKey("startTime")));
              String buildName = request.getQueryParamByKey("buildName");
              String buildNumber = request.getQueryParamByKey("buildNumber");
             AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
             ArtifactBuildAddon artifactBuildAddon = addonsManager.addonByType(ArtifactBuildAddon.class);
            BuildRun artifactBuildByBuildNumber = artifactBuildAddon.getBuildRun(buildName, buildNumber, startTime);
            if (artifactBuildByBuildNumber != null) {
                String json = buildService.getBuildAsJson(artifactBuildByBuildNumber);
                BuildJsonInfo buildJsonInfo = new BuildJsonInfo(json);
                response.iModel(buildJsonInfo);
            }
          } catch (ParseException e) {
              log.error(e.toString());
          }
    }
}
