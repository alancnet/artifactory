package org.artifactory.request;

import org.artifactory.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;

/*
 * @author Lior Azar
 */
public class RequestWrapper {
    private HttpServletRequest request;

    public RequestWrapper(HttpServletRequest request) {
        this.request = request;
    }

    public String getClientAddress() {
        return HttpUtils.getRemoteClientAddress(request);
    }
}
