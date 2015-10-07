package org.artifactory.webapp.wicket.util.validation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Shay Yaakov
 */
@Test
public class EmailAddressValidatorTest {

    @Test
    public void testValidEmails() throws Exception {
        EmailAddressValidator validator = new EmailAddressValidator();
        String[] emails = new String[]{"b.blaat@topicus.nl",
                "blaat@hotmail.com",
                "1.2.3.4@5.6.7.nl",
                "m@m.nl",
                "M@M.NL",
                "shay+friends@gmail.com",
                "shay.yaakov+friends@gmail.com"};
        for (String email : emails) {
            assertTrue(validator.getPattern().matcher(email).matches(), email + " should be valid");
        }
    }

    @Test
    public void testInvalidEmails() throws Exception {
        EmailAddressValidator validator = new EmailAddressValidator();
        String[] emails = new String[]{".blaat@topicus.nl",
                "blaat.@hotmail.com",
                "blaat@nl",
                "blaat@.nl"};
        for (String email : emails) {
            assertFalse(validator.getPattern().matcher(email).matches(), email + " should not be valid");
        }

    }
}