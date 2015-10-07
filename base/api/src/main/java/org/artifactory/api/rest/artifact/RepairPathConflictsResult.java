/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.api.rest.artifact;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Container for path conflict repair results
 * @author Yoav Luft
 */
public class RepairPathConflictsResult {

    public String message;
    public long numConflicts;
    public long numRepaired;
    public List<PathConflict> conflicts;

    public RepairPathConflictsResult(String message) {
        this.message = message;
    }

    public RepairPathConflictsResult(List<PathConflict> conflicts, String message) {
        this.conflicts = conflicts;
        this.numConflicts = conflicts.size();
        this.message = message;
    }

    @XmlType(name = "PathConflict", propOrder = {"path", "conflicts"})
    public static class PathConflict {

        private String path;
        private List<String> conflicts = Lists.newArrayListWithCapacity(2);

        // Required by JABX
        @SuppressWarnings("unused")
        public PathConflict() { }

        public PathConflict(String path) {
            this.path = path;
            this.conflicts = Lists.newArrayList();
        }

        public void add(String path) {
            conflicts.add(path);
        }

        public String getPath() {
            return path;
        }

        public List<String> getConflicts() {
            return conflicts;
        }
    }
}
