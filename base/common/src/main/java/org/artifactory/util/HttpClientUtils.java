/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.net.UnknownHostException;

/**
 * Set of utils for easier configuration of apache http client.
 *
 * @author Yossi Shaul
 */
public abstract class HttpClientUtils {
    private HttpClientUtils() {
        // utility class
    }

    /**
     * Creates a custom configuration for the request based on the default client configuration.
     * Http client will disregard the default client configuration if a request contains specific configuration. So it
     * important to copy from the client default config and not just creating a plain new config.
     *
     * @param client  The client expected to run this request
     * @param request The request for the custom config
     * @return Request configuration builder based on the client defaults ot the already existing request config.
     */
    public static RequestConfig.Builder copyOrCreateConfig(@Nonnull HttpClient client, HttpRequestBase request) {
        if (request.getConfig() != null) {
            // request already has custom config -> copy from it
            return RequestConfig.copy(request.getConfig());
        }

        RequestConfig defaultConfig = getDefaultConfig(client);
        if (defaultConfig != null) {
            // create based on the client default config
            return RequestConfig.copy(defaultConfig);
        } else {
            return RequestConfig.custom();
        }
    }

    /**
     * @param e The throwable to inspect for the error message
     * @return Most fitting error message for the given throwable. Tries to prevent empty exception messages.
     */
    public static String getErrorMessage(Throwable e) {
        if (e == null) {
            return null;
        }
        String message = e.getMessage(); // default message
        if (e instanceof UnknownHostException) {
            message = "Unknown host - " + e.getMessage();
        } else if (e instanceof ClientProtocolException) {
            // ClientProtocolException doesn't return a message but holds the cause with the message
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
        }
        if (StringUtils.isBlank(message)) {
            message = e.getClass().toString();
        }
        return message;
    }

    private static RequestConfig getDefaultConfig(HttpClient client) {
        if (client == null) {
            return null;
        }
        try {
            Field requestConfigField = client.getClass().getDeclaredField("defaultConfig");
            requestConfigField.setAccessible(true);
            return (RequestConfig) ReflectionUtils.getField(requestConfigField, client);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Failed to get default request config", e);
        }
    }
}
