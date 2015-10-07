package org.artifactory.ui.rest.service.artifacts.search.classsearch;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchService;
import org.artifactory.api.search.archive.ArchiveSearchControls;
import org.artifactory.api.search.archive.ArchiveSearchResult;
import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.search.SearchResult;
import org.artifactory.ui.rest.model.artifacts.search.classsearch.ClassSearch;
import org.artifactory.ui.rest.model.artifacts.search.classsearch.ClassSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ClassSearchService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(ClassSearchService.class);

    @Autowired
    private SearchService searchService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        ClassSearch classSearch = (ClassSearch) request.getImodel();
        // get artifact search control instance
        ArchiveSearchControls archiveSearchControl = getArchiveSearchControl(classSearch);
        // check if search empty or contain wild card only
        if (isSearchEmptyOrWildCardOnly(archiveSearchControl)) {
            response.error("Search term empty or containing only wildcards is not permitted");
            return;
        }
        try {
            List<ClassSearchResult> classSearchResults = Lists.newArrayList();
            ItemSearchResults<ArchiveSearchResult> archiveSearchResults = searchService.searchArchiveContent( archiveSearchControl);
            for (ArchiveSearchResult archiveSearchResult : archiveSearchResults.getResults()) {
                classSearchResults.add(new ClassSearchResult(archiveSearchResult));
            }
            long resultsCount;
            int maxResults = ConstantValues.searchMaxResults.getInt();
            if (archiveSearchControl.isLimitSearchResults() && classSearchResults.size() > maxResults) {
                classSearchResults = classSearchResults.subList(0, maxResults);
                resultsCount = classSearchResults.size() == 0 ? 0 : archiveSearchResults.getFullResultsCount();
            } else {
                resultsCount = classSearchResults.size();
            }
            SearchResult model = new SearchResult(classSearchResults, archiveSearchControl.getQueryDisplay(),
                    resultsCount, archiveSearchControl.isLimitSearchResults());
            model.addNotifications(response);
            response.iModel(model);
        } catch (Exception e) {
            log.debug(e.toString());
            response.error(e.getMessage());
        }
    }

    /**
     * check if search is empty or contain wildcard only
     *
     * @param archiveSearchControl
     * @return if true - search is empty or containing wild card only
     */
    private boolean isSearchEmptyOrWildCardOnly(ArchiveSearchControls archiveSearchControl) {
        return archiveSearchControl.isEmpty() || archiveSearchControl.isWildcardsOnly();
    }

    /**
     * create artifact search control from class search instance
     *
     * @param classSearch - class search instance
     * @return artifact search control instance
     */
    private ArchiveSearchControls getArchiveSearchControl(ClassSearch classSearch) {
        ArchiveSearchControls archiveSearchControls = new ArchiveSearchControls();
        archiveSearchControls.setSelectedRepoForSearch(classSearch.getSelectedRepositories());
        archiveSearchControls.setLimitSearchResults(!classSearch.getSelectedRepositories().isEmpty());
        archiveSearchControls.setPath(classSearch.getPath());
        archiveSearchControls.setName(classSearch.getName());
        archiveSearchControls.setSearchClassResourcesOnly(classSearch.isSearchClassOnly());
        archiveSearchControls.setLimitSearchResults(true);
        archiveSearchControls.setExcludeInnerClasses(classSearch.isExcludeInnerClasses());
        return archiveSearchControls;
    }

    /**
     * add wild card to query
     *
     * @param artifactSearchControls - quick search model
     */
    private void setQueryWildCard(ArchiveSearchControls artifactSearchControls) {
        String query = artifactSearchControls.getName();
        if (StringUtils.isNotBlank(query)) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(query);

            if (!query.endsWith("*") && !query.endsWith("?")) {
                queryBuilder.append("*");
            }
            artifactSearchControls.setName(queryBuilder.toString());
        }
    }
}
