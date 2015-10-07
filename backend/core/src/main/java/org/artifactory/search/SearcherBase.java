/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.search;

import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.ItemSearchResults;
import org.artifactory.api.search.SearchControls;
import org.artifactory.api.search.Searcher;
import org.artifactory.common.ConstantValues;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.sapi.search.InvalidQueryRuntimeException;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryService;
import org.artifactory.schedule.TaskInterruptedException;
import org.artifactory.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yoavl
 */
public abstract class SearcherBase<C extends SearchControls, R extends ItemSearchResult> implements Searcher<C, R> {
    private static final Logger log = LoggerFactory.getLogger(SearcherBase.class);

    private final VfsQueryService vfsQueryService;
    private final InternalRepositoryService repoService;

    //Cache this calculation (and don't make it a static...)
    private final int maxResults = ConstantValues.searchUserQueryLimit.getInt();

    protected SearcherBase() {
        ArtifactoryContext context = ContextHelper.get();
        vfsQueryService = context.beanForType(VfsQueryService.class);
        repoService = context.beanForType(InternalRepositoryService.class);
    }

    @Override
    public final ItemSearchResults<R> search(C controls) {
        long start = System.currentTimeMillis();
        ItemSearchResults<R> results;
        try {
            results = doSearch(controls);
        } catch (TaskInterruptedException e) {
            throw e;
        } catch (Exception e) {
            //Handle bad queries
            @SuppressWarnings({"unchecked", "ThrowableResultOfMethodCallIgnored"})
            Throwable invalidQueryException = ExceptionUtils.getCauseOfTypes(e, InvalidQueryRuntimeException.class);
            if (invalidQueryException != null) {
                log.debug("Invalid query encountered.", e);
                throw (InvalidQueryRuntimeException) e;
            } else {
                log.error("Could not perform search.", e);
                throw new RepositoryRuntimeException("Could not execute search query", e);
            }
        }
        long time = System.currentTimeMillis() - start;
        results.setTime(time);
        log.debug("Total search time: {} ms", time);
        return results;
    }

    protected VfsQuery createQuery(SearchControls controls) {
        VfsQuery query = getVfsQueryService().createQuery();
        if (controls.isSpecificRepoSearch()) {
            query.setRepoKeys(controls.getSelectedRepoForSearch());
        }
        return query;
    }

    public abstract ItemSearchResults<R> doSearch(C controls);

    public InternalRepositoryService getRepoService() {
        return repoService;
    }

    public VfsQueryService getVfsQueryService() {
        return vfsQueryService;
    }

    protected int getMaxResults() {
        return maxResults;
    }

    /**
     * Indicates whether the given result repo path is valid or not.<br> A repo path will stated as valid if the given
     * origin local repo is not null, the path is readable (permission and repo-configuration-wise) and if it is not a
     * checksum file.
     *
     * @param repoPath Repo path to validate
     * @return True if the repo path is valid
     */
    protected boolean isResultAcceptable(RepoPath repoPath) {
        if (repoPath == null) {
            return false;
        }
        LocalRepo localRepo = getRepoService().localOrCachedRepositoryByKey(repoPath.getRepoKey());
        return isResultAcceptable(repoPath, localRepo);
    }

    /**
     * Indicates whether the given result repo path is valid or not.<br> A repo path will stated as valid if the given
     * origin local repo is not null, the path is readable (permission and repo-configuration-wise) and if it is not a
     * checksum file.
     *
     * @param repoPath        Repo path to validate
     * @param sourceLocalRepo Source local repo to assert as valid
     * @return True if the repo path is valid
     */
    protected boolean isResultAcceptable(RepoPath repoPath, LocalRepo sourceLocalRepo) {
        return (sourceLocalRepo != null) && repoService.isRepoPathVisible(repoPath) &&
                (!NamingUtils.isChecksum(repoPath.getPath()));
    }

    protected int getLimit(SearchControls controls) {
        if (controls.isLimitSearchResults()) {
            return getMaxResults();
        }
        return Integer.MAX_VALUE;
    }
}