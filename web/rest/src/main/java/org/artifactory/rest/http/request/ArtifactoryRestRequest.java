package org.artifactory.rest.http.request;


import org.artifactory.api.rest.restmodel.IModel;
import org.artifactory.util.UiRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class ArtifactoryRestRequest {

    private HttpServletRequest servletRequest;
    private Request request;
    private UriInfo uriInfo;
    private HttpHeaders httpHeaders;
    private IModel imodel;
    private List<IModel> models;
    private boolean uiRestCall;

    public ArtifactoryRestRequest(RequestBuilder requestBuilder) {
        this.servletRequest = requestBuilder.servletRequest;
        this.request = requestBuilder.request;
        this.uriInfo = requestBuilder.uriInfo;
        this.httpHeaders = requestBuilder.httpHeaders;
        this.imodel = requestBuilder.imodel;
        this.models = requestBuilder.imodels;
        this.uiRestCall = UiRequestUtils.isUiRestRequest(servletRequest);
    }

    /**
     * return path param by key
     *
     * @param key - path param key
     * @return path param value
     */
    public String getPathParamByKey(String key) {
        String value = "";
        List<String> valueList = uriInfo.getPathParameters().get(key);
        if (valueList != null && !valueList.isEmpty()) {
            String currentValue = valueList.get(0);
            value = (currentValue == null) ? currentValue : currentValue.replaceAll("/", "");
        }
        return value;
    }

    /**
     * return path param by key
     *
     * @param key - path param key
     * @return path param value
     */
    public String getQueryParamByKey(String key) {
        String value = "";
        List<String> valueList = uriInfo.getQueryParameters().get(key);
        if (valueList != null && !valueList.isEmpty()) {
            String currentValue = valueList.get(0);
            value = (currentValue == null) ? currentValue : currentValue;
        }
        return value;
    }

    public boolean isUiRestCall() {
        return uiRestCall;
    }

    public void setUiRestCall(boolean uiRestCall) {
        this.uiRestCall = uiRestCall;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public List getModels() {
        return models;
    }

    public void setModels(List<IModel> models) {
        this.models = models;
    }

    public IModel getImodel() {
        return imodel;
    }

    public void setImodel(IModel imodel) {
        this.imodel = imodel;
    }

    public static class RequestBuilder {
        private HttpServletRequest servletRequest;
        private Request request;
        private UriInfo uriInfo;
        private HttpHeaders httpHeaders;
        private IModel imodel;
        private String id;
        private List<IModel> imodels;


        public RequestBuilder(HttpServletRequest servletRequest, Request request, UriInfo uriInfo,
                HttpHeaders httpHeaders) {
            this.servletRequest = servletRequest;
            this.request = request;
            this.uriInfo = uriInfo;
            this.httpHeaders = httpHeaders;
        }

        public RequestBuilder model(IModel imodel) {
            this.imodel = imodel;
            return this;
        }

        public RequestBuilder models(List<IModel> iModels) {
            this.imodels = iModels;
            return this;
        }
    }
}
