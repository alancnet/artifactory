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

package org.artifactory.addon.license;


/**
 * Dummy object to hold all the different statuses for a license
 *
 * @author Tomer Cohen
 */
public class LicenseStatus {
    private final boolean approved;
    private final boolean unapproved;
    private final boolean unknown;
    private final boolean notFound;
    private final boolean neutral;
    private final boolean autofind;

    public LicenseStatus(boolean approved, boolean autofind, boolean neutral, boolean notFound, boolean unapproved,
            boolean unknown) {
        this.approved = approved;
        this.autofind = autofind;
        this.neutral = neutral;
        this.notFound = notFound;
        this.unapproved = unapproved;
        this.unknown = unknown;
    }

    public boolean isApproved() {
        return approved;
    }

    public boolean isAutofind() {
        return autofind;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public boolean isNotFound() {
        return notFound;
    }

    public boolean isUnapproved() {
        return unapproved;
    }

    public boolean isUnknown() {
        return unknown;
    }
}
