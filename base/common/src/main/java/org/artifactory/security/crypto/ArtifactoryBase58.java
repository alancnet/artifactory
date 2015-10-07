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

import org.artifactory.checksum.ChecksumType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Date: 5/20/14 11:25 PM
 *
 * @author freds
 */
public abstract class ArtifactoryBase58 {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryBase58.class);

    private static final char ARTIFACTORY_BYTE = 'A';
    private static final char ARTIFACTORY_PASSWORD_BYTE = 'P';  // encrypted with user specific key
    private static final char ARTIFACTORY_MASTER_BYTE = 'M';    // encrypted with master encryption key
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
            .toCharArray();
    private static final int BASE_58 = ALPHABET.length;
    private static final int BASE_256 = 256;

    private static final int[] INDEXES = new int[128];
    private static final byte[] EMPTY_STRING_CODE = new byte[2];

    static {
        for (int i = 0; i < INDEXES.length; i++) {
            INDEXES[i] = -1;
        }
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

    public static boolean isCorrectFormat(String input) {
        return extractBytes(input) != null;
    }

    public static boolean isPasswordEncrypted(String input) {
        if (!isCorrectFormat(input)) {
            return false;
        }
        return input.charAt(1) == ARTIFACTORY_PASSWORD_BYTE;
    }

    public static boolean isMasterEncrypted(String input) {
        if (!isCorrectFormat(input)) {
            return false;
        }
        return input.charAt(1) == ARTIFACTORY_MASTER_BYTE;
    }

    public static byte[] extractBytes(String input) {
        if (input != null && input.length() > 3 && input.charAt(0) == ARTIFACTORY_BYTE
                && (input.charAt(1) == ARTIFACTORY_MASTER_BYTE || input.charAt(1) == ARTIFACTORY_PASSWORD_BYTE)) {
            if (!isBase58(input)) {
                return null;
            }

            byte[] inputWithChk = decode(input.substring(2));
            MessageDigest digest = getSha256MessageDigest();
            byte[] bytes = copyOfRange(inputWithChk, 0, inputWithChk.length - 2);
            byte[] doubleDigest = digest.digest(digest.digest(bytes));
            if ((doubleDigest[0] != inputWithChk[inputWithChk.length - 2])
                    || (doubleDigest[1] != inputWithChk[inputWithChk.length - 1])) {
                return null;
            }
            return bytes;
        }
        return null;
    }

    public static String convertToString(byte[] toEncode, boolean master) {
        byte[] bytes;
        if (toEncode == null || toEncode.length == 0) {
            checkEmptyString();
            bytes = EMPTY_STRING_CODE;
        } else {
            MessageDigest digest = getSha256MessageDigest();
            byte[] doubleDigest = digest.digest(digest.digest(toEncode));
            bytes = new byte[toEncode.length + 2];
            System.arraycopy(toEncode, 0, bytes, 0, toEncode.length);
            bytes[bytes.length - 2] = doubleDigest[0];
            bytes[bytes.length - 1] = doubleDigest[1];
        }
        String result = "" + ARTIFACTORY_BYTE + (master ? ARTIFACTORY_MASTER_BYTE : ARTIFACTORY_PASSWORD_BYTE)
                + encode(bytes);
        return result;
    }

    private static void checkEmptyString() {
        if (EMPTY_STRING_CODE[0] == 0) {
            MessageDigest digest = getSha256MessageDigest();
            byte[] doubleDigest = digest.digest(digest.digest(new byte[0]));
            EMPTY_STRING_CODE[0] = doubleDigest[0];
            EMPTY_STRING_CODE[1] = doubleDigest[1];
        }
    }

    public static MessageDigest getSha256MessageDigest() {
        try {
            return MessageDigest.getInstance(ChecksumType.sha256.alg());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isBase58(String input) {
        char[] chars = input.toCharArray();
        for (char c : chars) {
            boolean inAlphabet = false;
            for (char c1 : ALPHABET) {
                if (c1 == c) {
                    inAlphabet = true;
                    break;
                }
            }
            if (!inAlphabet) {
                return false;
            }
        }
        return true;
    }

    /**
     * Pure base 58 encoding
     *
     * @param input Input to encode
     * @return Decoded string
     */
    public static String encode(byte[] input) {
        if (input.length == 0) {
            // paying with the same coin
            return "";
        }

        //
        // Make a copy of the input since we are going to modify it.
        //
        input = copyOfRange(input, 0, input.length);

        //
        // Count leading zeroes
        //
        int zeroCount = 0;
        while (zeroCount < input.length && input[zeroCount] == 0) {
            ++zeroCount;
        }

        //
        // The actual encoding
        //
        byte[] temp = new byte[input.length * 2];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input.length) {
            byte mod = divmod58(input, startAt);
            if (input[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = (byte) ALPHABET[mod];
        }

        //
        // Strip extra '1' if any
        //
        while (j < temp.length && temp[j] == ALPHABET[0]) {
            ++j;
        }

        //
        // Add as many leading '1' as there were leading zeros.
        //
        while (--zeroCount >= 0) {
            temp[--j] = (byte) ALPHABET[0];
        }

        byte[] output = copyOfRange(temp, j, temp.length);
        return new String(output);
    }

    /**
     * Pure base 58 decoding
     *
     * @param input Input to decode
     * @return Decoded bytes
     */
    public static byte[] decode(String input) {
        if (input.length() == 0) {
            // paying with the same coin
            return new byte[0];
        }

        byte[] input58 = new byte[input.length()];
        //
        // Transform the String to a base58 byte sequence
        //
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);

            int digit58 = -1;
            if (c >= 0 && c < 128) {
                digit58 = INDEXES[c];
            }
            if (digit58 < 0) {
                throw new RuntimeException("Not a ArtifactoryBase58 input: " + input);
            }

            input58[i] = (byte) digit58;
        }

        //
        // Count leading zeroes
        //
        int zeroCount = 0;
        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }

        //
        // The encoding
        //
        byte[] temp = new byte[input.length()];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input58.length) {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }

        //
        // Do no add extra leading zeroes, move j to first non null byte.
        //
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    private static byte divmod58(byte[] number, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number.length; i++) {
            int digit256 = (int) number[i] & 0xFF;
            int temp = remainder * BASE_256 + digit256;

            number[i] = (byte) (temp / BASE_58);

            remainder = temp % BASE_58;
        }

        return (byte) remainder;
    }

    private static byte divmod256(byte[] number58, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number58.length; i++) {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * BASE_58 + digit58;

            number58[i] = (byte) (temp / BASE_256);

            remainder = temp % BASE_256;
        }

        return (byte) remainder;
    }

    private static byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);

        return range;
    }
}
