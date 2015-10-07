package org.artifactory.ui.rest.service.admin.configuration.repositories.util;


import com.google.common.base.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.PathUtils;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Create Http request for test replication button
 *
 * @author Aviad Shikloshi
 */
public class TestMethodFactory {

    public static HttpRequestBase createTestMethod(String repoUrl,
            RemoteAdvancedRepositoryConfigModel advancedRepository, TypeSpecificConfigModel typeSpecific) {

        if (advancedRepository == null || typeSpecific == null) {
            throw new RuntimeException("Missing repository details");
        }

        RepoType repoType = typeSpecific.getRepoType();
        HttpRequestBase request;
        switch (repoType) {
            case NuGet:
                request = createNuGetTestMethod(repoUrl, advancedRepository.getQueryParams());
                break;
            case Gems:
                request = createGemsTestMethod(repoUrl);
                break;
            case Docker:
                request = createDockerTestMethod(repoUrl);
                break;
            default:
                request = new HttpHead(HttpUtils.encodeQuery(repoUrl));
        }
        return request;
    }

    private static HttpRequestBase createGemsTestMethod(String repoUrl) {
        String path = repoUrl;
        if (path.endsWith("/")) {
            path = PathUtils.trimTrailingSlashes(path);
        }
        path += "/api/v1/dependencies";
        return new HttpGet(path);
    }

    private static HttpRequestBase createNuGetTestMethod(String repoUrl, String queryParams) {
        try {
            URIBuilder uriBuilder = new URIBuilder(repoUrl);
            HttpRequestBase request = new HttpGet();
            if(StringUtils.isNotBlank(queryParams)) {
                List<NameValuePair> queryParamsMap = URLEncodedUtils.parse(queryParams, Charsets.UTF_8);
                uriBuilder.setParameters(queryParamsMap);
            }
            request.setURI(uriBuilder.build());
            return request;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to build test URI", e);
        }
    }

    private static HttpRequestBase createDockerTestMethod(String repoUrl) {
        String path = repoUrl;
        if (path.endsWith("/")) {
            path = PathUtils.trimTrailingSlashes(path);
        }
        path += "/v2/";
        return new HttpGet(path);
    }

    private TestMethodFactory() {
    }

}
