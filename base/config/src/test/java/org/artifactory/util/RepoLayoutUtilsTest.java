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

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoLayoutBuilder;
import org.artifactory.util.layouts.token.OrganizationPathTokenFilter;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Tests the repository layouts utility methods and constants
 *
 * @author Noam Y. Tenne
 */
@Test
public class RepoLayoutUtilsTest {

    public void testDefaultLayoutNames() {
        testDefaultLayoutName(RepoLayoutUtils.MAVEN_2_DEFAULT_NAME, "maven-2-default");
        testDefaultLayoutName(RepoLayoutUtils.IVY_DEFAULT_NAME, "ivy-default");
        testDefaultLayoutName(RepoLayoutUtils.GRADLE_DEFAULT_NAME, "gradle-default");
        testDefaultLayoutName(RepoLayoutUtils.MAVEN_1_DEFAULT_NAME, "maven-1-default");
    }

    public void testDefaultLayouts() {
        testDefaultLayout(RepoLayoutUtils.MAVEN_2_DEFAULT,
                "[orgPath]/[module]/[baseRev](-[folderItegRev])/" +
                        "[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                true,
                "[orgPath]/[module]/[baseRev](-[folderItegRev])/[module]-[baseRev](-[fileItegRev])(-[classifier]).pom",
                "SNAPSHOT", "SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))");

        testDefaultLayout(RepoLayoutUtils.IVY_DEFAULT,
                "[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]",
                true,
                "[org]/[module]/[baseRev](-[folderItegRev])/[type]s/ivy-[baseRev](-[fileItegRev]).xml",
                "\\d{14}", "\\d{14}");

        testDefaultLayout(RepoLayoutUtils.GRADLE_DEFAULT,
                "[org]/[module]/[baseRev](-[folderItegRev])/[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                true,
                "[org]/[module]/ivy-[baseRev](-[fileItegRev]).xml",
                "\\d{14}", "\\d{14}");

        testDefaultLayout(RepoLayoutUtils.MAVEN_1_DEFAULT,
                "[org]/[type]s/[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]",
                true,
                "[org]/[type]s/[module]-[baseRev](-[fileItegRev]).pom",
                ".+", ".+");
    }

    public void testDefaultTokenValues() {
        assertEquals(RepoLayoutUtils.ORGANIZATION, "org", "Unexpected default 'organization' token value.");
        assertEquals(RepoLayoutUtils.ORGANIZATION_PATH, "orgPath",
                "Unexpected default 'organization path' token value.");
        assertEquals(RepoLayoutUtils.MODULE, "module", "Unexpected default 'module' token value.");
        assertEquals(RepoLayoutUtils.BASE_REVISION, "baseRev", "Unexpected default 'base revision' token value.");
        assertEquals(RepoLayoutUtils.FOLDER_INTEGRATION_REVISION, "folderItegRev",
                "Unexpected default 'folder integration revision' token value.");
        assertEquals(RepoLayoutUtils.FILE_INTEGRATION_REVISION, "fileItegRev",
                "Unexpected default 'file integration revision' token value.");
        assertEquals(RepoLayoutUtils.CLASSIFIER, "classifier", "Unexpected default 'classifier' token value.");
        assertEquals(RepoLayoutUtils.EXT, "ext", "Unexpected default 'extension' token value.");
        assertEquals(RepoLayoutUtils.TYPE, "type", "Unexpected default 'type' token value.");
    }

    public void testDefaultTokenSet() {
        assertEquals(RepoLayoutUtils.TOKENS.size(), 10, "Unexpected size of layout token set.");
        assertTokenSetContents(RepoLayoutUtils.ORGANIZATION, RepoLayoutUtils.ORGANIZATION_PATH,
                RepoLayoutUtils.MODULE, RepoLayoutUtils.BASE_REVISION, RepoLayoutUtils.FOLDER_INTEGRATION_REVISION,
                RepoLayoutUtils.FILE_INTEGRATION_REVISION, RepoLayoutUtils.CLASSIFIER, RepoLayoutUtils.EXT,
                RepoLayoutUtils.TYPE, RepoLayoutUtils.RELEASE);
    }

    public void testDefaultTokenFilterMap() {
        assertEquals(RepoLayoutUtils.TOKEN_FILTERS.size(), 1, "Unexpected size of default token filter map.");
        assertTrue(RepoLayoutUtils.TOKEN_FILTERS.containsKey(RepoLayoutUtils.ORGANIZATION_PATH),
                "Default token filter map should contain a filter for 'orgPath'.");
        assertEquals(RepoLayoutUtils.TOKEN_FILTERS.get(RepoLayoutUtils.ORGANIZATION_PATH),
                OrganizationPathTokenFilter.getInstance(), "Unexpected filter found for 'orgPath'.");
    }

