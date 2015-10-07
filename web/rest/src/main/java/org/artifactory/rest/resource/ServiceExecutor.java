package org.artifactory.rest.resource;

import org.artifactory.rest.http.request.ArtifactoryRestRequest;
import org.artifactory.rest.http.response.IResponse;
import org.artifactory.rest.services.IService;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Component("serviceExecutor")
public class ServiceExecutor {

    public Response process(ArtifactoryRestRequest restReq, IResponse restRes, IService serviceAction) {
        // execute service method
        serviceAction.execute(restReq, restRes);
        // build response
        Response response = restRes.buildResponse();
        return response;
    }
}
