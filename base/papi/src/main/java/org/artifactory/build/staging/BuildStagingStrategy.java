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

package org.artifactory.build.staging;

import org.artifactory.build.promotion.PromotionConfig;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public class BuildStagingStrategy implements Serializable {

    private Map<String, ModuleVersion> moduleVersionsMap;
    private ModuleVersion defaultModuleVersion;
    private VcsConfig vcsConfig;
    private PromotionConfig promotionConfig;

    @Nullable
    public Map<String, ModuleVersion> getModuleVersionsMap() {
        return moduleVersionsMap;
    }

    public void setModuleVersionsMap(@Nullable Map<String, ModuleVersion> moduleVersionsMap) {
        this.moduleVersionsMap = moduleVersionsMap;
    }

    @Nullable
    public ModuleVersion getDefaultModuleVersion() {
        return defaultModuleVersion;
    }

    public void setDefaultModuleVersion(@Nullable ModuleVersion defaultModuleVersion) {
        this.defaultModuleVersion = defaultModuleVersion;
    }

    @Nullable
    public VcsConfig getVcsConfig() {
        return vcsConfig;
    }

    public void setVcsConfig(@Nullable VcsConfig vcsConfig) {
        this.vcsConfig = vcsConfig;
    }

    @Nullable
    public PromotionConfig getPromotionConfig() {
        return promotionConfig;
    }

    public void setPromotionConfig(@Nullable PromotionConfig promotionConfig) {
        this.promotionConfig = promotionConfig;
    }
}