    public void testReservedRepoLayoutNames() {
        testReservedRepoLayoutName(RepoLayoutUtils.MAVEN_2_DEFAULT_NAME);
        testReservedRepoLayoutName(RepoLayoutUtils.IVY_DEFAULT_NAME);
        testReservedRepoLayoutName(RepoLayoutUtils.GRADLE_DEFAULT_NAME);
        testReservedRepoLayoutName(RepoLayoutUtils.MAVEN_1_DEFAULT_NAME);
        assertFalse(RepoLayoutUtils.isReservedName("momo"), "Unexpected reserved layout name.");
    }

    public void testIsDefaultM2Layout() {
        assertTrue(RepoLayoutUtils.isDefaultM2(RepoLayoutUtils.MAVEN_2_DEFAULT), "Default M2 layout isn't recognized.");
        assertFalse(RepoLayoutUtils.isDefaultM2(RepoLayoutUtils.IVY_DEFAULT),
                "Default Ivy layout should not be recognized as default M2.");
        assertFalse(RepoLayoutUtils.isDefaultM2(RepoLayoutUtils.GRADLE_DEFAULT),
                "Default Gradle layout should not be recognized as default M2.");
        assertFalse(RepoLayoutUtils.isDefaultM2(RepoLayoutUtils.MAVEN_1_DEFAULT),
                "Default M1 layout should not be recognized as default M2.");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "(.*)null layout for token existence(.*)")
    public void testNullLayoutContainsOrgPathToken() {
        RepoLayoutUtils.layoutContainsOrgPathToken(null);
    }

    public void testLayoutContainsOrgPathToken() {
        RepoLayoutBuilder repoLayoutBuilder = new RepoLayoutBuilder().artifactPathPattern(null).descriptorPathPattern(
                null);

        assertFalse(RepoLayoutUtils.layoutContainsOrgPathToken(repoLayoutBuilder.build()),
                "Null paths shouldn't contain the 'orgPath' token.");

        repoLayoutBuilder.artifactPathPattern("[orgPath]");
        assertTrue(RepoLayoutUtils.layoutContainsOrgPathToken(repoLayoutBuilder.build()),
                "Expected to find the 'orgPath' token");

        repoLayoutBuilder.artifactPathPattern(null);
        repoLayoutBuilder.distinctiveDescriptorPathPattern(true);
        repoLayoutBuilder.descriptorPathPattern("[orgPath]");
        assertTrue(RepoLayoutUtils.layoutContainsOrgPathToken(repoLayoutBuilder.build()),
                "Expected to find the 'orgPath' token");

        repoLayoutBuilder.artifactPathPattern("[orgPath]");
        assertTrue(RepoLayoutUtils.layoutContainsOrgPathToken(repoLayoutBuilder.build()),
                "Expected to find the 'orgPath' token");
    }

