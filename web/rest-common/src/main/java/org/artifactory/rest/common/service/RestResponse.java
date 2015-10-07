package org.artifactory.rest.common.service;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

/**
 * @author Chen Keinan
 */
public interface RestResponse<T> {

    /**
     * build rest response instance with status code and entity (if needed)
     *
     * @return standard Rest Response instance
     */
     Response buildResponse();

    /**
     * set the status code following to rest request to be send back
     * with response if not set , default code  = 200
     *
     * @param responseCode - status code
     */
    ArtifactoryRestResponse responseCode(int responseCode);

    /**
     * set List of model (i.e. users list and etc) to be send in entity
     * with rest response
     *
     * @param iModelList
     */
    ArtifactoryRestResponse iModelList(Collection<T> iModelList);

    /**
     * set single model (i.e. users  and etc) to be send in entity
     * with rest response
     *
     * @param iModel
     */
    ArtifactoryRestResponse iModel(T iModel);

    /**
     * feedback error msg
     *
     * @param error - error msg
     */
    ArtifactoryRestResponse error(String error);

    /**
     * feedback warn msg
     *
     * @param warn - warn msg
     */
    ArtifactoryRestResponse warn(String warn);

    /**
     * feedback info msg
     *
     * @param info - info msg
     */
    ArtifactoryRestResponse info(String info);

    ArtifactoryRestResponse errors(List<String> errors);

    ArtifactoryRestResponse url(String url);

    /**
     * return rest servlet response
     *
     * @return HttpServletResponse instance for this rest call
     */
     HttpServletResponse getServletResponse();

    <T> T getIModel();

    Collection<T> getiModelList();

    Object getEntity();

    int getResponseCode();
}
