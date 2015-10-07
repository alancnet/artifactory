package org.artifactory.ui.rest.service.utils.validation;

import org.apache.commons.lang.StringUtils;

/**
 * Generic name validator for root entity names (repos, descriptor keys etc.).
 *
 * @author Yossi Shaul
 */
public final class NameValidator {
    private static final char[] forbiddenChars = {'/', '\\', ':', '|', '?', '*', '"', '<', '>'};

    public static void validate(String name) throws ValidationException {
        if (StringUtils.isBlank(name)) {
            throw new ValidationException("Name cannot be blank");
        }

        if (name.equals(".") || name.equals("..") || name.equals("&")) {
            throw new ValidationException("Name cannot be empty link: '" + name + "'");
        }

        char[] nameChars = name.toCharArray();
        for (int i = 0; i < nameChars.length; i++) {
            char c = nameChars[i];
            for (char fc : forbiddenChars) {
                if (c == fc) {
                    throw new ValidationException("Illegal name character: '" + c + "' at index " + i + ": " + name);
                }
            }
        }
    }
}