    public void testRemoveReplacedTokenOptionalBrackets() {
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("", false), "");
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("", true), "");

        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org]-momo", false), "[org]-momo");
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org]-momo", true), "[org]-momo");

        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[popo<[^\\].+?>]-momo", false),
                "[popo<[^\\].+?>]-momo");
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[popo<[^\\].+?>]-momo", true),
                "[popo<[^\\].+?>]-momo");

        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org](-[momo])", false), "[org](-[momo])");
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org](-[momo])", true), "[org](-[momo])");

        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org](-[momo<[^\\][.+]>])", false),
                "[org](-[momo<[^\\][.+]>])");
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org](-[momo<[^\\][.+]>])", true),
                "[org](-[momo<[^\\][.+]>])");

        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org](-momo)", false), "[org]-momo");
        assertEquals(RepoLayoutUtils.removeReplacedTokenOptionalBrackets("[org](-momo)", true), "[org]");
    }

    public void testRemoveUnReplacedTokenOptionalBrackets() {
        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets(""), "");

        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets("[org]-momo"), "[org]-momo");

        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets("[org<[^\\](.+)>]-momo"), "[org<[^\\](.+)>]-momo");

        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets("[org](-[momo])"), "[org]");

        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets("[org](-[momo<[^\\][.+]>])"), "[org]");

        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets("[org](-momo)"), "[org](-momo)");

        assertEquals(RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets("[org<[^\\].+>](-momo)"), "[org<[^\\].+>](-momo)");
    }

    public void testGenerateRegExpFromPatternOfDefaultLayouts() {
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.MAVEN_2_DEFAULT.getArtifactPathPattern(),
                "(?<orgPath>.+?)/(?<module>[^/]+)/(?<baseRev>[^/]+?)(?:-(?<folderItegRev>SNAPSHOT))?/" +
                        "(?<module>\\2)-(?<baseRev>\\3)(?:-(?<fileItegRev>SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-" +
                        "(?:[0-9]+))))?(?:-(?<classifier>[^/]+?))?\\.(?<ext>(?:(?!\\d))[^\\-/]+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.MAVEN_2_DEFAULT.getDescriptorPathPattern(),
                "(?<orgPath>.+?)/(?<module>[^/]+)/(?<baseRev>[^/]+?)(?:-(?<folderItegRev>SNAPSHOT))?/" +
                        "(?<module>\\2)-(?<baseRev>\\3)(?:-(?<fileItegRev>SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-" +
                        "(?:[0-9]+))))?(?:-(?<classifier>[^/]+?))?\\.pom");

        testGeneratedPatternRegExp(RepoLayoutUtils.IVY_DEFAULT,
                RepoLayoutUtils.IVY_DEFAULT.getArtifactPathPattern(),
                "(?<org>[^/]+?)/(?<module>[^/]+)/(?<baseRev>[^/]+?)(?:-(?<folderItegRev>\\d{14}))?/" +
                        "(?<type>[^/]+?)s/(?<module>\\2)(?:-(?<classifier>[^/]+?))?-" +
                        "(?<baseRev>\\3)(?:-(?<fileItegRev>\\d{14}))?\\.(?<ext>(?:(?!\\d))[^\\-/]+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.IVY_DEFAULT,
                RepoLayoutUtils.IVY_DEFAULT.getDescriptorPathPattern(),
                "(?<org>[^/]+?)/(?<module>[^/]+)/(?<baseRev>[^/]+?)(?:-(?<folderItegRev>\\d{14}))?/" +
                        "(?<type>[^/]+?)s/ivy-(?<baseRev>\\3)(?:-(?<fileItegRev>\\d{14}))?\\.xml");

        testGeneratedPatternRegExp(RepoLayoutUtils.GRADLE_DEFAULT,
                RepoLayoutUtils.GRADLE_DEFAULT.getArtifactPathPattern(),
                "(?<org>[^/]+?)/(?<module>[^/]+)/(?<baseRev>[^/]+?)(?:-(?<folderItegRev>\\d{14}))?/" +
                        "(?<module>\\2)-(?<baseRev>\\3)(?:-(?<fileItegRev>\\d{14}))?(?:-" +
                        "(?<classifier>[^/]+?))?\\.(?<ext>(?:(?!\\d))[^\\-/]+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.GRADLE_DEFAULT,
                RepoLayoutUtils.GRADLE_DEFAULT.getDescriptorPathPattern(),
                "(?<org>[^/]+?)/(?<module>[^/]+)/ivy-(?<baseRev>[^/]+?)(?:-(?<fileItegRev>\\d{14}))?\\.xml");
        // Test patterns that contain regex special characters
    }

    public void testDefaultTokenRegExpValues() {
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.ORGANIZATION), "(?<org>[^/]+?)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.ORGANIZATION_PATH), "(?<orgPath>.+?)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.MODULE), "(?<module>[^/]+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.BASE_REVISION), "(?<baseRev>[^/]+?)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.CLASSIFIER), "(?<classifier>[^/]+?)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.EXT), "(?<ext>(?:(?!\\d))[^\\-/]+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.TYPE), "(?<type>[^/]+?)");

        //Integration revisions
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FOLDER_INTEGRATION_REVISION),
                "(?<folderItegRev>SNAPSHOT)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FILE_INTEGRATION_REVISION),
                "(?<fileItegRev>SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+)))");
        testGeneratedPatternRegExp(RepoLayoutUtils.IVY_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FOLDER_INTEGRATION_REVISION),
                "(?<folderItegRev>\\d{14})");
        testGeneratedPatternRegExp(RepoLayoutUtils.IVY_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FILE_INTEGRATION_REVISION),
                "(?<fileItegRev>\\d{14})");
        testGeneratedPatternRegExp(RepoLayoutUtils.GRADLE_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FOLDER_INTEGRATION_REVISION),
                "(?<folderItegRev>\\d{14})");
        testGeneratedPatternRegExp(RepoLayoutUtils.GRADLE_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FILE_INTEGRATION_REVISION),
                "(?<fileItegRev>\\d{14})");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_1_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FOLDER_INTEGRATION_REVISION),
                "(?<folderItegRev>.+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_1_DEFAULT,
                RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FILE_INTEGRATION_REVISION), "(?<fileItegRev>.+)");
    }

    public void testCustomTokenRegExpValues() {
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT, "[momo<.+>]", "(?<momo>.+)");
        testGeneratedPatternRegExp(RepoLayoutUtils.IVY_DEFAULT, "[popo<[^/]+?>]", "(?<popo>[^/]+?)");
        testGeneratedPatternRegExp(RepoLayoutUtils.GRADLE_DEFAULT,
                "[moo<SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))>]",
                "(?<moo>SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+)))");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_1_DEFAULT, "[popo<\\d{14}>]", "(?<popo>\\d{14})");
    }

    public void testCustomTokenWithSpecialCharacters() {
        testGeneratedPatternRegExp(RepoLayoutUtils.GRADLE_DEFAULT,
                "^$Blah.special?{yes!}[moo<SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))>]",
                "\\^\\$Blah\\.special\\?\\{yes!\\}(?<moo>SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+)))");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*[momo].*unknown.*")
    public void testFailOnUnknownCustomToken() {
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT, "[momo]", "", true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*[koko].*unknown.*")
    public void testFailCustomTokenWithNoRegEx() {
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT, "[koko<>]", "", true);
    }

    public void testIgnoreUnknownTokens() {
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT, "[momo]", "");
        testGeneratedPatternRegExp(RepoLayoutUtils.MAVEN_2_DEFAULT, "[koko<>]", "");
    }

    public void testTokenHasFilter() {
        testTokenFilterAssociation(RepoLayoutUtils.ORGANIZATION, false);
        testTokenFilterAssociation(RepoLayoutUtils.ORGANIZATION_PATH, true);
        testTokenFilterAssociation(RepoLayoutUtils.MODULE, false);
        testTokenFilterAssociation(RepoLayoutUtils.BASE_REVISION, false);
        testTokenFilterAssociation(RepoLayoutUtils.FOLDER_INTEGRATION_REVISION, false);
        testTokenFilterAssociation(RepoLayoutUtils.FILE_INTEGRATION_REVISION, false);
        testTokenFilterAssociation(RepoLayoutUtils.CLASSIFIER, false);
        testTokenFilterAssociation(RepoLayoutUtils.EXT, false);
        testTokenFilterAssociation(RepoLayoutUtils.TYPE, false);
    }

    public void testGetArtifactLayoutAsIvyPattern() {
        testArtifactLayoutAsIvyPattern(RepoLayoutUtils.MAVEN_2_DEFAULT, "[organization]/[module]/[revision]/" +
                "[module]-[revision](-[classifier]).[ext]");
        testArtifactLayoutAsIvyPattern(RepoLayoutUtils.IVY_DEFAULT, "[organization]/[module]/[revision]/[type]s/" +
                "[module](-[classifier])-[revision].[ext]");
        testArtifactLayoutAsIvyPattern(RepoLayoutUtils.GRADLE_DEFAULT, "[organization]/[module]/[revision]/" +
                "[module]-[revision](-[classifier]).[ext]");
        testArtifactLayoutAsIvyPattern(RepoLayoutUtils.MAVEN_1_DEFAULT, "[organization]/[type]s/[module]-[revision]" +
                "(-[classifier]).[ext]");
    }

    public void testGetDescriptorLayoutAsIvyPattern() {
        testDescriptorLayoutAsIvyPattern(RepoLayoutUtils.MAVEN_2_DEFAULT, "[organization]/[module]/[revision]/" +
                "[module]-[revision](-[classifier]).pom");
        testDescriptorLayoutAsIvyPattern(RepoLayoutUtils.IVY_DEFAULT, "[organization]/[module]/[revision]/[type]s/" +
                "ivy-[revision].xml");
        testDescriptorLayoutAsIvyPattern(RepoLayoutUtils.GRADLE_DEFAULT, "[organization]/[module]/ivy-[revision].xml");
        testDescriptorLayoutAsIvyPattern(RepoLayoutUtils.MAVEN_1_DEFAULT, "[organization]/[type]s/" +
                "[module]-[revision].pom");
    }

    public void testWrapKeywordAsToken() {
        testWrappedKeywords(null, "[null]");
        testWrappedKeywords("", "[]");
        testWrappedKeywords(" ", "[ ]");

        for (String defaultToken : RepoLayoutUtils.TOKENS) {
            testWrappedKeywords(defaultToken, String.format("[%s]", defaultToken));
        }

        testWrappedKeywords("momo", "[momo]");
        testWrappedKeywords("popo", "[popo]");
    }

    public void testLayoutsAreCompatible() {
        RepoLayout[] repoLayouts = {RepoLayoutUtils.MAVEN_2_DEFAULT, RepoLayoutUtils.IVY_DEFAULT,
                RepoLayoutUtils.GRADLE_DEFAULT, RepoLayoutUtils.MAVEN_1_DEFAULT, new RepoLayoutBuilder().
                artifactPathPattern("[org](-[momo<[^\\](.+)>])").descriptorPathPattern("[org](-[momo<[^\\](.+)>])").
                build()};

        for (RepoLayout firstLayout : repoLayouts) {

            for (RepoLayout secondLayout : repoLayouts) {

                boolean layoutsAreCompatible = RepoLayoutUtils.layoutsAreCompatible(firstLayout, secondLayout);
                if (firstLayout.equals(secondLayout)) {
                    assertTrue(layoutsAreCompatible, "Expected layouts to be fully compatible.");
                } else {
                    assertFalse(layoutsAreCompatible, "Expected layouts to be incompatible.");
                }
            }
        }
    }

    private void testDefaultLayoutName(String actualName, String expectedName) {
        assertEquals(actualName, expectedName, "Unexpected default layout name.");
    }

    private void testDefaultLayout(RepoLayout repoLayout, String artifactPathPattern,
            boolean distinctiveDescriptorPathPattern, String descriptorPathPattern,
            String folderIntegrationRevisionRegExp, String fileIntegrationRevisionRegExp) {
        assertEquals(repoLayout.getArtifactPathPattern(), artifactPathPattern,
                "Unexpected default artifact path pattern.");
        assertEquals(repoLayout.isDistinctiveDescriptorPathPattern(), distinctiveDescriptorPathPattern,
                "Unexpected default distinctive descriptor path pattern.");
        assertEquals(repoLayout.getDescriptorPathPattern(), descriptorPathPattern,
                "Unexpected default descriptor path pattern.");
        assertEquals(repoLayout.getFolderIntegrationRevisionRegExp(), folderIntegrationRevisionRegExp,
                "Unexpected default folder integration revision regular expression.");
        assertEquals(repoLayout.getFileIntegrationRevisionRegExp(), fileIntegrationRevisionRegExp,
                "Unexpected default filer integration revision regular expression.");
    }

    private void assertTokenSetContents(String... expectedContents) {
        for (String expectedContent : expectedContents) {
            assertTrue(RepoLayoutUtils.TOKENS.contains(expectedContent),
                    "Default layout token set should contain '" + expectedContent + "'");
        }
    }

    private void testReservedRepoLayoutName(String expectedName) {
        assertTrue(RepoLayoutUtils.isReservedName(expectedName), "'" + expectedName +
                "' should be a reserved layout name.");
    }

    private void testGeneratedPatternRegExp(RepoLayout repoLayout, String pattern, String expectedRegExp) {
        testGeneratedPatternRegExp(repoLayout, pattern, expectedRegExp, false);
    }

    private void testGeneratedPatternRegExp(RepoLayout repoLayout, String pattern, String expectedRegExp,
            boolean failOnUnknownToken) {
        assertEquals(RepoLayoutUtils.generateRegExpFromPattern(repoLayout, pattern, failOnUnknownToken), expectedRegExp,
                "Unexpected converted path pattern regular expression.");
    }

    private void testTokenFilterAssociation(String token, boolean shouldHaveFilter) {
        boolean tokenHasFilter = RepoLayoutUtils.tokenHasFilter(token);
        if (shouldHaveFilter) {
            assertTrue(tokenHasFilter, "Expected token to have a filter.");
        } else {
            assertFalse(tokenHasFilter, "Unexpected token filter.");
        }
    }

    private void testArtifactLayoutAsIvyPattern(RepoLayout repoLayout, String expectedPattern) {
        assertEquals(RepoLayoutUtils.getArtifactLayoutAsIvyPattern(repoLayout), expectedPattern,
                "Unexpected converted ivy pattern.");
    }

    private void testDescriptorLayoutAsIvyPattern(RepoLayout repoLayout, String expectedPattern) {
        assertEquals(RepoLayoutUtils.getDescriptorLayoutAsIvyPattern(repoLayout), expectedPattern,
                "Unexpected converted ivy pattern.");
    }

    private void testWrappedKeywords(String keywordToWrapped, String expectedWrappedValue) {
        assertEquals(RepoLayoutUtils.wrapKeywordAsToken(keywordToWrapped), expectedWrappedValue,
                "Unexpected wrapped token value.");
    }
}
