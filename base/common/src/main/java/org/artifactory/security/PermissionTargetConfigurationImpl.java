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

package org.artifactory.security;

import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class PermissionTargetConfigurationImpl {

    private String name;
    private String includesPattern = "**";
    private String excludesPattern = "";
    private List<String> repositories;
    private PrincipalConfigurationImpl principals;

    public PermissionTargetConfigurationImpl() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIncludesPattern() {
        return includesPattern;
    }

    public void setIncludesPattern(String includesPattern) {
        this.includesPattern = includesPattern;
    }

    public String getExcludesPattern() {
        return excludesPattern;
    }

    public void setExcludesPattern(String excludesPattern) {
        this.excludesPattern = excludesPattern;
    }

    public List<String> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public PrincipalConfigurationImpl getPrincipals() {
        return principals;
    }

    public void setPrincipals(PrincipalConfigurationImpl principals) {
        this.principals = principals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PermissionTargetConfigurationImpl)) {
            return false;
        }

        PermissionTargetConfigurationImpl that = (PermissionTargetConfigurationImpl) o;

        if (excludesPattern != null ? !excludesPattern.equals(that.excludesPattern) : that.excludesPattern != null) {
            return false;
        }
        if (includesPattern != null ? !includesPattern.equals(that.includesPattern) : that.includesPattern != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (principals != null ? !principals.equals(that.principals) : that.principals != null) {
            return false;
        }
        if (repositories != null ? !repositories.equals(that.repositories) : that.repositories != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (includesPattern != null ? includesPattern.hashCode() : 0);
        result = 31 * result + (excludesPattern != null ? excludesPattern.hashCode() : 0);
        result = 31 * result + (repositories != null ? repositories.hashCode() : 0);
        result = 31 * result + (principals != null ? principals.hashCode() : 0);
        return result;
    }
}
