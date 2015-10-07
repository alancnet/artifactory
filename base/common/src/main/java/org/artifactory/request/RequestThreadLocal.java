package org.artifactory.request;

import java.io.IOException;

/*
 * @author Lior Azar
 */
public class RequestThreadLocal {

    private static ThreadLocal<RequestThreadLocalContext> context = new ThreadLocal<>();

    private RequestThreadLocal() {
    }

    public static void set(RequestWrapper requestWrapper) {
        context.set(RequestThreadLocalContext.create(requestWrapper));
    }

    public static void destroy() throws IOException {
        try {
            RequestThreadLocalContext requestThreadLocalContext = context.get();
            if (requestThreadLocalContext != null) {
                requestThreadLocalContext.destroy();
            }
        } finally {
            context.remove();
        }
    }

    public static String getClientAddress(){
        RequestThreadLocalContext requestThreadLocalContext = context.get();
        if (requestThreadLocalContext != null) {
            return requestThreadLocalContext.getRequestThreadLocal().getClientAddress();
        }
        return "";
    }
}