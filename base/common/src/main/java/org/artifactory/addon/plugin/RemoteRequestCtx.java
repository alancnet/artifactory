package org.artifactory.addon.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public class RemoteRequestCtx extends ResourceStreamCtx {

    private Map<String, String> headers = new HashMap<>();

    public Map<String, String> getHeaders() {return headers;}

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
