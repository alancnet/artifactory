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

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * PathMatcher unit tests.
 *
 * @author Yossi Shaul
 */
@Test
public class PathMatcherTest extends ArtifactoryHomeBoundTest {

    public void matchWithIncludeAll() {
        // File
        boolean matches1 = PathMatcher.matches("apath", Arrays.asList("**"), Arrays.asList(""), false);
        assertTrue(matches1);
        // Folder
        boolean matches2 = PathMatcher.matches("apath", Arrays.asList("**"), Arrays.asList(""), true);
        assertTrue(matches2);
    }

    public void matchWithExcludeAll() {
        // File
        boolean matches1 = PathMatcher.matches("apath", Arrays.asList(""), Arrays.asList("**"), false);
        assertFalse(matches1);

        // Folder
        boolean matches2 = PathMatcher.matches("apath", Arrays.asList(""), Arrays.asList("**"), true);
        assertFalse(matches2);
    }

    public void matchWithIncludesOnly() {
        // File
        List<String> includes = Arrays.asList("apath/*", "**/my/test/path", "public/in/**");
        List<String> excludes = Arrays.asList("");
        assertFalse(PathMatcher.matches("apath", includes, excludes, false));
        assertTrue(PathMatcher.matches("this/is/my/test/path", includes, excludes, false));
        assertFalse(PathMatcher.matches("this/is/my/test/path/andmore", includes, excludes, false));
        assertFalse(PathMatcher.matches("this/is/my/test/path/andmore", includes, excludes, false));
        assertTrue(PathMatcher.matches("public/in/the/public.jar", includes, excludes, false));
        assertFalse(PathMatcher.matches("public", includes, excludes, false));
        assertFalse(PathMatcher.matches("public/i", includes, excludes, false));
        assertFalse(PathMatcher.matches("public2", Arrays.asList("apath/*", "public/in/**"), excludes, false));
        //Folder
        assertTrue(PathMatcher.matches("apath", includes, excludes, true));
        assertTrue(PathMatcher.matches("this/is/my/test/path", includes, excludes, true));
        assertTrue(PathMatcher.matches("this/is/my/test/path/andmore", includes, excludes, true));
        assertTrue(PathMatcher.matches("this/is/my/test/path/andmore", includes, excludes, true));
        assertTrue(PathMatcher.matches("public/in/the/public.jar", includes, excludes, true));
        assertTrue(PathMatcher.matches("public", includes, excludes, true));
        assertTrue(PathMatcher.matches("public/i", includes, excludes, true));
        assertFalse(PathMatcher.matches("public2", Arrays.asList("apath/*", "public/in/**"), excludes, true));
    }

