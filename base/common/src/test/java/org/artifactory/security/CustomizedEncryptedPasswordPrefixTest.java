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

package org.artifactory.security;

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.crypto.ArtifactoryBase64;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.test.ArtifactoryHomeStub;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;

import static org.testng.Assert.assertTrue;

/**
 * Tests the customized encrypted password prefix brackets
 *
 * @author Noam Y. Tenne
 */
@Test
public class CustomizedEncryptedPasswordPrefixTest extends ArtifactoryHomeBoundTest {

    @Test(enabled = false)
    public void testStandardEncryptCharacters() throws Exception {
        nullifyEncryptionPrefix();
        setSurroundAndBase("%%&&", "false");
        SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(CryptoHelper.generateKeyPair());
        String encrypted = CryptoHelper.encryptSymmetric("toto", secretKey, false);
        assertTrue(encrypted.startsWith("A"), "Encrypted password should have been prefixed with A " +
                "since customized surrounding characters are ignored in Base58.");
    }

    @Test
    public void testCustomizedEncryptedPasswordPrefixSurroundingCharacters() throws Exception {
        nullifyEncryptionPrefix();
        setSurroundAndBase("%%&&", "true");
        SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(CryptoHelper.generateKeyPair());
        String encrypted = CryptoHelper.encryptSymmetric("toto", secretKey, false);
        assertTrue(encrypted.startsWith("%%DESede&&"), "Encrypted password should have been prefixed with the " +
                "customized surrounding characters.");
    }

    @Test
    public void testCustomizedEncryptedPasswordPrefixWithInvalidSurroundingCharacters() throws Exception {
        nullifyEncryptionPrefix();
        setSurroundAndBase("###*&", "true");
        SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(CryptoHelper.generateKeyPair());
        String encrypted = CryptoHelper.encryptSymmetric("toto", secretKey, false);
        assertTrue(encrypted.startsWith("{DESede}"), "Encrypted password should have been prefixed with the " +
                "default surrounding characters since the customized ones were not of an even number.");
    }

    private static void nullifyEncryptionPrefix() throws NoSuchFieldException, IllegalAccessException {
        Field encryptionPrefix = ArtifactoryBase64.class.getDeclaredField("encryptionPrefix");
        encryptionPrefix.setAccessible(true);
        encryptionPrefix.set(null, null);
    }

    private static void setSurroundAndBase(String surroundChars, String base64) {
        ((ArtifactoryHomeStub) ArtifactoryHome.get()).setProperty(
                ConstantValues.securityAuthenticationEncryptedPasswordSurroundChars, surroundChars);
        ((ArtifactoryHomeStub) ArtifactoryHome.get()).setProperty(
                ConstantValues.securityUseBase64, base64);
    }

}
