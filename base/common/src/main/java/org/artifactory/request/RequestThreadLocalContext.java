package org.artifactory.request;

import java.io.IOException;

/*
 * @author Lior Azar
 */
public class RequestThreadLocalContext {
    private final RequestWrapper requestWrapper;

    public static RequestThreadLocalContext create(RequestWrapper requestWrapper) {
        return new RequestThreadLocalContext(requestWrapper);
    }

    public void destroy() throws IOException {
    }

    protected RequestThreadLocalContext(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    protected RequestWrapper getRequestThreadLocal() {
        return requestWrapper;
    }
}
