package org.artifactory.webapp.wicket.util.validation;

import org.apache.wicket.validation.validator.PatternValidator;

import java.util.regex.Pattern;

/**
 * A custom email validator which works the same as {@link org.apache.wicket.validation.validator.EmailAddressValidator}
 * with the exception of accepting emails with + sign (e.g: shay+friends@gmail.com)
 *
 * @author Shay Yaakov
 */
public class EmailAddressValidator extends PatternValidator {

    private static final EmailAddressValidator INSTANCE = new EmailAddressValidator();

    /**
     * Retrieves the singleton instance of <code>EmailAddressValidator</code>.
     *
     * @return the singleton instance of <code>EmailAddressValidator</code>
     */
    public static EmailAddressValidator getInstance() {
        return INSTANCE;
    }

    /**
     * Protected constructor to force use of static singleton accessor. Override this constructor to
     * implement resourceKey(Component).
     */
    protected EmailAddressValidator() {
        super(
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-\\+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)",
                Pattern.CASE_INSENSITIVE);
    }
}