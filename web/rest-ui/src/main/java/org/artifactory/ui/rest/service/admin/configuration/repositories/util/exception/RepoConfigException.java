package org.artifactory.ui.rest.service.admin.configuration.repositories.util.exception;

/**
 * Exception to propagate replication errors back to UI
 *
 * @author Dan Feldman
 */
public class RepoConfigException extends Exception {

    int statusCode;

    public RepoConfigException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
