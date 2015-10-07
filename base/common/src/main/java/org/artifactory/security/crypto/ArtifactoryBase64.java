/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.security.crypto;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 5/18/14 6:23 PM
 *
 * @author freds
 */
public abstract class ArtifactoryBase64 {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryBase64.class);

    private static final String DEFAULT_ENCRYPTION_PREFIX = "{DESede}";
    // since maven 2.1.0 the curly braces are treated as special characters and hence needs to be escaped
    // but still, maven sends the password with the escape characters. go figure...
    private static final String ESCAPED_DEFAULT_ENCRYPTION_PREFIX = "\\{DESede\\}";
    private static String encryptionPrefix;

    public static boolean isCorrectFormat(String in) {
        return extractBytes(in) != null;
    }

    public static boolean isPasswordEncrypted(String in) {
        if (!isCorrectFormat(in)) {
            return false;
        }
        return in.startsWith(ESCAPED_DEFAULT_ENCRYPTION_PREFIX) || in.startsWith(getEncryptionPrefix());
    }

    public static byte[] extractBytes(String encrypted) {
        String stripped;
        if (encrypted.startsWith(ESCAPED_DEFAULT_ENCRYPTION_PREFIX)) {
            stripped = StringUtils.removeStart(encrypted, ESCAPED_DEFAULT_ENCRYPTION_PREFIX);
        } else if (encrypted.startsWith(getEncryptionPrefix())) {
            stripped = StringUtils.removeStart(encrypted, getEncryptionPrefix());
        } else if (encrypted.length() > 125) {
            // The private and public key are big and have no {DESede} in the front but are full base64
            stripped = encrypted;
        } else {
            return null;
        }
        if (Base64.isBase64(stripped)) {
            return fromBase64(stripped);
        }
        return null;
    }

    public static String convertToString(byte[] encrypted, boolean master) {
        if (master) {
            return toBase64(encrypted);
        } else {
            return getEncryptionPrefix() + toBase64(encrypted);
        }
    }

    private static String getEncryptionPrefix() {
        if (StringUtils.isBlank(encryptionPrefix)) {
            String surroundCharacters = ConstantValues.securityAuthenticationEncryptedPasswordSurroundChars.getString();
            if ((surroundCharacters.length() % 2) != 0) {
                log.error("Provided with an asymmetric pair of encrypted password prefix surrounding characters: " +
                        "falling back to the default.");
                surroundCharacters = ConstantValues.securityAuthenticationEncryptedPasswordSurroundChars.getDefValue();
            }

            int middle = surroundCharacters.length() / 2;
            String opening = surroundCharacters.substring(0, middle);
            String closing = surroundCharacters.substring(middle, surroundCharacters.length());
            encryptionPrefix = new StringBuilder(opening).append("DESede").append(closing).toString();
        }

        return encryptionPrefix;
    }

    static String toBase64(byte[] bytes) {
        return CryptoHelper.bytesToString(Base64.encodeBase64(bytes));
    }

    static byte[] fromBase64(String base64Encoded) {
        return Base64.decodeBase64(CryptoHelper.stringToBytes(base64Encoded));
    }

    /**
     * Escape the encrypted password for maven usage.
     *
     * @param encryptedPassword Encrypted password to escape
     * @return Escaped encrypted password.
     */
    public static String escapeEncryptedPassword(String encryptedPassword) {

        if (encryptedPassword.startsWith(DEFAULT_ENCRYPTION_PREFIX)) {
            return encryptedPassword.replace(DEFAULT_ENCRYPTION_PREFIX, ESCAPED_DEFAULT_ENCRYPTION_PREFIX);
        }
        return encryptedPassword;
    }

    public static boolean isEncryptedPasswordPrefixedWithDefault(String encryptedPassword) {
        return encryptedPassword.startsWith(DEFAULT_ENCRYPTION_PREFIX);
    }
}
