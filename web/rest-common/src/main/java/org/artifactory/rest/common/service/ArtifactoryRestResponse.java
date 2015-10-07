package org.artifactory.rest.common.service;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.FeedbackMsg;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.ResponseHandler;
import org.artifactory.util.UiRequestUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * @author Chen Keinan
 */
@Component("artifactoryUiResponse")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ArtifactoryRestResponse<T> implements RestResponse<T> {
    private Collection<T> iModelList;
    private T iModel;
    private HttpServletResponse servletResponse;
    private HttpServletRequest servletRequest;
    private FeedbackMsg feedbackMsg = new FeedbackMsg();
    private boolean hasModelList;
    private boolean hasModel;
    boolean uiCall;

    public FeedbackMsg getFeedbackMsg() {
        return feedbackMsg;
    }

    public boolean hasModel() {
        return hasModel;
    }

    private int responseCode = SC_OK;

    public ArtifactoryRestResponse() {
    }

    public ArtifactoryRestResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ArtifactoryRestResponse responseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }

    public void setServletResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }

    public Collection<T> getiModelList() {
        return iModelList;
    }

    @Override
    public Object getEntity() {
        if (this.hasModel) {
            // return object entity

            return getObject();
        } else if (this.hasModelList) {
            //return collection entity
            Collection<T> ts = getiModelList();
            Iterator<T> iterator = ts.iterator();
            Object entity = getObject(iterator, ts);
            if (entity != null) return entity;
        } else {// send feedback message
            if (getFeedbackMsg().hasMessages()) {
                return getFeedbackMsg().toString();
            }
        }
        return null;
    }

    /**
     * return collection entity
     *
     * @param iterator   - collection iterator
     * @param collection - collection
     * @return response entity
     */
    private Object getObject(Iterator<T> iterator, Collection<T> collection) {
        if (iterator.hasNext()) {
            // serialize model with object mapper
            if (iterator.next() instanceof RestModel) {
                return collection.toString();
            } else {
                return collection;
            }
        }
        return collection.toString();
    }


    /**
     * return object entityr
     *
     * @return entity
     */
    private Object getObject() {
        if (getIModel() instanceof RestModel) {
            return getIModel().toString();
        } else {
            return getIModel();
        }
    }

    /**
     *
     * @param iModelList
     * @return
     */
    public ArtifactoryRestResponse iModelList(Collection<T> iModelList) {
        this.iModelList = iModelList;
        if (iModelList != null) {
            hasModelList = true;
        } else {
            hasModelList = false;
        }
        return this;
    }

    public <T> T getIModel() {
        return (T) iModel;
    }

    /**
     *
     * @param iModel
     * @return
     */
    public ArtifactoryRestResponse iModel(T iModel) {
        this.iModel = iModel;
        if (iModel != null) {
            hasModel = true;
        } else {
            hasModel = false;
        }
        return this;
    }


    /**
     * update response data with model data
     */
    public Response buildResponse() {
        Response serviceResponse;
        if (feedbackMsg.hasError() && responseCode == SC_OK) {
            responseCode = SC_BAD_REQUEST;
        }
        // add feedback msg to model if exist
        updateFeedbackMsgOnTopModel();
        serviceResponse = ResponseHandler.buildJerseyResponse(this);
        return serviceResponse;
    }

    /**
     * update feedback msg to model if exist
     */
    private void updateFeedbackMsgOnTopModel() {
        if (hasModel && feedbackMsg.hasMessages() && getIModel() instanceof BaseModel) {
            ((BaseModel) getIModel()).setFeedbackMsg(feedbackMsg);
        }
    }

    public ArtifactoryRestResponse error(String error) {
        feedbackMsg.setError(error);
        return this;
    }

    public ArtifactoryRestResponse warn(String warn) {
        feedbackMsg.setWarn(warn);
        return this;
    }

    public ArtifactoryRestResponse info(String info) {
        feedbackMsg.setInfo(info);
        return this;
    }

    @Override
    public ArtifactoryRestResponse errors(List<String> errors) {
        feedbackMsg.setErrors(errors);
        return this;
    }

    @Override
    public ArtifactoryRestResponse url(String url) {
        feedbackMsg.setUrl(url);
        return this;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
        if (UiRequestUtils.isUiRestRequest(servletRequest)) {
            setUiCall(true);
        }
    }

    public boolean isUiCall() {
        return uiCall;
    }

    public void setUiCall(boolean uiCall) {
        this.uiCall = uiCall;
    }
}
