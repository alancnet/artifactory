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

package org.artifactory.addon.layouts.translate;

import com.google.common.collect.Lists;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.util.Pair;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Holds the cross layout path translation logic
 *
 * @author Noam Y. Tenne
 */
public class PathTranslationHelper {

    private static final Logger log = LoggerFactory.getLogger(PathTranslationHelper.class);

    private TranslatorFilter[] translatorFilters = new TranslatorFilter[]{new ChecksumTranslatorFilter(),
            new MetadataTranslatorFilter()};

    public String translatePath(RepoLayout sourceRepoLayout, RepoLayout targetRepoLayout, String path,
            BasicStatusHolder multiStatusHolder) {

        LinkedList<Pair<String, TranslatorFilter>> filters = Lists.newLinkedList();

        path = filterPaths(path, filters);

        ModuleInfo moduleInfo = null;
        boolean descriptor = true;

        if (sourceRepoLayout.isDistinctiveDescriptorPathPattern()) {
            moduleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(path, sourceRepoLayout);
        }

        if ((moduleInfo == null) || !moduleInfo.isValid()) {
            moduleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(path, sourceRepoLayout);
            descriptor = false;
        }

        if ((moduleInfo == null) || !moduleInfo.isValid()) {
            if (multiStatusHolder != null) {
                multiStatusHolder.warn("Unable to translate path '" + path +
                        "': does not represent a valid module path within the source.", log);
            }

            return applyFilteredContent(path, filters);
        }

        String result;
        ModuleInfo resultModuleInfo;

        /**
         * If mixing between Maven and non-Maven layouts, treat the source descriptor as a normal artifact. Most chances
         * are that the descriptor won't have any meaning. Currently the best compromise
         */
        if (descriptor && !isExclusivelyOneLayoutM2(sourceRepoLayout, targetRepoLayout)) {
            result = ModuleInfoUtils.constructDescriptorPath(moduleInfo, targetRepoLayout, true);
            resultModuleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(result, targetRepoLayout);
        } else {
            result = ModuleInfoUtils.constructArtifactPath(moduleInfo, targetRepoLayout, true);
            resultModuleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(result, targetRepoLayout);
        }

        if (!resultModuleInfo.isValid() && (multiStatusHolder != null)) {
            multiStatusHolder.warn("Translated path '" + path +
                    "', but the result does not represent a valid module path within the target.", log);
        }

        return applyFilteredContent(result, filters);
    }

    private String filterPaths(String path, LinkedList<Pair<String, TranslatorFilter>> filterList) {

        for (TranslatorFilter translatorFilter : translatorFilters) {
            if (translatorFilter.filterRequired(path)) {
                String filteredContent = translatorFilter.getFilteredContent(path);
                path = translatorFilter.stripPath(path);
                filterList.add(new Pair<>(filteredContent, translatorFilter));
            }
        }

        return path;
    }

    private boolean isExclusivelyOneLayoutM2(RepoLayout layoutA, RepoLayout layoutB) {
        boolean sourceIsDefaultM2 = RepoLayoutUtils.isDefaultM2(layoutA);
        boolean targetIsDefaultM2 = RepoLayoutUtils.isDefaultM2(layoutB);

        return ((sourceIsDefaultM2 && !targetIsDefaultM2) || (!sourceIsDefaultM2 && targetIsDefaultM2));
    }

    private String applyFilteredContent(String strippedPath, LinkedList<Pair<String, TranslatorFilter>> filterList) {
        while (filterList.size() != 0) {
            Pair<String, TranslatorFilter> filterPair = filterList.removeLast();
            strippedPath = filterPair.getSecond().applyFilteredContent(strippedPath, filterPair.getFirst());
        }

        return strippedPath;
    }
}
