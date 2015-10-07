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

package org.artifactory.webapp.wicket.util.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests {@link UriValidator}.
 *
 * @author Yossi Shaul
 */
@Test
public class UriValidatorTest {
    private IValidatable<String> validatable;

    @SuppressWarnings({"unchecked"})
    @BeforeMethod
    protected void setUp() throws Exception {
        validatable = EasyMock.createMock(IValidatable.class);
    }

    public void httpIp() {
        EasyMock.expect(validatable.getValue()).andReturn("http://127.0.0.1/test").times(2);
        EasyMock.replay(validatable);
        UriValidator validator = new UriValidator("http");
        validator.validate(validatable);
        EasyMock.verify(validatable);
    }

    public void httpHostname() {
        EasyMock.expect(validatable.getValue()).andReturn("ldap://localhost/test/123456").times(2);
        EasyMock.replay(validatable);
        UriValidator validator = new UriValidator("http", "https", "ldap");
        validator.validate(validatable);
        EasyMock.verify(validatable);
    }

    public void httpHostnameWithUnderscore() {
        // RTFACT-3184
        EasyMock.expect(validatable.getValue()).andReturn("https://with_underscore/any").times(2);
        EasyMock.replay(validatable);
        UriValidator validator = new UriValidator("http", "https", "ftp");
        validator.validate(validatable);
        EasyMock.verify(validatable);
    }

    public void invalidSchema() {
        EasyMock.expect(validatable.getValue()).andReturn("https://localhost/test").times(2);
        validatable.error(EasyMock.<IValidationError>anyObject());  // expect an error
        EasyMock.replay(validatable);
        UriValidator validator = new UriValidator("http", "ftp");
        validator.validate(validatable);
        EasyMock.verify(validatable);
    }

    public void invalidHostname() {
        EasyMock.expect(validatable.getValue()).andReturn("https://no host/test").times(2);
        validatable.error(EasyMock.<IValidationError>anyObject());  // expect an error
        EasyMock.replay(validatable);
        UriValidator validator = new UriValidator("http", "ftp");
        validator.validate(validatable);
        EasyMock.verify(validatable);
    }
}
