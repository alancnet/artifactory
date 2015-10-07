package org.artifactory.aql;

/**
 * @author Gidi Shabat
 */
public class AqlException extends RuntimeException {
    public AqlException(String message) {
        super(message);
    }

    public AqlException(String message, Exception e) {
        super(message, e);
    }
}