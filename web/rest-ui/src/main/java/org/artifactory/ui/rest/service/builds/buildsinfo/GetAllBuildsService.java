package org.artifactory.ui.rest.service.builds.buildsinfo;

import org.artifactory.api.build.BuildService;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.build.BuildRun;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.PagingData;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission.PagingModel;
import org.artifactory.ui.rest.model.builds.GeneralBuildInfo;
import org.artifactory.ui.utils.ModelDbMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetAllBuildsService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(GetAllBuildsService.class);

    @Autowired
    CentralConfigService centralConfigService;

    @Autowired
    BuildService buildService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        // get paging data from request
        PagingData pagingData = request.getPagingData();
        // fetch build info data
        fetchAllBuildsData(response, pagingData);
    }

    /**
     * fect all build data by type
     *
     * @param artifactoryResponse - encapsulate data related to response
     * @param pagingData          - paging data sa send from client
     */
    private void fetchAllBuildsData(RestResponse artifactoryResponse, PagingData pagingData) {
        Map<String, String> buildsMap = ModelDbMap.getBuildsMap();
        String offset = pagingData.getStartOffset();
        String orderBy = buildsMap.get(pagingData.getOrderBy());
        String direction = pagingData.getDirection();
        String limit = pagingData.getLimit();
        Set<BuildRun> latestBuildsPaging = buildService.getLatestBuildsPaging(offset, orderBy, direction, limit);
        if (latestBuildsPaging != null && !latestBuildsPaging.isEmpty()) {
            List<GeneralBuildInfo> generalBuildInfoList = new ArrayList<>();
            latestBuildsPaging.forEach(buildRun ->
                    generalBuildInfoList.add(new GeneralBuildInfo(new GeneralBuildInfo.BuildBuilder()
                                    .buildName(buildRun.getName())
                                    .lastBuildTime(centralConfigService.getDateFormatter().print(buildRun.getStartedDate().getTime()))
                                    .buildNumber(buildRun.getNumber()).ciUrl(buildRun.getCiUrl()).
                                            time(buildRun.getStartedDate() != null ? buildRun.getStartedDate().getTime() : 0))
                    ));
            PagingModel pagingModel = new PagingModel(0, generalBuildInfoList);
            artifactoryResponse.iModel(pagingModel);
        }
    }
}
