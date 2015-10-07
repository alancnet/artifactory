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

package org.artifactory.api.module.regex;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

/**
 * A reg ex pattern with added support for named capturing groups.
 * BASED ON THE PROJECT: http://code.google.com/p/named-regexp
 * REMOVE WHEN MIGRATING TO JAVA 7 (yeah, right)
 *
 * @author Noam Y. Tenne
 */
public interface NamedMatchResult extends MatchResult {

    public List<String> orderedGroups();

    public Map<String, String> namedGroups();

    public String group(String groupName);

    public int start(String groupName);

    public int end(String groupName);
}
