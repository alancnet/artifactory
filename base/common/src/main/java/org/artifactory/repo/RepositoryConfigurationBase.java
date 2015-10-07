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

package org.artifactory.repo;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.DockerApiVersion;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlEnumValue;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Base class for the repository configuration.
 *
 * @author Tomer Cohen
 */
public abstract class RepositoryConfigurationBase implements RepositoryConfiguration {

    private String key;
    private String type;
    private String packageType;
    private String description = "";
    private String notes = "";
    private String includesPattern = "";
    private String excludesPattern = "";
    private String repoLayoutRef;
    private boolean enableNuGetSupport = false;
    private boolean enableGemsSupport = false;
    private boolean enableNpmSupport = false;
    private boolean enableBowerSupport = false;
    private boolean enableDebianSupport = false;
    private boolean debianTrivialLayout = false;
    private boolean enablePypiSupport = false;
    private boolean enableDockerSupport = false;
    private DockerApiVersion dockerApiVersion = DockerApiVersion.V1;
    private boolean forceDockerAuthentication = false;
    private boolean forceNugetAuthentication = false;
    private boolean enableVagrantSupport = false;
    private boolean enableGitLfsSupport = false;

    protected RepositoryConfigurationBase() {
    }

    protected RepositoryConfigurationBase(RepoDescriptor repoDescriptor, String type) {
        this.key = repoDescriptor.getKey();
        this.type = type;
        this.packageType = repoDescriptor.getType().toString().toLowerCase();
        String description = repoDescriptor.getDescription();
        if (StringUtils.isNotBlank(description)) {
            setDescription(description);
        }
        String notes = repoDescriptor.getNotes();
        if (StringUtils.isNotBlank(notes)) {
            setNotes(notes);
        }
        String excludesPattern = repoDescriptor.getExcludesPattern();
        if (StringUtils.isNotBlank(excludesPattern)) {
            setExcludesPattern(excludesPattern);
        }
        String includesPattern = repoDescriptor.getIncludesPattern();
        if (StringUtils.isNotBlank(includesPattern)) {
            setIncludesPattern(includesPattern);
        }
        RepoLayout repoLayout = repoDescriptor.getRepoLayout();
        if (repoLayout != null) {
            setRepoLayoutRef(repoLayout.getName());
        }


        switch (repoDescriptor.getType()) {
            case Maven:
                break;
            case Gradle:
                break;
            case Ivy:
                break;
            case SBT:
                break;
            case NuGet:
                setEnableNuGetSupport(true);
                setForceNugetAuthentication(repoDescriptor.isForceNugetAuthentication());
                break;
            case Gems:
                setEnableGemsSupport(true);
                break;
            case Npm:
                setEnableNpmSupport(true);
                break;
            case Bower:
                setEnableBowerSupport(true);
                break;
            case Debian:
                setEnableDebianSupport(true);
                break;
            case Pypi:
                setEnablePypiSupport(true);
                break;
            case Docker:
                setEnableDockerSupport(true);
                setDockerApiVersion(repoDescriptor.getDockerApiVersion().name());
                setForceDockerAuthentication(repoDescriptor.isForceDockerAuthentication());
                break;
            case Vagrant:
                setEnableVagrantSupport(true);
                break;
            case GitLfs:
                setEnableGitLfsSupport(true);
                break;
            case YUM:
                break;
            case VCS:
                break;
            case P2:
                break;
            case Generic:
                break;
        }

    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    @JsonProperty(TYPE_KEY)
    public String getType() {
        return type;
    }

    @Override
    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType.toLowerCase();
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getExcludesPattern() {
        return excludesPattern;
    }

    public void setExcludesPattern(String excludesPattern) {
        this.excludesPattern = excludesPattern;
    }

    @Override
    public String getIncludesPattern() {
        return includesPattern;
    }

    public void setIncludesPattern(String includesPattern) {
        this.includesPattern = includesPattern;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getRepoLayoutRef() {
        return repoLayoutRef;
    }

    public void setRepoLayoutRef(String repoLayoutRef) {
        this.repoLayoutRef = repoLayoutRef;
    }

    @Override
    public boolean isEnableNuGetSupport() {
        return enableNuGetSupport;
    }

    public void setEnableNuGetSupport(boolean enableNuGetSupport) {
        this.enableNuGetSupport = enableNuGetSupport;
    }

    @Override
    public boolean isEnableGemsSupport() {
        return enableGemsSupport;
    }

    public void setEnableGemsSupport(boolean enableGemsSupport) {
        this.enableGemsSupport = enableGemsSupport;
    }

    @Override
    public boolean isEnableNpmSupport() {
        return enableNpmSupport;
    }

    public void setEnableNpmSupport(boolean enableNpmSupport) {
        this.enableNpmSupport = enableNpmSupport;
    }

    @Override
    public boolean isEnableBowerSupport() {
        return enableBowerSupport;
    }

    public void setEnableBowerSupport(boolean enableBowerSupport) {
        this.enableBowerSupport = enableBowerSupport;
    }

    public void setEnableDebianSupport(boolean enableDebianSupport) {
        this.enableDebianSupport = enableDebianSupport;
    }

    @Override
    public boolean isEnableDebianSupport() {
        return enableDebianSupport;
    }

    @Override
    public boolean isDebianTrivialLayout() {
        return debianTrivialLayout;
    }

    public void setDebianTrivialLayout(boolean debianTrivialLayout) {
        this.debianTrivialLayout = debianTrivialLayout;
    }

    @Override
    public boolean isEnablePypiSupport() {
        return enablePypiSupport;
    }

    public void setEnablePypiSupport(boolean enablePypiSupport) {
        this.enablePypiSupport = enablePypiSupport;
    }

    @Override
    public boolean isEnableDockerSupport() {
        return enableDockerSupport;
    }

    public void setEnableDockerSupport(boolean enableDockerSupport) {
        this.enableDockerSupport = enableDockerSupport;
    }

    @Override
    public String getDockerApiVersion() {
        return dockerApiVersion.name();
    }

    public void setDockerApiVersion(String dockerApiVersion) {
        this.dockerApiVersion = DockerApiVersion.valueOf(dockerApiVersion);
    }

    @Override
    public boolean isForceDockerAuthentication() {
        return forceDockerAuthentication;
    }

    public void setForceDockerAuthentication(boolean forceDockerAuthentication) {
        this.forceDockerAuthentication = forceDockerAuthentication;
    }

    @Override
    public boolean isForceNugetAuthentication() {
        return forceNugetAuthentication;
    }

    public void setForceNugetAuthentication(boolean forceNugetAuthentication) {
        this.forceNugetAuthentication = forceNugetAuthentication;
    }

    @Override
    public boolean isEnableVagrantSupport() {
        return enableVagrantSupport;
    }

    public void setEnableVagrantSupport(boolean enableVagrantSupport) {
        this.enableVagrantSupport = enableVagrantSupport;
    }

    @Override
    public boolean isEnableGitLfsSupport() {
        return enableGitLfsSupport;
    }

    public void setEnableGitLfsSupport(boolean enableGitLfsSupport) {
        this.enableGitLfsSupport = enableGitLfsSupport;
    }

    /**
     * Extract from an Enum the {@link javax.xml.bind.annotation.XmlEnumValue} that are associated with its fields.
     *
     * @param clazz The class that is to be introspected
     * @return A map that maps {@link javax.xml.bind.annotation.XmlEnumValue#value()} to the enum name itself.
     */
    protected Map<String, String> extractXmlValueFromEnumAnnotations(Class clazz) {
        Map<String, String> annotationToName = Maps.newHashMap();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(XmlEnumValue.class)) {
                XmlEnumValue annotation = field.getAnnotation(XmlEnumValue.class);
                annotationToName.put(annotation.value(), field.getName());
            }
        }
        return annotationToName;
    }
}
