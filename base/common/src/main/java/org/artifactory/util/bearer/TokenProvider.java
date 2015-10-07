package org.artifactory.util.bearer;

import org.apache.http.auth.Credentials;
import org.artifactory.spring.ReloadableBean;

import java.util.Map;

/**
 * Provides a bearer token to be used by {@link BearerScheme}
 *
 * @author Shay Yaakov
 */
public interface TokenProvider extends ReloadableBean {

    String getToken(Map<String, String> challengeParams, String method, String uri, Credentials credentials);
}
