package org.artifactory.aql;

/**
 * @author Gidi Shabat
 */
public class AqlParserException extends AqlException {
    public AqlParserException(String message) {
        super(message);
    }

    public AqlParserException(String message, Exception e) {
        super(message, e);
    }
}
