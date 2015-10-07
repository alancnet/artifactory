package org.artifactory.rest.http.response;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author Chen Keinan
 */
public interface IResponse {

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
    void setResponseCode(int responseCode);

    /**
     * set List of model (i.e. users list and etc) to be send in entity
     * with rest response
     *
     * @param iModelList
     */
    void setIModelList(Collection iModelList);

    /**
     * set single model (i.e. users  and etc) to be send in entity
     * with rest response
     *
     * @param iModel
     */
    void setIModel(Object iModel);

    /**
     * return rest servlet response
     *
     * @return HttpServletResponse instance for this rest call
     */
    HttpServletResponse getServletResponse();

    void setServletResponse(HttpServletResponse servletResponse);

}
