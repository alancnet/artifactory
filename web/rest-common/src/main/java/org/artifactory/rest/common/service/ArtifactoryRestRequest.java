package org.artifactory.rest.common.service;

import org.artifactory.util.UiRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class ArtifactoryRestRequest<T> {

    private HttpServletRequest servletRequest;
    private Request request;
    private UriInfo uriInfo;
    private HttpHeaders httpHeaders;
    private T imodel;
    private List<T> models;
    private boolean uiRestCall;

    public ArtifactoryRestRequest(RequestBuilder<T> requestBuilder) {
        this.servletRequest = requestBuilder.servletRequest;
        this.request = requestBuilder.request;
        this.uriInfo = requestBuilder.uriInfo;
        this.httpHeaders = requestBuilder.httpHeaders;
        imodel = requestBuilder.imodel;
        models = requestBuilder.imodels;
        this.uiRestCall = UiRequestUtils.isUiRestRequest(servletRequest);
     }

    /**
     * return path param by key
     * @param key - path param key
     * @return path param value
     */
    public String getPathParamByKey(String key){
        String value="";
        List<String> valueList = uriInfo.getPathParameters().get(key);
        if (valueList != null && !valueList.isEmpty()){
            String currentValue = valueList.get(0);
            value =   (currentValue==null)? currentValue : currentValue.replaceAll("/","");
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

    public List<T> getModels() {
        return models;
    }

    public void setModels(List<T> models) {
        this.models = models;
    }

    public T getImodel() {
        return (T) imodel;
    }

    public void setImodel(T imodel) {
        this.imodel = imodel;
    }

    public PagingData getPagingData() {
        return new PagingData(this);
    }

    public long getPages(long allUsersGroupsCount, long currentPageSize, String limit, String offset) {
        long numOfPages = allUsersGroupsCount / currentPageSize;
        long mod = allUsersGroupsCount % currentPageSize;
        if (mod > 0 && numOfPages == Integer.parseInt(limit) - Integer.parseInt(offset)) {
            numOfPages = numOfPages + 1;
        }
        return numOfPages;
    }

    public static class RequestBuilder<T> {
        private HttpServletRequest servletRequest;
        private Request request;
        private UriInfo uriInfo;
        private HttpHeaders httpHeaders;
        private T imodel;
        private String id;
        private List<T> imodels;


        public RequestBuilder(HttpServletRequest servletRequest, Request request, UriInfo uriInfo,
                HttpHeaders httpHeaders) {
            this.servletRequest = servletRequest;
            this.request = request;
            this.uriInfo = uriInfo;
            this.httpHeaders = httpHeaders;
        }

        public RequestBuilder model(T imodel) {
            this.imodel = imodel;
            return this;
        }

        public RequestBuilder models(List<T> iModels) {
            this.imodels = iModels;
            return this;
        }
    }
}
