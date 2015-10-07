package org.artifactory.ui.rest.service.artifacts.search.searchresults;

import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CopySearchResultsService extends BaseSearchResultService {
    private static final Logger log = LoggerFactory.getLogger(RemoveSearchResultsService.class);

    @Autowired
    RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String searchName = request.getQueryParamByKey("name");
        String moveToRepoKey = request.getQueryParamByKey("repoKey");
        boolean isDryRun = Boolean.valueOf(request.getQueryParamByKey("dryRun"));
        Set<RepoPath> pathsToMove = getRepoPaths(request, searchName);
        MoveMultiStatusHolder status = repoService.copy(pathsToMove, moveToRepoKey,
                (Properties) InfoFactoryHolder.get().createProperties(), isDryRun, false);
        if (status.hasErrors()) {
            updateErrorResponse(response, status);
        }
        else {
            response.info("Search results successfully copied to: "+moveToRepoKey);
        }
    }
}
