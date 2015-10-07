/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.converters.helpers;

import org.artifactory.converters.ArtifactoryConverterAdapter;
import org.artifactory.version.CompoundVersionDetails;

import java.io.FileOutputStream;

/**
 * @author Gidi Shabat
 */
public class MockHomeConverter implements ArtifactoryConverterAdapter {
    private String parh;

    public MockHomeConverter(String parh) {
        this.parh = parh;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(parh);
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target) {
        return source.getVersion().before(target.getVersion());
    }

    @Override
    public void revert() {

    }

    @Override
    public void backup() {

    }

    @Override
    public void clean() {

    }
}
