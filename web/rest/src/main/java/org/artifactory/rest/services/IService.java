package org.artifactory.rest.services;

import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;

/**
 * @author Chen Keinan
 */
public interface IService {

    /**
     * execute service method (i.e:login,create user and etc)
     *
     * @param artifactoryRequest  - encapsulate all data require for request processing
     * @param artifactoryResponse - encapsulate all data require from response
     * @return data model to be build in response
     */
    void execute(ArtifactoryRestRequest artifactoryRequest, IResponse artifactoryResponse);
}
