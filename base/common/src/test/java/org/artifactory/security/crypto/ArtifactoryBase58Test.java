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

import org.testng.annotations.Test;

import static org.artifactory.security.crypto.ArtifactoryBase58.decode;
import static org.artifactory.security.crypto.ArtifactoryBase58.encode;
import static org.testng.Assert.assertEquals;

/**
 * Tests the bases 58 encoding
 *
 * @autho Yossi Shaul
 */
@Test
public class ArtifactoryBase58Test {

    public void pureBase58Encoding() {
        assertEquals(encode("286755fad04869ca523320acce0dc6a4".getBytes()),
                "4P36jbxC2teSxQ9tBPTFfPz5fmtL3jG3tFtsLTkmTfrK");
        assertEquals(encode("h".getBytes()), "2o");
        assertEquals(encode("#0@".getBytes()), "CpXZ");
        assertEquals(encode("sdf34tmo 3po4tk3opsv 34 t34t6 ebergv3w45 bt".getBytes()),
                "9W1pJHcN8v4aWXofPp8KBXjSTaCiVqdQExA4duH616XD7H8cz9VoeDSiohu");
        assertEquals(encode("\"12432EFWQER#@234qravfqwv32".getBytes()), "5d2qLgyvpfGsdeLSYA3Hos6muR3DKKVysZ43s");
        assertEquals(encode("PFAJ#OOOOOOASDvsdfsfasfd11111111111111111111111111111".getBytes()),
                "2U3pyXTXsQt9GtdpqYGH2ns6jZF6mZ2FrCHgMtt9RxmvcTEePN8v1QSfWiVU66dRRB1TKAMex");
    }

    public void pureBase58Decoding() {
        assertEquals(new String(decode("2d4bH6gYhhJCCs5WGS35tALmDqsZ")), "this is how we do it");
        assertEquals(new String(decode("2DNGgN9CaCS4TwwcT")), "@Jfrog#SWAMP");
        assertEquals(new String(decode("5jga22KMYWJSDczEVFAaxMNc4fekocBdgm3YxTiTCCJX8KaghMwabBZa2kn")),
                "@Jfrog#SWAMP9sdsdfj4qioienjbnoka[awemjidwew");
        assertEquals(new String(decode("3MnTTd4bFfKCxXDKHoVxfLqiPxxb1WSd1JURMGgesX3EFrTR8sPRbgb8W8WWCGKHGo")),
                "@Jfrog#SWAMP9sdsdfj4qioidsfw09-0---___0kdsfoij94");
        assertEquals(new String(
                        decode("2Fw7NuAc3Q9omjY4hvc2gfwCS9JWATxK7oJasXKDx3zZS4bQCyda86S9zwjh35JaAP1Xxt7R879PknEMLfHd")),
                "]>{LadsfweSAE>#0qwfe-l2fc9sdsdfj4qioidsfw09-0---___0kdsfoij94");
    }

    public void base58EncodeDecode() {
        assertEquals(new String(decode(encode("a".getBytes()))), "a");
        assertEquals(new String(decode(encode("ofer".getBytes()))), "ofer");
        assertEquals(new String(decode(encode("dima".getBytes()))), "dima");
        assertEquals(new String(decode(encode("daniel".getBytes()))), "daniel");
        assertEquals(new String(decode(encode("roy".getBytes()))), "roy");
    }
}