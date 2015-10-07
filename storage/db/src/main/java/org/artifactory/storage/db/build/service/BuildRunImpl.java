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

package org.artifactory.storage.db.build.service;

import org.artifactory.build.BuildInfoUtils;
import org.artifactory.build.BuildRun;

import java.util.Date;

/**
 * A basic build run info holder
 *
 * @author Noam Y. Tenne
 */
public class BuildRunImpl implements BuildRun {

    private final long buildId;
    private final String name;
    private final String number;
    private final String started;
    private final String ciUrl;
    private final String releaseStatus;
    private String numOfModules;
    private String numOfArtifact;
    private String numOfDependencies;

    public BuildRunImpl(String name, String number, Date started) {
        this(name, number, BuildInfoUtils.formatBuildTime(started.getTime()));
    }

    /**
     * @param name    Build name
     * @param number  Build number
     * @param started Build started
     */
    public BuildRunImpl(String name, String number, String started) {
        this(name, number, started, null, null);
    }

    /**
     * @param name          Build name
     * @param number        Build number
     * @param started       Build started
     * @param ciUrl         The URL of the original CI Server build
     * @param releaseStatus Build release status
     */
    public BuildRunImpl(String name, String number, String started, String ciUrl, String releaseStatus) {
        this(0L, name, number, started, ciUrl, releaseStatus);
    }

    /**
     * @param buildId
     * @param name
     * @param number
     * @param started
     * @param ciUrl
     * @param releaseStatus
     */
    public BuildRunImpl(long buildId, String name, String number, String started, String ciUrl, String releaseStatus) {
        this.buildId = buildId;
        this.name = name;
        this.number = number;
        this.started = started;
        this.ciUrl = ciUrl;
        this.releaseStatus = releaseStatus;
    }

    public long getBuildId() {
        return buildId;
    }

    /**
     * Returns the name of the build
     *
     * @return Build name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the number of the build
     *
     * @return Build number
     */
    @Override
    public String getNumber() {
        return number;
    }

    /**
     * Returns the starting time of the build
     *
     * @return Build start time
     */
    @Override
    public String getStarted() {
        return started;
    }

    /**
     * Returns a date representation of the build starting time
     *
     * @return Build started date
     * @throws java.text.ParseException
     */
    @Override
    public Date getStartedDate() {
        return new Date(BuildInfoUtils.parseBuildTime(getStarted()));
    }

    @Override
    public String getCiUrl() {
        return ciUrl;
    }

    @Override
    public String getReleaseStatus() {
        return releaseStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BuildRun)) {
            return false;
        }

        BuildRun run = (BuildRun) o;

        if (!name.equals(run.getName())) {
            return false;
        }
        if (!number.equals(run.getNumber())) {
            return false;
        }
        if (started != null ? !started.equals(run.getStarted()) : run.getStarted() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + number.hashCode();
        result = 31 * result + (started != null ? started.hashCode() : 0);
        return result;
    }

    public String getNumOfModules() {
        return numOfModules;
    }

    public void setNumOfModules(String numOfModules) {
        this.numOfModules = numOfModules;
    }

    public String getNumOfArtifact() {
        return numOfArtifact;
    }

    public void setNumOfArtifact(String numOfArtifact) {
        this.numOfArtifact = numOfArtifact;
    }

    public String getNumOfDependencies() {
        return numOfDependencies;
    }

    public void setNumOfDependencies(String numOfDependencies) {
        this.numOfDependencies = numOfDependencies;
    }

    @Override
    public String toString() {
        return "" + buildId + ' ' + name + ':' + number + ':' + started;
    }
}