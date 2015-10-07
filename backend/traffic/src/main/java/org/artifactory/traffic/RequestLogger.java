/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.traffic;

import org.artifactory.traffic.entry.RequestEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger class for general web requests.
 * An example of log line:
 * <code>20140508154145|2632|REQUEST|82:12:13:14|admin|GET|/jcenter/org/iostreams-0.2.jar|HTTP/1.1|200|8296</code>
 * The columns are pipe delimited:
 * <ol>
 *  <li>Log entry date (the end of the request) in the format of YYYYMMDDHHMMSS</li>
 *  <li>Processing time in millis</li>
 *  <li>Request type ({@link org.artifactory.traffic.TrafficAction})</li>
 *  <li>IP address of the requesting user</li>
 *  <li>Username (non_authenticated_user for anonymous)</li>
 *  <li>The HTTP request method</li>
 *  <li>The requested resource path</li>
 *  <li>The HTTP protocol version</li>
 *  <li>HTTP response code</li>
 *  <li>Content length in byes of the response in case of HEAD and GET requests and of the request in case of PUT and POST requests</li>
 * </ol>
 *
 * @author Noam Tenne
 */
public abstract class RequestLogger {
    private static final Logger log = LoggerFactory.getLogger(RequestLogger.class);

    private RequestLogger() {
        // utility class
    }

    /**
     * Logs a web request
     *
     * @param userAddress   Address of client machine
     * @param username      Client Artifactory username
     * @param method        HTTP Request method
     * @param path          Request path
     * @param protocol      Request protocol
     * @param returnCode    Response status code
     * @param contentLength Response body size for GET requests or request body size in case of PUT or POST
     */
    public static void request(String userAddress, String username, String method, String path, String protocol,
            int returnCode, long contentLength, long duration) {
        RequestEntry requestEntry = new RequestEntry(userAddress, username, method, path, protocol, returnCode,
                contentLength, duration);
        log.info(requestEntry.toString());
    }
}