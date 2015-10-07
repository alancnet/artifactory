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

package org.artifactory.api.module;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.module.regex.NamedMatcher;
import org.artifactory.api.module.regex.NamedPattern;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.util.PathUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.util.layouts.token.BaseTokenFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public abstract class ModuleInfoUtils {

    private static final Logger log = LoggerFactory.getLogger(ModuleInfoUtils.class);

    private ModuleInfoUtils() {
    }

    public static String constructArtifactPath(ModuleInfo moduleInfo, RepoLayout repoLayout) {
        return constructArtifactPath(moduleInfo, repoLayout, true);
    }

    public static String constructArtifactPath(ModuleInfo moduleInfo, RepoLayout repoLayout,
            boolean clearUnReplacedOptionals) {
        return constructItemPath(moduleInfo, repoLayout, false, clearUnReplacedOptionals);
    }

    public static String constructDescriptorPath(ModuleInfo moduleInfo, RepoLayout repoLayout,
            boolean clearUnReplacedOptionals) {
        return constructItemPath(moduleInfo, repoLayout, true, clearUnReplacedOptionals);
    }

    /**
     * Creates a module info object from an <b>artifact</b> path.<br/>
     * <b>NOTE:</b> Unless you've got a really good reason, refrain from using this method directly. Please create a
     * module info using the methods of {@link org.artifactory.repo.Repo} or {@link org.artifactory.api.repo.RepositoryService}
     *
     * @param itemPath   Artifact path (relative to the repository)
     * @param repoLayout Repository layout to use for resolution
     * @return Module info object. When supplied with a path that does not match the layout, validity is unwarranted.
     */
    public static ModuleInfo moduleInfoFromArtifactPath(String itemPath, RepoLayout repoLayout) {
        return moduleInfoFromItemPath(itemPath, repoLayout, false, false);
    }

    public static ModuleInfo moduleInfoFromArtifactPath(String itemPath, RepoLayout repoLayout,
            boolean supportVersionsTokens) {
        ModuleInfo moduleInfo = null;
        if (repoLayout.isDistinctiveDescriptorPathPattern()) {
            moduleInfo = moduleInfoFromItemPath(itemPath, repoLayout, true, supportVersionsTokens);
            if (moduleInfo.isValid() && StringUtils.isBlank(moduleInfo.getExt())) {
                moduleInfo = new ModuleInfoBuilder(moduleInfo).ext(PathUtils.getExtension(itemPath)).build();
            }
        }

        if ((moduleInfo == null) || !moduleInfo.isValid()) {
            moduleInfo = moduleInfoFromItemPath(itemPath, repoLayout, false, supportVersionsTokens);
        }

        return moduleInfo;
    }

    /**
     * Creates a module info object from a <b>descriptor</b> path.<br/>
     * <b>NOTE:</b> Unless you've got a really good reason, refrain from using this method directly. Please create a
     * module info using the methods of {@link org.artifactory.repo.Repo} or {@link org.artifactory.api.repo.RepositoryService}
     *
     * @param itemPath   Descriptor path (relative to the repository)
     * @param repoLayout Repository layout to use for resolution
     * @return Module info object. When supplied with a path that does not match the layout, validity is unwarranted.
     */
    public static ModuleInfo moduleInfoFromDescriptorPath(String itemPath, RepoLayout repoLayout) {
        ModuleInfo moduleInfo = moduleInfoFromItemPath(itemPath, repoLayout, true, false);
        if (repoLayout.isDistinctiveDescriptorPathPattern() && moduleInfo.isValid() &&
                StringUtils.isBlank(moduleInfo.getExt())) {
            return new ModuleInfoBuilder(moduleInfo).ext(PathUtils.getExtension(itemPath)).build();
        }
        return moduleInfo;
    }

    private static String constructItemPath(ModuleInfo moduleInfo, RepoLayout repoLayout, boolean descriptor,
            boolean clearUnReplacedOptionals) {
        if (moduleInfo == null) {
            throw new IllegalArgumentException("Unable to construct a path from a null module info object.");
        }
        if (!moduleInfo.isValid()) {
            throw new IllegalArgumentException("Unable to construct a path from an invalid module info object.");
        }
        if (repoLayout == null) {
            throw new IllegalArgumentException("Unable to construct a path from a null repository layout.");
        }

        String itemPathPattern;
        if (descriptor && repoLayout.isDistinctiveDescriptorPathPattern()) {
            itemPathPattern = repoLayout.getDescriptorPathPattern();
        } else {
            itemPathPattern = repoLayout.getArtifactPathPattern();
        }

        itemPathPattern = replaceDeclaredFields(moduleInfo, itemPathPattern);

        itemPathPattern = RepoLayoutUtils.removeReplacedTokenOptionalBrackets(itemPathPattern, false);

        if (clearUnReplacedOptionals) {
            itemPathPattern = RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets(itemPathPattern);
        }

        return itemPathPattern;
    }

    /**
     * Constructs a path according to a given {@link ModuleInfo} information and a specific repo layout
     * This path can be used as a repoPath for searching artifacts
     *
     * @param moduleInfo the {@link ModuleInfo} information, it must contains at least groupId and artifactId
     * @param repoLayout the {@link RepoLayout} to base the path on
     * @return String path of the gathered information, null in case the given {@link RepoLayout} is invalid,
     *         an invalid repo layout is one that has brackets between the groupId and the moduleId or a bracket before them
     */
    public static String constructBaseItemPath(ModuleInfo moduleInfo, RepoLayout repoLayout) {
        if (moduleInfo == null) {
            throw new IllegalArgumentException("Unable to construct a path from a null module info object.");
        }
        if (repoLayout == null) {
            throw new IllegalArgumentException("Unable to construct a path from a null repository layout.");
        }

        String itemPathPattern = repoLayout.getArtifactPathPattern();
        itemPathPattern = replaceDeclaredFields(moduleInfo, itemPathPattern);
        itemPathPattern = RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets(itemPathPattern);
        itemPathPattern = verifyAndBuildPathPattern(itemPathPattern);
        if (StringUtils.isBlank(itemPathPattern)) {
            log.warn("The repo layout '{}' is invalid and cannot be parsed for searching versions.",
                    repoLayout.getName());
        }

        return itemPathPattern;
    }

    /**
     * Constructs a module version according to the given {@link ModuleInfo} details and the {@link RepoLayout}
     * Examples: '1.4.1-20120319.160615-2', '1.0-SNAPSHOT', '1.2'
     *
     * @param moduleInfo Contains the module details (base revision, file integration revision..)
     * @param repoLayout The repo layout to calculate the module version from
     * @return A module version string
     */
    public static String constructModuleVersion(ModuleInfo moduleInfo, RepoLayout repoLayout) {
        if (moduleInfo == null) {
            throw new IllegalArgumentException("Unable to construct a path from a null module info object.");
        }
        if (repoLayout == null) {
            throw new IllegalArgumentException("Unable to construct a path from a null repository layout.");
        }

        String itemPathPattern = repoLayout.getArtifactPathPattern();
        itemPathPattern = getModuleVersionPattern(itemPathPattern);
        if (!StringUtils.isBlank(itemPathPattern)) {
            itemPathPattern = replaceDeclaredFields(moduleInfo, itemPathPattern);
            itemPathPattern = RepoLayoutUtils.removeReplacedTokenOptionalBrackets(itemPathPattern, false);
            itemPathPattern = RepoLayoutUtils.removeUnReplacedTokenOptionalBrackets(itemPathPattern);
        } else {
            log.warn("The repo layout '{}' is invalid and cannot be parsed for version file name",
                    repoLayout.getName());
        }

        return itemPathPattern;
    }

    /**
     * Constructs the module version pattern, starting from [baseRev] to [fileItegRev]
     *
     * @param itemPathPattern the item path containing all the tokens of a given repo layout
     * @return the tokens path representing the file name, empty string ("") in case of unsupported layout,
     *         Unsupported layout is one which [fileItegRev] is before [baseRev]
     */
    private static String getModuleVersionPattern(String itemPathPattern) {
        String wrappedBaseRevisionToken = RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.BASE_REVISION);
        String wrappedFileItegRevToken = RepoLayoutUtils.wrapKeywordAsToken(RepoLayoutUtils.FILE_INTEGRATION_REVISION);
        int baseRevStartPos = StringUtils.lastIndexOf(itemPathPattern, wrappedBaseRevisionToken);
        int fileItegRevEndPos = StringUtils.lastIndexOf(itemPathPattern, wrappedFileItegRevToken);
        int indexOfClosingOptionalBracket = StringUtils.indexOf(itemPathPattern, ")", fileItegRevEndPos);
        if (indexOfClosingOptionalBracket > 0) {
            fileItegRevEndPos = indexOfClosingOptionalBracket + 1;
        }
        if (fileItegRevEndPos >= baseRevStartPos) {
            return StringUtils.substring(itemPathPattern, baseRevStartPos, fileItegRevEndPos);
        }

        return "";
    }

    /**
     * Verifies the given path is legit and removes the remaining brackets from it
     *
     * @param itemPathPattern the item path to verify
     * @return the repo path without any brackets tokens in it, null in case the repo path is invalid
     *         an invalid repo path is one that has brackets between the groupId and the moduleId or a bracket before them
     */
    private static String verifyAndBuildPathPattern(String itemPathPattern) {
        String[] tokens = StringUtils.split(itemPathPattern, "/");
        StringBuilder newPathBuilder = new StringBuilder();
        boolean foundBracketToken = false;
        for (String token : tokens) {
            if (StringUtils.containsNone(token, new char[]{'[', ']'})) {
                if (foundBracketToken) {
                    // Invalid repo path, found a bracket between the groupId and the moduleId
                    // or a bracket before them, returning null
                    return null;
                }
                newPathBuilder.append(token).append("/");
                foundBracketToken = false;
            } else {
                foundBracketToken = true;
            }
        }

        return newPathBuilder.toString();
    }

    /**
     * Insert all the given module info values to the pattern
     *
     * @param moduleInfo       Module info to use
     * @param itemPathTemplate Item path template to fill
     * @return Modified item path template
     */
    private static String replaceDeclaredFields(ModuleInfo moduleInfo, String itemPathTemplate) {
        Set<String> tokens = Sets.newHashSet(RepoLayoutUtils.TOKENS);
        Map<String, String> customFields = moduleInfo.getCustomFields();
        if (customFields != null) {
            itemPathTemplate = RepoLayoutUtils.clearCustomTokenRegEx(itemPathTemplate);
            tokens.addAll(customFields.keySet());
        }

        for (String token : tokens) {
            String wrappedToken = RepoLayoutUtils.wrapKeywordAsToken(token);

            if (StringUtils.contains(itemPathTemplate, wrappedToken)) {

                String tokenValue = getTokenValue(moduleInfo, token);

                if (RepoLayoutUtils.TYPE.equals(token) && StringUtils.isBlank(tokenValue)) {
                    tokenValue = moduleInfo.getExt();
                }

                if (StringUtils.isNotBlank(tokenValue)) {

                    if (RepoLayoutUtils.tokenHasFilter(token)) {
                        BaseTokenFilter tokenFilter = RepoLayoutUtils.TOKEN_FILTERS.get(token);
                        if (tokenFilter != null) {
                            tokenValue = tokenFilter.forPath(tokenValue);
                        }
                    }

                    itemPathTemplate = StringUtils.replace(itemPathTemplate, wrappedToken, tokenValue);
                }
            }
        }
        return itemPathTemplate;
    }

    private static String getTokenValue(ModuleInfo moduleInfo, String tokenName) {
        if (RepoLayoutUtils.ORGANIZATION.equals(tokenName)) {
            return moduleInfo.getOrganization();
        } else if (RepoLayoutUtils.ORGANIZATION_PATH.equals(tokenName)) {
            return moduleInfo.getOrganization();
        } else if (RepoLayoutUtils.MODULE.equals(tokenName)) {
            return moduleInfo.getModule();
        } else if (RepoLayoutUtils.BASE_REVISION.equals(tokenName)) {
            return moduleInfo.getBaseRevision();
        } else if (RepoLayoutUtils.FOLDER_INTEGRATION_REVISION.equals(tokenName)) {
            return moduleInfo.getFolderIntegrationRevision();
        } else if (RepoLayoutUtils.FILE_INTEGRATION_REVISION.equals(tokenName)) {
            return moduleInfo.getFileIntegrationRevision();
        } else if (RepoLayoutUtils.CLASSIFIER.equals(tokenName)) {
            return moduleInfo.getClassifier();
        } else if (RepoLayoutUtils.EXT.equals(tokenName)) {
            return moduleInfo.getExt();
        } else if (RepoLayoutUtils.TYPE.equals(tokenName)) {
            return moduleInfo.getType();
        } else {
            return moduleInfo.getCustomField(tokenName);
        }
    }

    private static ModuleInfo moduleInfoFromItemPath(String itemPath, RepoLayout repoLayout, boolean descriptor,
            boolean supportVersionsTokens) {
        if (StringUtils.isBlank(itemPath)) {
            throw new IllegalArgumentException("Cannot construct a module info object from a blank item path.");
        }
        if (repoLayout == null) {
            throw new IllegalArgumentException("Cannot construct a module info object from a null repository layout.");
        }

        String pattern;
        if (descriptor && repoLayout.isDistinctiveDescriptorPathPattern()) {
            pattern = repoLayout.getDescriptorPathPattern();
        } else {
            pattern = repoLayout.getArtifactPathPattern();
        }

        return moduleInfoFromPattern(itemPath, repoLayout, pattern, supportVersionsTokens);
    }

    private static ModuleInfo moduleInfoFromPattern(String itemPath, RepoLayout repoLayout, String pattern,
            boolean supportVersionsTokens) {
        String itemPathPatternRegExp = RepoLayoutUtils.generateRegExpFromPattern(repoLayout, pattern, false,
                supportVersionsTokens);
        //TODO: [by yl] Cache the patterns
        NamedPattern itemPathRegExPattern = NamedPattern.compile(itemPathPatternRegExp);
        NamedMatcher itemPathMatcher = itemPathRegExPattern.matcher(itemPath);
        ModuleInfo moduleInfo;
        if (!itemPathMatcher.matches()) {
            moduleInfo = new ModuleInfo();
        } else {
            moduleInfo = createModuleInfo(itemPathMatcher);
        }
        if (!moduleInfo.isValid()) {
            log.debug("Could not transform {} to a valid module info.", itemPath);
        }
        return moduleInfo;
    }

    private static ModuleInfo createModuleInfo(NamedMatcher itemPathMatcher) {
        Map<String, String> namedGroups = itemPathMatcher.namedGroups();
        ModuleInfoBuilder moduleInfoBuilder = new ModuleInfoBuilder();
        for (Map.Entry<String, String> namedGroupEntry : namedGroups.entrySet()) {
            String groupName = namedGroupEntry.getKey();
            String groupValue = namedGroupEntry.getValue();

            /**
             * Check if the group name (token name) has a filter registered to it before checking for token aliases
             * since the filter might be registered to the alias
             */
            if (RepoLayoutUtils.tokenHasFilter(groupName)) {
                BaseTokenFilter tokenFilter = RepoLayoutUtils.TOKEN_FILTERS.get(groupName);
                if (tokenFilter != null) {
                    groupValue = tokenFilter.fromPath(groupValue);
                }
            }
            setTokenValue(moduleInfoBuilder, groupName, groupValue);
        }

        return moduleInfoBuilder.build();
    }

    private static void setTokenValue(ModuleInfoBuilder moduleInfoBuilder, String tokenName, String tokenValue) {
        if (RepoLayoutUtils.ORGANIZATION.equals(tokenName)) {
            moduleInfoBuilder.organization(tokenValue);
        } else if (RepoLayoutUtils.ORGANIZATION_PATH.equals(tokenName)) {
            moduleInfoBuilder.organization(tokenValue);
        } else if (RepoLayoutUtils.MODULE.equals(tokenName)) {
            moduleInfoBuilder.module(tokenValue);
        } else if (RepoLayoutUtils.BASE_REVISION.equals(tokenName)) {
            moduleInfoBuilder.baseRevision(tokenValue);
        } else if (RepoLayoutUtils.FOLDER_INTEGRATION_REVISION.equals(tokenName)) {
            moduleInfoBuilder.folderIntegrationRevision(tokenValue);
        } else if (RepoLayoutUtils.FILE_INTEGRATION_REVISION.equals(tokenName)) {
            moduleInfoBuilder.fileIntegrationRevision(tokenValue);
        } else if (RepoLayoutUtils.CLASSIFIER.equals(tokenName)) {
            moduleInfoBuilder.classifier(tokenValue);
        } else if (RepoLayoutUtils.EXT.equals(tokenName)) {
            moduleInfoBuilder.ext(tokenValue);
        } else if (RepoLayoutUtils.TYPE.equals(tokenName)) {
            moduleInfoBuilder.type(tokenValue);
        } else {
            moduleInfoBuilder.customField(tokenName, tokenValue);
        }
    }
}
