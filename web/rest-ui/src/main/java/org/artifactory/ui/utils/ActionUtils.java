package org.artifactory.ui.utils;

import org.artifactory.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
public class ActionUtils {


    public static String getDownloadLink(HttpServletRequest request, String repoKey, String repoPath) {
        return HttpUtils.getServletContextUrl(request) + "/" + repoKey + "/" + repoPath;
    }
}