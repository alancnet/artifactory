package org.artifactory.sapi.search;

import org.artifactory.sapi.common.RepositoryRuntimeException;

/**
 * Date: 8/6/11
 * Time: 1:56 PM
 *
 * @author Fred Simon
 */
public class InvalidQueryRuntimeException extends RepositoryRuntimeException {
    public InvalidQueryRuntimeException(String message) {
        super(message);
    }

    public InvalidQueryRuntimeException(Throwable cause) {
        super(cause);
    }

    public InvalidQueryRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
