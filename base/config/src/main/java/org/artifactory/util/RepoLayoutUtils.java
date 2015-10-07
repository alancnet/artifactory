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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoLayoutBuilder;
import org.artifactory.util.layouts.token.BaseTokenFilter;
import org.artifactory.util.layouts.token.OrganizationPathTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Noam Y. Tenne
 */
public abstract class RepoLayoutUtils {
    private static final Logger log = LoggerFactory.getLogger(RepoLayoutUtils.class);

    /**
     * LAYOUT PRESETS
     */
    public static final String MAVEN_2_DEFAULT_NAME = "maven-2-default";
    public static final String MAVEN_1_DEFAULT_NAME = "maven-1-default";
    public static final String IVY_DEFAULT_NAME = "ivy-default";
    public static final String GRADLE_DEFAULT_NAME = "gradle-default";
    public static final String NUGET_DEFAULT_NAME = "nuget-default";
    public static final String SBT_DEFAULT_NAME = "sbt-default";
    public static final String NPM_DEFAULT_NAME = "npm-default";
    public static final String BOWER_DEFAULT_NAME = "bower-default";
    public static final String VSC_DEFAULT_NAME = "vcs-default";
    public static final String SIMPLE_DEFAULT_NAME = "simple-default";

    public static List<String> DEFAULT_LAYOUTS = new LinkedList<String>() {{
        add(MAVEN_2_DEFAULT_NAME);
        add(MAVEN_1_DEFAULT_NAME);
        add(IVY_DEFAULT_NAME);
        add(GRADLE_DEFAULT_NAME);
        add(NUGET_DEFAULT_NAME);
        add(SBT_DEFAULT_NAME);
        add(NPM_DEFAULT_NAME);
        add(BOWER_DEFAULT_NAME);
        add(VSC_DEFAULT_NAME);
        add(SIMPLE_DEFAULT_NAME);
    }};