    public void matchWithIncludesOnlyPartial() {
        // File
        List<String> excludes = Arrays.asList("");
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*p/x/y"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("**/y/*"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*/x/*"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*/*/*"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*/*/t"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*/x/t"), excludes, false));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("a?/x"), excludes, false));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("?p*/?"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("?p*/??"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("aa?/x"), excludes, false));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*/d/*"), excludes, false));
        assertFalse(PathMatcher.matches("apath", Arrays.asList("apath/some/other/*"), excludes, false));
        assertFalse(PathMatcher.matches("apath/some", Arrays.asList("apath/some/other/*"), excludes, false));
        assertFalse(PathMatcher.matches("apath/some2", Arrays.asList("apath/some/other/*"), excludes, false));
        assertFalse(PathMatcher.matches("apath2", Arrays.asList("apath/some/other/*"), excludes, false));
        assertFalse(
                PathMatcher.matches("com", Arrays.asList("com/some/other/*", "com/acme/***", "com/toto/*"), excludes,
                        false));
        assertFalse(
                PathMatcher.matches("org", Arrays.asList("com/some/other/*", "com/acme/***", "com/toto/*"), excludes,
                        false));
        assertFalse(PathMatcher.matches("apath2", Arrays.asList("apath/*"), excludes, false));
        assertFalse(PathMatcher.matches("apath", Arrays.asList("apath2/*"), excludes, false));
        assertFalse(PathMatcher.matches("apath", Arrays.asList("apath2"), excludes, false));
        // Folder
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("*p/x/y"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("**/y/*"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("*/x/*"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("*/*/*"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("*/*/t"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("*/x/t"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("a?/x"), excludes, true));
        assertTrue(PathMatcher.matches("ap/x", Arrays.asList("?p*/?"), excludes, true));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("?p*/??"), excludes, true));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("aa?/x"), excludes, true));
        assertFalse(PathMatcher.matches("ap/x", Arrays.asList("*/d/*"), excludes, true));
        assertTrue(PathMatcher.matches("apath", Arrays.asList("apath/some/other/*"), excludes, true));
        assertTrue(PathMatcher.matches("apath/some", Arrays.asList("apath/some/other/*"), excludes, true));
        assertFalse(PathMatcher.matches("apath/some2", Arrays.asList("apath/some/other/*"), excludes, true));
        assertFalse(PathMatcher.matches("apath2", Arrays.asList("apath/some/other/*"), excludes, true));
        assertTrue(
                PathMatcher.matches("com", Arrays.asList("com/some/other/*", "com/acme/***", "com/toto/*"), excludes,
                        true));
        assertFalse(
                PathMatcher.matches("org", Arrays.asList("com/some/other/*", "com/acme/***", "com/toto/*"), excludes,
                        true));
        assertFalse(PathMatcher.matches("apath2", Arrays.asList("apath/*"), excludes, true));
        assertFalse(PathMatcher.matches("apath", Arrays.asList("apath2/*"), excludes, true));
        assertFalse(PathMatcher.matches("apath", Arrays.asList("apath2"), excludes, true));

    }

    public void matchWithExcludesOnly() {
        // File
        List<String> includes = Collections.emptyList();
        List<String> excludes = Arrays.asList("apath", "**/my/test/path", "commons-*", "main/*");
        assertTrue(PathMatcher.matches("apat", includes, excludes, false));
        assertFalse(PathMatcher.matches("apath", includes, excludes, false));
        assertTrue(PathMatcher.matches("apath2", includes, excludes, false));
        assertFalse(PathMatcher.matches("commons-codec", includes, excludes, false));
        assertTrue(PathMatcher.matches("main.123", includes, excludes, false));
        assertTrue(PathMatcher.matches("apath/deep", includes, excludes, false));
        assertFalse(PathMatcher.matches("this/is/my/test/path", includes, excludes, false));
        assertTrue(PathMatcher.matches("this/is/my/test/path/andmore", includes, excludes, false));
        // Folder
        assertTrue(PathMatcher.matches("apat", includes, excludes, true));
        assertFalse(PathMatcher.matches("apath", includes, excludes, true));
        assertTrue(PathMatcher.matches("apath2", includes, excludes, true));
        assertFalse(PathMatcher.matches("commons-codec", includes, excludes, true));
        assertTrue(PathMatcher.matches("main.123", includes, excludes, true));
        assertTrue(PathMatcher.matches("apath/deep", includes, excludes, true));
        assertFalse(PathMatcher.matches("this/is/my/test/path", includes, excludes, true));
        assertTrue(PathMatcher.matches("this/is/my/test/path/andmore", includes, excludes, true));

    }

    public void matchExcludesAndIncludes() {
        // File
        List<String> includes = Arrays.asList("org/**", "com/**", "net/**");
        List<String> excludes = Arrays.asList("org/apache/**", "commons-*");
        assertTrue(PathMatcher.matches("org/codesulting", includes, excludes, false));
        assertTrue(PathMatcher.matches("com/test/123", includes, excludes, false));
        assertFalse(PathMatcher.matches("org/apache/bla", includes, excludes, false));
        assertFalse(PathMatcher.matches("commons-lang", includes, excludes, false));
        // Folder
        assertTrue(PathMatcher.matches("org/codesulting", includes, excludes, true));
        assertTrue(PathMatcher.matches("com/test/123", includes, excludes, true));
        assertFalse(PathMatcher.matches("org/apache/bla", includes, excludes, true));
        assertFalse(PathMatcher.matches("commons-lang", includes, excludes, true));
    }

    public void excludesTakesPrecedenceOverIncludes() {
        // currently excludes alway takes precedence, we might want to change it in the future so the closer will win
        // File
        List<String> excludes = Arrays.asList("apath/**");
        List<String> includes = Arrays.asList("**", "apath/sub/1");
        assertFalse(PathMatcher.matches("apath", includes, excludes, false));
        assertFalse(PathMatcher.matches("apath/sub", includes, excludes, false));
        assertFalse(PathMatcher.matches("apath/sub/1", includes, excludes,
                false));  // even though it is explicitly included
        assertTrue(PathMatcher.matches("apath2", includes, excludes, false));
        //Folder
        assertFalse(PathMatcher.matches("apath", includes, excludes, true));
        assertFalse(PathMatcher.matches("apath/sub", includes, excludes, true));
        assertFalse(
                PathMatcher.matches("apath/sub/1", includes, excludes, true));  // even though it is explicitly included
        assertTrue(PathMatcher.matches("apath2", includes, excludes, true));
    }

    public void subPathIncludes() {
        List<String> includes = Arrays.asList("apath1/sub1/**", "apath1/sub2/**", "apath2/sub1/**");
        //File
        assertFalse(PathMatcher.matches("apath", includes, null, false));
        assertFalse(PathMatcher.matches("apath1", includes, null, false));
        assertFalse(PathMatcher.matches("apath2", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/sub", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/sub1", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/sub1/t", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/sub1/toto/tutu", includes, null, false));
        // Folder
        assertFalse(PathMatcher.matches("apath", includes, null, true));
        assertTrue(PathMatcher.matches("apath1", includes, null, true));
        assertTrue(PathMatcher.matches("apath2", includes, null, true));
        assertFalse(PathMatcher.matches("apath1/sub", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub1", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub1/t", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub1/toto/tutu", includes, null, true));
    }

    public void sourcesIncludes() {
        List<String> includes = Arrays.asList("**/*-sources.jar*");
        // File
        assertFalse(PathMatcher.matches("apath/", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/", includes, null, false));
        assertFalse(PathMatcher.matches("apath2/", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/subfolder-longer-than-pattern/", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/sub1/", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/sub1/t/", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/sub1/toto/tutu/", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/toto-sources.jar", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/sub/toto-sources.jar", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/toto-sources.jar.md5", includes, null, false));
        assertTrue(PathMatcher.matches("apath1/sub/toto-sources.jar.md5", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/toto-1.0.jar", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/sub/toto-1.0.jar", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/toto-1.0.jar.md5", includes, null, false));
        assertFalse(PathMatcher.matches("apath1/sub/toto-1.0.jar.md5", includes, null, false));
        // Folder
        assertTrue(PathMatcher.matches("apath/", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/", includes, null, true));
        assertTrue(PathMatcher.matches("apath2/", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/subfolder-longer-than-pattern/", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub1/", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub1/t/", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub1/toto/tutu/", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/toto-sources.jar", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub/toto-sources.jar", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/toto-sources.jar.md5", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub/toto-sources.jar.md5", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/toto-1.0.jar", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub/toto-1.0.jar", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/toto-1.0.jar.md5", includes, null, true));
        assertTrue(PathMatcher.matches("apath1/sub/toto-1.0.jar.md5", includes, null, true));
    }

    public void test() {
        List<String> includes = Arrays.asList("**/*-test.jar");
        List<String> excludes = Arrays.asList();
        assertFalse(PathMatcher.matches("a/", includes, excludes, false));
        assertTrue(PathMatcher.matches("a/", includes, excludes, true));
    }
}
