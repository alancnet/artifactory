package org.artifactory.ui.rest.service.utils.validation;

/**
 * Exception thrown on invalid input from a UI control.
 *
 * @author Yossi Shaul
 */
public class ValidationException extends Exception {
    /**
     * Builds a new validation exception with message to display to the user.
     *
     * @param uiMessage Message to display in the UI
     */
    public ValidationException(String uiMessage) {
        super(uiMessage);
    }
}