    public static final RepoLayout MAVEN_2_DEFAULT = new RepoLayoutBuilder()
            .name(MAVEN_2_DEFAULT_NAME)
            .artifactPathPattern("[orgPath]/[module]/[baseRev](-[folderItegRev])/" +
                    "[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]")
            .distinctiveDescriptorPathPattern(true)
            .descriptorPathPattern("[orgPath]/[module]/[baseRev](-[folderItegRev])/" +
                    "[module]-[baseRev](-[fileItegRev])(-[classifier]).pom")
            .folderIntegrationRevisionRegExp("SNAPSHOT")
            .fileIntegrationRevisionRegExp("SNAPSHOT|(?:(?:[0-9]{8}.[0-9]{6})-(?:[0-9]+))")
            .build();
    public static final RepoLayout IVY_DEFAULT = new RepoLayoutBuilder()
            .name(IVY_DEFAULT_NAME)
            .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                    "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
            .distinctiveDescriptorPathPattern(true)
            .descriptorPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                    "ivy-[baseRev](-[fileItegRev]).xml")
            .folderIntegrationRevisionRegExp("\\d{14}")
            .fileIntegrationRevisionRegExp("\\d{14}")
            .build();
    public static final RepoLayout GRADLE_DEFAULT = new RepoLayoutBuilder()
            .name(GRADLE_DEFAULT_NAME)
            .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/" +
                    "[module]-[baseRev](-[fileItegRev])(-[classifier]).[ext]")
            .distinctiveDescriptorPathPattern(true)
            .descriptorPathPattern("[org]/[module]/ivy-[baseRev](-[fileItegRev]).xml")
            .folderIntegrationRevisionRegExp("\\d{14}")
            .fileIntegrationRevisionRegExp("\\d{14}")
            .build();
    public static final RepoLayout MAVEN_1_DEFAULT = new RepoLayoutBuilder()
            .name(MAVEN_1_DEFAULT_NAME)
            .artifactPathPattern("[org]/[type]s/[module]-[baseRev](-[fileItegRev])" +
                    "(-[classifier]).[ext]")
            .distinctiveDescriptorPathPattern(true)
            .descriptorPathPattern("[org]/[type]s/[module]-[baseRev](-[fileItegRev]).pom")
            .folderIntegrationRevisionRegExp(".+")
            .fileIntegrationRevisionRegExp(".+")
            .build();

    /**
     * LAYOUT TOKENS
     */
    public static final String ORGANIZATION = "org";
    public static final String ORGANIZATION_PATH = "orgPath";
    public static final String MODULE = "module";
    public static final String BASE_REVISION = "baseRev";
    public static final String FOLDER_INTEGRATION_REVISION = "folderItegRev";
    public static final String FILE_INTEGRATION_REVISION = "fileItegRev";
    public static final String CLASSIFIER = "classifier";
    public static final String EXT = "ext";
    public static final String TYPE = "type";
    public static final String RELEASE = "RELEASE";

    public static final Set<String> TOKENS = Sets.newHashSet(ORGANIZATION, ORGANIZATION_PATH, MODULE, BASE_REVISION,
            FOLDER_INTEGRATION_REVISION, FILE_INTEGRATION_REVISION, CLASSIFIER, EXT, TYPE, RELEASE);

    public static final Map<String, BaseTokenFilter> TOKEN_FILTERS;

    private static final Set<Character> REGEX_SPECIAL_TOKENS = Sets.newHashSet('.', '+', '?', '*', '{', '}', '^', '$');
    private static final Pattern OPTIONAL_AREA_PATTERN = Pattern.compile("\\([^\\(]*\\)");
    private static final Pattern REPLACED_OPTIONAL_TOKEN_PATTERN = Pattern.compile("\\([^\\[\\(]*\\)");
    private static final Pattern CUSTOM_TOKEN_PATTERN = Pattern.compile("<[^<]*>");

    static {
        Map<String, BaseTokenFilter> temp = Maps.newHashMap();
        temp.put(RepoLayoutUtils.ORGANIZATION_PATH, OrganizationPathTokenFilter.getInstance());
        TOKEN_FILTERS = Collections.unmodifiableMap(temp);
    }

    private RepoLayoutUtils() {
    }

    public static boolean isReservedName(String layoutName) {
        return DEFAULT_LAYOUTS.contains(layoutName);
    }

    public static boolean isDefaultM2(RepoLayout repoLayout) {
        return MAVEN_2_DEFAULT.equals(repoLayout);
    }

    public static boolean isDefaultSimple(RepoLayout repoLayout) {
        if (repoLayout == null) {
            return false;
        }
        String name = repoLayout.getName();
        return StringUtils.equals(name, "simple-default") || StringUtils.equals(name, "art-simple-default");
    }

    public static boolean isDefaultIvy(RepoLayout repoLayout) {
        return IVY_DEFAULT.equals(repoLayout);
    }

    public static boolean isDefaultGradle(RepoLayout repoLayout) {
        return GRADLE_DEFAULT.equals(repoLayout);
    }

    /**
     * Indicates whether the given layout contains the orgPath token, equal to Ivy's M2 compatibility,
     *
     * @param repoLayout Layout to check
     * @return True if the layout contains the orgPath token
     */
    public static boolean layoutContainsOrgPathToken(RepoLayout repoLayout) {
        if (repoLayout == null) {
            throw new IllegalArgumentException("Cannot check a null layout for token existence.");
        }

        String artifactPathPattern = repoLayout.getArtifactPathPattern();
        String descriptorPathPattern = repoLayout.getDescriptorPathPattern();

        return ((artifactPathPattern != null) && artifactPathPattern.contains(ORGANIZATION_PATH)) ||
                (repoLayout.isDistinctiveDescriptorPathPattern() && (descriptorPathPattern != null) &&
                        descriptorPathPattern.contains(ORGANIZATION_PATH));
    }

    /**
     * Find all optional areas that their token values were provided and remove the "optional" brackets that surround
     * them.
     *
     * @param itemPathTemplate     Item path template to modify
     * @param removeBracketContent True if the content of the optional bracket should be disposed
     * @return Modified item path template
     */
    public static String removeReplacedTokenOptionalBrackets(String itemPathTemplate, boolean removeBracketContent) {
        Matcher matcher = REPLACED_OPTIONAL_TOKEN_PATTERN.matcher(itemPathTemplate);

        int latestGroupEnd = 0;
        StringBuilder newPathBuilder = new StringBuilder();

        while (matcher.find()) {
            int replacedOptionalTokenAreaStart = matcher.start();
            int replacedOptionalTokenAreaEnd = matcher.end();
            String replacedOptionalTokenValue = matcher.group(0);

            newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd, replacedOptionalTokenAreaStart));

            if (!removeBracketContent) {
                newPathBuilder.append(replacedOptionalTokenValue.replaceAll("[\\(\\)]", ""));
            }

            //Path after optional area
            latestGroupEnd = replacedOptionalTokenAreaEnd;
        }

        if ((latestGroupEnd != 0) && latestGroupEnd < itemPathTemplate.length()) {
            newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd));
        }

        if (newPathBuilder.length() == 0) {
            return itemPathTemplate;
        }

        return newPathBuilder.toString();
    }

    /**
     * Find all remaining optional areas that were left with un-replaced tokens and remove them completely
     *
     * @param itemPathTemplate Item path template to modify
     * @return Modified item path template
     */
    public static String removeUnReplacedTokenOptionalBrackets(String itemPathTemplate) {
        Matcher matcher = OPTIONAL_AREA_PATTERN.matcher(itemPathTemplate);

        int latestGroupEnd = 0;
        StringBuilder newPathBuilder = new StringBuilder();

        while (matcher.find()) {
            int optionalAreaStart = matcher.start();
            int optionalAreaEnd = matcher.end();
            String optionalAreaValue = matcher.group(0);

            if (optionalAreaValue.contains("[")) {
                newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd, optionalAreaStart));
                latestGroupEnd = optionalAreaEnd;
            }
        }

        if ((latestGroupEnd != 0) && latestGroupEnd < itemPathTemplate.length()) {
            newPathBuilder.append(itemPathTemplate.substring(latestGroupEnd));
        }

        if (newPathBuilder.length() == 0) {
            return itemPathTemplate;
        }

        return newPathBuilder.toString();
    }

    /**
     * Creates a regular expression based on the given path pattern and layout
     *
     * @param repoLayout   Repo layout to target
     * @param patternToUse Pattern to translate
     * @return Regular expression of given path
     */
    public static String generateRegExpFromPattern(RepoLayout repoLayout, String patternToUse) {
        return generateRegExpFromPattern(repoLayout, patternToUse, false, false);
    }

    /**
     * Creates a regular expression based on the given path pattern and layout
     * the pattern may contain version tokens ([RELEASE] or [INTEGRATION])
     *
     * @param repoLayout         Repo layout to target
     * @param patternToUse       Pattern to translate
     * @param failOnUnknownToken Throw exception if the pattern contains an unknown token (neither reserved nor custom)
     * @return Regular expression of given path
     */
    public static String generateRegExpFromPattern(RepoLayout repoLayout, String patternToUse,
            boolean failOnUnknownToken) {
        return generateRegExpFromPattern(repoLayout, patternToUse, failOnUnknownToken, false);
    }

    /**
     * Creates a regular expression based on the given path pattern and layout
     *
     * @param repoLayout         Repo layout to target
     * @param patternToUse       Pattern to translate
     * @param failOnUnknownToken Throw exception if the pattern contains an unknown token (neither reserved nor custom)
     * @param hasVersionTokens   indicates if the pattern contains version tokens
     * @return Regular expression of given path
     */
    public static String generateRegExpFromPattern(RepoLayout repoLayout, String patternToUse,
            boolean failOnUnknownToken, boolean hasVersionTokens) {
        List<String> tokenAppearance = Lists.newArrayList();
        StringBuilder itemPathPatternRegExpBuilder = new StringBuilder();

        boolean withinToken = false;
        boolean withinCustomToken = false;
        StringBuilder currentTokenBuilder = new StringBuilder();
        StringBuilder customRegExTokenBuilder = new StringBuilder();
        for (char c : patternToUse.toCharArray()) {
            if (('[' == c) && !withinToken && !withinCustomToken) {
                withinToken = true;
            } else if ((']' == c) && withinToken && !withinCustomToken) {
                withinToken = false;
                String currentToken = currentTokenBuilder.toString();
                currentTokenBuilder.delete(0, currentTokenBuilder.length());
                if (isReservedToken(currentToken)) {
                    appendToken(itemPathPatternRegExpBuilder, currentToken, tokenAppearance,
                            getTokenRegExp(currentToken, repoLayout, hasVersionTokens));
                } else if (customRegExTokenBuilder.length() != 0) {
                    appendToken(itemPathPatternRegExpBuilder, currentToken, tokenAppearance,
                            customRegExTokenBuilder.toString());
                    customRegExTokenBuilder.delete(0, customRegExTokenBuilder.length());
                } else {
                    String errorMessage = "The token '[" + currentToken + "]' is unknown. If this is not intended, " +
                            "please verify the token name for correctness or add a mapping for this token using the " +
                            "'[$NAME&lt;REGEXP&gt;]' syntax.";
                    if (log.isDebugEnabled()) {
                        log.debug("Error occurred while generating regular expressions from the repository layout " +
                                "pattern '{}': {}", patternToUse, errorMessage);
                    }
                    if (failOnUnknownToken) {
                        throw new IllegalArgumentException(errorMessage);
                    }
                }
            } else if ('<' == c) {
                withinCustomToken = true;
            } else if ('>' == c) {
                withinCustomToken = false;
            } else if (withinCustomToken) {
                customRegExTokenBuilder.append(c);
            } else if (!withinToken) {
                appendNonReservedToken(itemPathPatternRegExpBuilder, Character.toString(c));
            } else {
                currentTokenBuilder.append(c);
            }
        }
        return itemPathPatternRegExpBuilder.toString();
    }

    /**
     * Indicates whether the given token has a value filter assigned to it
     *
     * @param tokenName Name of token check
     * @return True if the token relies on a filter
     */
    public static boolean tokenHasFilter(String tokenName) {
        return TOKEN_FILTERS.containsKey(tokenName);
    }

    /**
     * Returns the Ivy pattern representation of the layout's artifact patten
     *
     * @param repoLayout Layout to "translate"
     * @return Ivy pattern
     */
    public static String getArtifactLayoutAsIvyPattern(RepoLayout repoLayout) {
        return getItemLayoutAsIvyPattern(repoLayout, false);
    }

    /**
     * Returns the Ivy pattern representation of the layout's descriptor patten
     *
     * @param repoLayout Layout to "translate"
     * @return Ivy pattern
     */
    public static String getDescriptorLayoutAsIvyPattern(RepoLayout repoLayout) {
        return getItemLayoutAsIvyPattern(repoLayout, true);
    }

    /**
     * Wraps the given keyword with the token parentheses ('[', ']')
     *
     * @param keyword Keyword to wrap
     * @return Wrapped keyword
     */
    public static String wrapKeywordAsToken(String keyword) {
        return "[" + keyword + "]";
    }

    /**
     * Indicates whether the compared layouts are fully compatible (don't miss any tokens when crossed)
     *
     * @param first  Layout to compare
     * @param second Layout to compare
     * @return True if no tokens are missed between the layouts
     */
    public static boolean layoutsAreCompatible(RepoLayout first, RepoLayout second) {
        String firstArtifactPathPattern = first.getArtifactPathPattern();
        String secondArtifactPathPattern = second.getArtifactPathPattern();

        if (foundMissingTokens(firstArtifactPathPattern, secondArtifactPathPattern)) {
            return false;
        }
        if (foundMissingTokens(secondArtifactPathPattern, firstArtifactPathPattern)) {
            return false;
        }

        if (first.isDistinctiveDescriptorPathPattern() && second.isDistinctiveDescriptorPathPattern()) {

            String firstDescriptorPathPattern = first.getDescriptorPathPattern();
            String secondDescriptorPathPattern = second.getDescriptorPathPattern();

            if (foundMissingTokens(firstDescriptorPathPattern, secondDescriptorPathPattern)) {
                return false;
            }
            if (foundMissingTokens(secondDescriptorPathPattern, firstDescriptorPathPattern)) {
                return false;
            }
        }
        return true;
    }

    public static String clearCustomTokenRegEx(String path) {
        return CUSTOM_TOKEN_PATTERN.matcher(path).replaceAll("");
    }

    private static void appendNonReservedToken(StringBuilder itemPathPatternRegExpBuilder,
            String itemPathPatternElement) {
        char[] splitPathPatternElement = itemPathPatternElement.toCharArray();
        for (char elementToken : splitPathPatternElement) {
            // Escaping special regex characters
            if (REGEX_SPECIAL_TOKENS.contains(elementToken)) {
                itemPathPatternRegExpBuilder.append("\\");
            }

            itemPathPatternRegExpBuilder.append(elementToken);

            if ('(' == elementToken) {
                itemPathPatternRegExpBuilder.append("?:");
            }

            //Append the '?' character to the end of the parenthesis - optional group
            if (')' == elementToken) {
                itemPathPatternRegExpBuilder.append("?");
            }
        }
    }

    private static boolean isReservedToken(String pathElement) {
        return TOKENS.contains(pathElement);
    }

    private static String getTokenRegExp(String tokenName, RepoLayout repoLayout, boolean hasVersionTokens) {
        if (ORGANIZATION.equals(tokenName)) {
            return "[^/]+?";
        } else if (ORGANIZATION_PATH.equals(tokenName)) {
            return ".+?";
        } else if (MODULE.equals(tokenName)) {
            return "[^/]+";
        } else if (BASE_REVISION.equals(tokenName)) {
            return "[^/]+?";
        } else if (FOLDER_INTEGRATION_REVISION.equals(tokenName)) {
            String regExp = repoLayout.getFolderIntegrationRevisionRegExp();
            if (hasVersionTokens) {
                regExp = regExp + "|\\[INTEGRATION\\]" + "|\\[RELEASE\\]";
            }
            return regExp;
        } else if (FILE_INTEGRATION_REVISION.equals(tokenName)) {
            String regExp = repoLayout.getFileIntegrationRevisionRegExp();
            if (hasVersionTokens) {
                regExp = regExp + "|\\[INTEGRATION\\]" + "|\\[RELEASE\\]";
            }
            return regExp;
        } else if (CLASSIFIER.equals(tokenName)) {
            return "[^/]+?";
        } else if (EXT.equals(tokenName)) {
            return "(?:(?!\\d))[^\\-/]+";
        } else if (TYPE.equals(tokenName)) {
            return "[^/]+?";
        } else if (hasVersionTokens && RELEASE.equals(tokenName)) {
            return "[^/]+?";
        }
        return null;
    }

    private static String getItemLayoutAsIvyPattern(RepoLayout repoLayout, boolean descriptor) {
        if (repoLayout == null) {
            throw new IllegalArgumentException("Cannot translate a null layout.");
        }

        String layoutToTranslate;

        if (descriptor && repoLayout.isDistinctiveDescriptorPathPattern()) {
            layoutToTranslate = repoLayout.getDescriptorPathPattern();
        } else {
            layoutToTranslate = repoLayout.getArtifactPathPattern();
        }

        String organizationToken = wrapKeywordAsToken("organization");
        layoutToTranslate = layoutToTranslate.replaceAll("\\[" + ORGANIZATION_PATH + "\\]", organizationToken);
        layoutToTranslate = layoutToTranslate.replaceAll("\\[" + ORGANIZATION + "\\]", organizationToken);
        layoutToTranslate = layoutToTranslate.replaceAll("\\[" + BASE_REVISION + "\\]", wrapKeywordAsToken("revision"));
        layoutToTranslate = layoutToTranslate.replaceAll("\\[" + FOLDER_INTEGRATION_REVISION + "\\]", "");
        layoutToTranslate = layoutToTranslate.replaceAll("\\[" + FILE_INTEGRATION_REVISION + "\\]", "");
        layoutToTranslate = removeReplacedTokenOptionalBrackets(layoutToTranslate, true);

        return layoutToTranslate;
    }

    private static boolean foundMissingTokens(String firstPattern, String secondPattern) {
        boolean withinToken = false;
        boolean withinCustomToken = false;
        StringBuilder currentTokenValue = new StringBuilder();
        for (char c : firstPattern.toCharArray()) {
            if (('[' == c) && !withinCustomToken) {
                currentTokenValue.append("[");
                withinToken = true;
            } else if ((']' == c) && !withinCustomToken) {
                withinToken = false;
                currentTokenValue.append("]");
                String currentToken = currentTokenValue.toString();
                currentTokenValue.delete(0, currentTokenValue.length());
                if (!secondPattern.contains(currentToken)) {

                    /**
                     * If the unfound token is orgPath but org is found, or the opposite, don't consider as missing,
                     * they are interchangeable
                     */
                    if ((wrapKeywordAsToken(ORGANIZATION_PATH).equals(currentToken) &&
                            secondPattern.contains(wrapKeywordAsToken(ORGANIZATION))) ||
                            (wrapKeywordAsToken(ORGANIZATION).equals(currentToken) &&
                                    secondPattern.contains(wrapKeywordAsToken(ORGANIZATION_PATH)))) {
                        continue;
                    }
                    return true;
                }
            } else if (('<' == c) && withinToken) {
                withinCustomToken = true;
                currentTokenValue.append("<");
            } else if (('>' == c) && withinToken) {
                withinCustomToken = false;
                currentTokenValue.append(">");
            } else if (withinToken) {
                currentTokenValue.append(c);
            }
        }

        return false;
    }

    private static void appendToken(StringBuilder itemPathPatternRegExpBuilder, String tokenName,
            List<String> tokenAppearance, String tokenValue) {
        itemPathPatternRegExpBuilder.append("(?<").append(tokenName).append(">");
        if (tokenAppearance.contains(tokenName)) {
            itemPathPatternRegExpBuilder.append("\\").append(tokenAppearance.indexOf(tokenName) + 1);
        } else {
            itemPathPatternRegExpBuilder.append(tokenValue);
            tokenAppearance.add(tokenName);
        }
        itemPathPatternRegExpBuilder.append(")");
    }
}
