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

package org.artifactory.fs;

import java.io.Serializable;
import java.util.Map;

/**
 * Module related information (group, artifact, version, etc.) for a file, as it was extracted according to the layout
 * of the repository the file is part of.
 *
 * @author Yoav Landman
 */
public interface FileLayoutInfo extends Serializable {

    String getOrganization();

    String getModule();

    String getBaseRevision();

    String getFolderIntegrationRevision();

    String getFileIntegrationRevision();

    String getClassifier();

    String getExt();

    String getType();

    Map<String, String> getCustomFields();

    String getCustomField(String tokenName);

    boolean isValid();

    String getPrettyModuleId();

    boolean isIntegration();
}
