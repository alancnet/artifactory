package org.artifactory.rest.common.service;

/**
 * @author Chen Keinan
 */
public interface RestService<T> {

    /**
     * Execute service method (i.e:login,create user and etc)
     *
     * @param request  - encapsulate all data require for request processing
     * @param response - encapsulate all data require from response
     * @return data model to be build in response
     */
    void execute(ArtifactoryRestRequest<T> request, RestResponse response);
}
