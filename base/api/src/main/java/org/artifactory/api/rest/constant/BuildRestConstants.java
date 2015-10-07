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

package org.artifactory.api.rest.constant;

/**
 * Constants used by the REST build resource
 *
 * @author Noam Y. Tenne
 */
public interface BuildRestConstants {
    String PATH_ROOT = "build";

    //TODO: [by yl] Move to the buildInfo module
    String MT_BUILDS = RestConstants.MT_JFROG_APP + PATH_ROOT + ".Builds+json";
    String MT_BUILDS_BY_NAME = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildsByName+json";

    String MT_BUILD = RestConstants.MT_JFROG_APP + PATH_ROOT + ".Build+json";
    String MT_BUILD_INFO = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildInfo+json";
    String MT_BUILD_INFO_MODULE = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildInfoModule+json";
    String MT_BUILDS_DIFF = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildsDiff+json";
    String MT_BUILD_PATTERN_ARTIFACTS_REQUEST = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildPatternArtifactsRequest+json";
    String MT_BUILD_PATTERN_ARTIFACTS_RESULT = RestConstants.MT_JFROG_APP + PATH_ROOT + ".BuildPatternArtifactsResult+json";
    String MT_BUILD_ARTIFACTS_REQUEST = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".BuildArtifactsRequest+json";

    @Deprecated
    String MT_COPY_MOVE_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".CopyOrMoveResult+json";

    String MT_PROMOTION_REQUEST = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".PromotionRequest+json";
    String MT_PROMOTION_RESULT = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".PromotionResult+json";
    String MT_BINTRAY_DESCRIPTOR_OVERRIDE = RestConstants.MT_ARTIFACTORY_APP + PATH_ROOT + ".BintrayDescriptorOverrideParams+json";
}
