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

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A reg ex pattern with added support for named capturing groups.
 * BASED ON THE PROJECT: http://code.google.com/p/named-regexp
 * REMOVE WHEN MIGRATING TO JAVA 7 (yeah, right)
 *
 * @author Noam Y. Tenne
 */
public class NamedPattern {

    private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(\\w+)>");

    private Pattern pattern;
    private String namedPattern;
    private List<String> groupNames;

    public static NamedPattern compile(String regex) {
        return new NamedPattern(regex, 0);
    }

    public static NamedPattern compile(String regex, int flags) {
        return new NamedPattern(regex, flags);
    }

    private NamedPattern(String regex, int i) {
        namedPattern = regex;
        pattern = buildStandardPattern(regex);
        groupNames = extractGroupNames(regex);
    }

    public int flags() {
        return pattern.flags();
    }

    public NamedMatcher matcher(CharSequence input) {
        return new NamedMatcher(this, input);
    }

    Pattern pattern() {
        return pattern;
    }

    public String standardPattern() {
        return pattern.pattern();
    }

    public String namedPattern() {
        return namedPattern;
    }

    public List<String> groupNames() {
        return groupNames;
    }

    public String[] split(CharSequence input, int limit) {
        return pattern.split(input, limit);
    }

    public String[] split(CharSequence input) {
        return pattern.split(input);
    }

    public String toString() {
        return namedPattern;
    }

    static List<String> extractGroupNames(String namedPattern) {
        List<String> groupNames = Lists.newArrayList();
        Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
        while (matcher.find()) {
            groupNames.add(matcher.group(1));
        }
        return groupNames;
    }

    static Pattern buildStandardPattern(String namedPattern) {
        return Pattern.compile(NAMED_GROUP_PATTERN.matcher(namedPattern).replaceAll("("));
    }
}
