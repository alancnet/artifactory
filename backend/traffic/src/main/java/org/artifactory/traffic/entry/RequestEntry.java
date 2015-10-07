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

package org.artifactory.traffic.entry;

import org.artifactory.traffic.TrafficAction;

/**
 * General web request traffic entry.
 * <p/>
 * Example: 20090319110249|100|REQUEST|127.0.0.1|admin|GET|/remote-repos/antlr/antlr/2.7.6/antlr-2.7.6.jar|HTTP/1.1|200|443432
 *
 * @author Noam Tenne
 */
public class RequestEntry extends TokenizedTrafficEntry {
    private static final int COLUMNS_COUNT_SPEC = 10;

    private String userAddress;
    private String username;
    private String method;
    private String path;
    private String protocol;
    private int returnCode;
    private long contentLength;

    /**
     * Parses the given textual entry and sets the object fields accordingly
     *
     * @param entry Textual entry
     */
    public RequestEntry(String entry) {
        super(entry);
        this.userAddress = tokens[3];
        this.username = tokens[4];
        this.method = tokens[5];
        this.path = tokens[6];
        this.protocol = tokens[7];
        this.returnCode = Integer.parseInt(tokens[8]);
        this.contentLength = Long.parseLong(tokens[9]);
    }

    /**
     * Sets the given entry data in the relevant fields
     *
     * @param userAddress   Address of client machine
     * @param username      Client Artifactory username
     * @param method        HTTP Request method
     * @param path          Request path
     * @param protocol      Request protocol
     * @param returnCode    Response status code
     * @param contentLength Response body size
     */
    public RequestEntry(String userAddress, String username, String method, String path, String protocol,
            int returnCode, long contentLength, long duration) {
        super(duration);
        this.userAddress = userAddress;
        this.username = username;
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.returnCode = returnCode;
        this.contentLength = contentLength;
    }

    @Override
    protected void initTokens() {
        super.initTokens();
        tokens[3] = userAddress;
        tokens[4] = username;
        tokens[5] = method;
        tokens[6] = path;
        tokens[7] = protocol;
        tokens[8] = returnCode + "";
        tokens[9] = contentLength + "";
    }

    @Override
    public TrafficAction getAction() {
        return TrafficAction.REQUEST;
    }

    @Override
    public int getColumnsCount() {
        return COLUMNS_COUNT_SPEC;
    }

    /**
     * Returns the address of the client's machine
     *
     * @return String - Address of client machine
     */
    public String getUserAddress() {
        return userAddress;
    }

    /**
     * Returns the client's Artifactory username
     *
     * @return String - Client Artifactory username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the request method
     *
     * @return HTTP Request method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the request path
     *
     * @return String - Request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the request protocol
     *
     * @return String - Request protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns the response status code
     *
     * @return int - Response status code
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * Returns the response body size
     *
     * @return Response body size
     */
    public long getContentLength() {
        return contentLength;
    }
}
