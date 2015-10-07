package org.artifactory.ui.utils;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.search.SavedSearchResults;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.security.ArtifactoryPermission;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen keinan
 */
public class RequestUtils {

    public static final String REPO_KEY_PARAM = "repoKey";
    public static final String PATH_PARAM = "path";

    /**
     * return request headers map
     *
     * @param request - http servlet request
     * @return - map of request headers
     */
    public static Map<String, String> getHeadersMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        if (request != null) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                map.put(headerName.toUpperCase(), request.getHeader(headerName));
            }
        }
        return map;
    }

    public static RepoPath getPathFromRequest(ArtifactoryRestRequest request) {
        return InternalRepoPathFactory.create(request.getQueryParamByKey(REPO_KEY_PARAM),
                request.getQueryParamByKey(PATH_PARAM));
    }

    public static String getRepoKeyFromRequest(ArtifactoryRestRequest request) {
        return request.getQueryParamByKey(REPO_KEY_PARAM);
    }

    public static void setResultsToRequest(SavedSearchResults savedSearchResults, HttpServletRequest request) {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        if (authService.hasPermission(ArtifactoryPermission.DEPLOY)) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                session = request.getSession(true);
            }
            session.setAttribute(savedSearchResults.getName(), savedSearchResults);
        }
    }

    /**
     * @param name    - save search result
     * @param request - http servlet request
     * @return - search result
     */
    public static SavedSearchResults getResultsFromRequest(String name, HttpServletRequest request) {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        if (authService.hasPermission(ArtifactoryPermission.DEPLOY)) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return null;
            }
            return (SavedSearchResults) session.getAttribute(name);
        }
        return null;
    }

    /**
     * @param name    - save search result
     * @param request - http servlet request
     * @return - search result
     */
    public static void removeResultsToRequest(String name, HttpServletRequest request) throws Exception {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        if (authService.hasPermission(ArtifactoryPermission.DEPLOY)) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                throw new Exception("no search result to remove");
            }
            session.removeAttribute(name);
        }
    }
}
