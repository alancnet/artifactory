package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;


/**
 * Retrieves the available repositories for maven indexer
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetIndexerAvailableRepositories extends GetAvailableRepositories {

    @Override
    protected Predicate<RepoDescriptor> getFilter(ArtifactoryRestRequest request, String type) {
        String layout = request.getQueryParamByKey("layout");
        return repo -> filterByType(RepoType.valueOf(type), repo) && filterByLayout(layout, repo);
    }

    private boolean filterByType(RepoType type, RepoDescriptor repo) {
        return type.isMavenGroup() ? repo.getType().isMavenGroup() : repo.getType().equals(type);
    }

    private boolean filterByLayout(String layout, RepoDescriptor repo) {
        RepoLayout repoLayout = repo.getRepoLayout();
        return repo.getRepoLayout() == null || StringUtils.isNotBlank(layout) && layout.equals(repoLayout.getName());
    }
}