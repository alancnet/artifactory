package org.artifactory.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by michaelp on 6/16/15.
 */
public class UrlValidator {

    private final String[] allowedSchemes;

    /**
     * Creates new URI validator.
     *
     * @param allowedSchemes List of allowed uri schemes (http, ldap, etc.). If empty all schemes are allowed.
     */
    public UrlValidator(String... allowedSchemes) {
        this.allowedSchemes = allowedSchemes;
    }

    /**
     * @return allowed schemes
     */
    public String[] getAllowedSchemes() {
        return allowedSchemes;
    }

    /**
     * Validates URI
     *
     * @param url
     * @throws UrlValidationException is thrown when URL is in illegal form.
     */
    public void validate(String url) throws UrlValidationException {

        if (!PathUtils.hasText(url)) {
            throw new RuntimeException("The URL cannot be empty");
        }

        try {
            URI parsedUri = new URIBuilder(url).build();
            String scheme = parsedUri.getScheme();
            if (!isAnySchemaAllowed() && StringUtils.isBlank(scheme)) {

                throw new UrlValidationException(String.format(
                        "Url scheme cannot be empty. The following schemes are allowed: %s. " +
                                "For example: %s://host",
                        Arrays.asList(allowedSchemes), allowedSchemes[0]
                ));


            } else if (!isAllowedSchema(scheme)) {
                throw new UrlValidationException(String.format(
                        "Scheme '%s' is not allowed. The following schemes are allowed: %s",
                        scheme, Arrays.asList(allowedSchemes)));
            }

            HttpHost host = URIUtils.extractHost(parsedUri);
            if (host == null) {
                throw new UrlValidationException("Cannot resolve host from url: " + url);
            }
        } catch (URISyntaxException e) {
            throw new UrlValidationException(String.format("'%s' is not a valid url", url));
        }
    }

    /**
     * Allowed schemas
     *
     * @param scheme
     * @return boolean
     */
    public boolean isAllowedSchema(String scheme) {
        if (isAnySchemaAllowed()) {
            return true;
        }

        for (String allowedScheme : allowedSchemes) {
            if (allowedScheme.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if any schema allowed
     *
     * @return boolean
     */
    public boolean isAnySchemaAllowed() {
        return allowedSchemes == null || allowedSchemes.length == 0;
    }

    /**
     * Raised on validation error
     */
    public class UrlValidationException extends Exception {
        /**
         * @param message validation error
         */
        public UrlValidationException(String message) {
            super(message);
        }
    }
}
