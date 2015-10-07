package org.artifactory.ui.rest.resource;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.ServiceExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * @author chen keinan
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public abstract class BaseResource {
    @Context
    protected HttpServletRequest servletRequest;
    @Context
    protected HttpServletResponse servletResponse;
    @Autowired
    protected ServiceExecutor serviceExecutor;
    protected RestResponse artifactoryResponse;

    @Context Request request;
    @Context UriInfo uriInfo;
    @Context HttpHeaders httpHeaders;

    @Autowired
    @Qualifier("artifactoryUiResponse")
    public void setArtifactoryResponse(RestResponse artifactoryResponse) {
        this.artifactoryResponse = artifactoryResponse;
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     * @param modelData - rest data type
     * @return instance of ArtifactoryRestRequest
     */
    protected <Y> ArtifactoryRestRequest<Y> getArtifactoryRestRequest(Y modelData) {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders)
                .model(modelData);
        return new ArtifactoryRestRequest(builder);
     }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     * @param modelsData - rest data type List
     * @return instance of ArtifactoryRestRequest
     */
    protected <Y> ArtifactoryRestRequest<Y> getArtifactoryRestRequest(List<Y> modelsData) {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders)
                .models(modelsData);
        return new ArtifactoryRestRequest(builder);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) with  model
     *
     * @param service - service type (Login Service and etc)
     * @return - rest response
     */
    protected <Y> Response runService(RestService<Y> service, Y model) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest(model);
        updateServletData();
        // process service request
        return serviceExecutor.process(artifactoryRequest,artifactoryResponse,service);
    }

    /**
     * update http servlet request and response
     */
    private void updateServletData() {
        ((ArtifactoryRestResponse) artifactoryResponse).setServletResponse(servletResponse);
        ((ArtifactoryRestResponse) artifactoryResponse).setServletRequest(servletRequest);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) with  model
     *
     * @param service - service type (Login Service and etc)
     * @return rest response
     */
    protected <Y> Response runService(RestService<List<Y>> service, List<Y> model) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest(model);
        updateServletData();
        // process service request
        return serviceExecutor.process(artifactoryRequest, artifactoryResponse, service);
    }

    /**
     * execute service operation (i.e.: create user , login and etc) without model
     * @param service - service type (Login Service and etc)
     * @return - rest response
     */
    protected <Y> Response runService(RestService<Y> service) {
        // get encapsulated request data
        ArtifactoryRestRequest artifactoryRequest = getArtifactoryRestRequest();
        updateServletData();
        // process service request
        return serviceExecutor.process(artifactoryRequest,artifactoryResponse,service);
    }

    /**
     * return ArtifactoryRestRequest instance with http servlet response property
     * @return instance of ArtifactoryRestRequest
     */
    protected <Y> ArtifactoryRestRequest<Y> getArtifactoryRestRequest() {
        ArtifactoryRestRequest.RequestBuilder builder = new ArtifactoryRestRequest.
                RequestBuilder(servletRequest, request, uriInfo, httpHeaders);
        return new ArtifactoryRestRequest(builder);
    }
}
