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

package org.artifactory.util;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests {@link org.artifactory.util.UrlValidator}.
 *
 * @author Michael Pasternak
 */
@Test
public class UrlValidatorTest {

    @SuppressWarnings({"unchecked"})
    @BeforeMethod
    protected void setUp() throws Exception {
    }

    public void httpIp() throws UrlValidator.UrlValidationException {
        UrlValidator validator = new UrlValidator("http");
        validator.validate("http://127.0.0.1/test");
    }

    public void httpHostname() throws UrlValidator.UrlValidationException {
        UrlValidator validator = new UrlValidator("http", "https", "ldap");
        validator.validate("ldap://localhost/test/123456");
    }

    public void httpHostnameWithUnderscore() throws UrlValidator.UrlValidationException {
        UrlValidator validator = new UrlValidator("http", "https", "ftp");
        validator.validate("https://with_underscore/any");
    }

    @Test(expectedExceptions = {UrlValidator.UrlValidationException.class})
    public void illegalSchema() throws UrlValidator.UrlValidationException {
        UrlValidator validator = new UrlValidator("http", "ftp");
        validator.validate("https://localhost/test");
    }

    @Test(expectedExceptions = {UrlValidator.UrlValidationException.class})
    public void invalidSchema() throws UrlValidator.UrlValidationException {
        UrlValidator validator = new UrlValidator("http", "ftp");
        validator.validate("htddd://localhost/test");
    }

    @Test(expectedExceptions = {UrlValidator.UrlValidationException.class})
    public void invalidHostname() throws UrlValidator.UrlValidationException {
        UrlValidator validator = new UrlValidator("http", "ftp");
        validator.validate("https://no host/test");
    }
}
