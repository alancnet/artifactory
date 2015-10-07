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

package org.artifactory.model.xstream.fs;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author freds
 * @date Oct 12, 2008
 */
@XStreamAlias(FolderAdditionalInfo.ROOT)
public class FolderAdditionalInfo extends ItemAdditionalInfo {
    public static final String ROOT = "artifactory-folder-ext";

    public FolderAdditionalInfo() {
        super();
    }

    public FolderAdditionalInfo(FolderAdditionalInfo extension) {
        super(extension);
    }

    @Override
    public String toString() {
        return "FolderAdditionalInfo{" + super.toString() + "}";
    }

    @Override
    public boolean isIdentical(ItemAdditionalInfo additionalInfo) {
        return additionalInfo instanceof FolderAdditionalInfo && super.isIdentical(additionalInfo);
    }
}
