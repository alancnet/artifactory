package org.artifactory.repo;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gidi Shabat
 */
public class ArtifactoryStandardUrlResolver {

    private static final String[] templates = {"nuget", "yum", "npm", "gems", "deb", "pypi", "docker", "vcs", "bower"};
    private final Matcher remoteRepoUrlMatcher;

    private String url;

    public ArtifactoryStandardUrlResolver(String url) {
        this.url = url;
        Pattern remoteRepoUrlPattern = Pattern.compile("(.+)/([^/]+)");
        remoteRepoUrlMatcher = remoteRepoUrlPattern.matcher(url);
        if (!remoteRepoUrlMatcher.find()) {
            throwInvalidUrlForm(url);
        }
    }

    public String getBaseUrl() {
        String baseUrl = remoteRepoUrlMatcher.group(1);
        baseUrl = peelRestApi(baseUrl);
        if (StringUtils.isBlank(baseUrl)) {
            throwInvalidUrlForm(url);
        }
        return baseUrl;
    }

    public String getRepoKey() {
        String repoKey = remoteRepoUrlMatcher.group(2);
        if (StringUtils.isBlank(repoKey)) {
            throwInvalidUrlForm(url);
        }
        return repoKey;
    }

    private static String peelRestApi(String url) {
        for (String template : templates) {
            if (url.endsWith("/api/" + template)) {
                url = StringUtils.removeEnd(url, "/api/" + template);
            }
        }
        return url;
    }

    private void throwInvalidUrlForm(String remoteUrl) {
        throw new IllegalArgumentException("The URL form of '" + remoteUrl + "' is unsupported.");
    }
}
