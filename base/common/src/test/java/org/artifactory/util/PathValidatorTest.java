package org.artifactory.util;

import org.testng.annotations.Test;

import java.nio.file.InvalidPathException;

/**
 * Tests the behavior of {@link PathValidator}
 *
 * @author Shay Yaakov
 */
@Test
public class PathValidatorTest {

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleSlash() {
        PathValidator.validate("");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleColon() {
        PathValidator.validate(":");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleAmpersand() {
        PathValidator.validate("&");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleTokenAmpersand() {
        PathValidator.validate("bbb/&");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleDot() {
        PathValidator.validate(".");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleDotDot() {
        PathValidator.validate("..");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidDot() {
        PathValidator.validate("blabla/.");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidDotDot() {
        PathValidator.validate("dot/../dot/file.jar");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidAllSpaces() {
        PathValidator.validate("       ");
    }

    @Test
    public void validCarretLeft() {
        PathValidator.validate("hibla<sdf");
    }

    @Test
    public void validCarretRight() {
        PathValidator.validate("path/to>file.jar");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalBackslash() {
        PathValidator.validate("back\\slash.zors");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalQuestionMark() {
        PathValidator.validate("riddle/me_this?.tar");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalQuotationMark() {
        PathValidator.validate("make\"believe.hello");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalPipe() {
        PathValidator.validate("sup|r|mario");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalStar() {
        PathValidator.validate("the/universe/has/lots/of/*");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalColon() {
        PathValidator.validate("c/olo:n/repo/path");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSpaceBeforeSlash() {
        PathValidator.validate(" shsh/gaga /hdt");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSpaceAfterSlash() {
        PathValidator.validate("blabla/hello/      ");
    }

    public void validPaths() {
        PathValidator.validate("antlr/antlr/2.7.6/antlr-2.7.6.jar");
        PathValidator.validate("ca/juliusdavies/not-yet-commons-ssl/0.3.9/not-yet-commons-ssl-0.3.9.jar.sha1");
        PathValidator.validate("commons-codec/commons-codec/1.5/commons-codec-1.5.pom.md5");
        PathValidator.validate("/axis/axis-saaj/1.4/axis-saaj-1.4.pom");
        PathValidator.validate("cccc}dd{dd");
        PathValidator.validate("dsfsdf!cgxcbv$");
        PathValidator.validate("sdf/d dd$dd");
        PathValidator.validate("P@ssw0rd.h$sh");
        PathValidator.validate("sdfs9(vcx).jar");
        PathValidator.validate("xcv[sdf..sdf]");
        PathValidator.validate("vxvc^fvnb^..sdfsde&");
        PathValidator.validate("a=1&b=2");
        PathValidator.validate("copy   maven     project");
        PathValidator.validate("de/regnis/sequence/1.0.0/sequence-library-1.0.0.jar;tmp.sha1.tmp");
        PathValidator.validate("vv/aa..bb/&gg.pom");
        PathValidator.validate("v1.0...0");
        PathValidator.validate("jar-100%.jar");
        PathValidator.validate("gogo,baba.jar");
        PathValidator.validate("file    tab.jar");
        PathValidator.validate(".index");
        PathValidator.validate("..index");
        PathValidator.validate("blabla/.index");
        PathValidator.validate("blabla/..index");
        PathValidator.validate("blabla/index.");
        PathValidator.validate("blabla/index./asdasd");
        PathValidator.validate("blabla/index../asdasd");
    }
}
