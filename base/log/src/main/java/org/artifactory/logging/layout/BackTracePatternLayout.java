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

package org.artifactory.logging.layout;

import ch.qos.logback.classic.PatternLayout;
import org.artifactory.logging.converter.BackTraceLineOfCallerConverter;

import java.util.Map;

/**
 * Use a custom pattern layout when for the logger that replaces the default {@code LineOfCallerConverter} with {@code
 * ArtifactoryLineOfCallerConverter} to extract the correct line number
 *
 * @author Tomer Cohen
 */
public class BackTracePatternLayout extends PatternLayout {

    /**
     * Get the default converter map and replace the line number attribute (namely "L" and "line") from the default
     * converter to Artifactory's
     *
     * @return The default modified converter map.
     */
    @Override
    public Map<String, String> getDefaultConverterMap() {
        Map<String, String> map = super.getDefaultConverterMap();
        map.put("L", BackTraceLineOfCallerConverter.class.getName());
        map.put("line", BackTraceLineOfCallerConverter.class.getName());
        return map;
    }
}
