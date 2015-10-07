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

package org.artifactory.api.module;

import org.apache.commons.lang.StringUtils;
import org.artifactory.fs.FileLayoutInfo;

import java.util.Map;

/**
 * @author Noam Y. Tenne
 */
public class ModuleInfo implements FileLayoutInfo {

    private String organization;
    private String module;
    private String baseRevision;
    private String folderIntegrationRevision;
    private String fileIntegrationRevision;
    private String classifier;
    private String ext;
    private String type;
    private Map<String, String> customFields;

    public ModuleInfo() {
    }

    ModuleInfo(String organization, String module, String baseRevision, String folderIntegrationRevision,
            String fileIntegrationRevision, String classifier, String ext, String type,
            Map<String, String> customFields) {
        this.organization = organization;
        this.module = module;
        this.baseRevision = baseRevision;
        this.folderIntegrationRevision = folderIntegrationRevision;
        this.fileIntegrationRevision = fileIntegrationRevision;
        this.classifier = classifier;
        this.ext = ext;
        this.type = type;
        this.customFields = customFields;
    }

    @Override
    public String getOrganization() {
        return organization;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public String getBaseRevision() {
        return baseRevision;
    }

    @Override
    public String getFolderIntegrationRevision() {
        return folderIntegrationRevision;
    }

    @Override
    public String getFileIntegrationRevision() {
        return fileIntegrationRevision;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getExt() {
        return ext;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getCustomFields() {
        return customFields;
    }

    @Override
    public String getCustomField(String tokenName) {
        if (customFields == null) {
            return null;
        }
        return customFields.get(tokenName);
    }

    @Override
    public boolean isValid() {
        return StringUtils.isNotBlank(organization) && StringUtils.isNotBlank(module) &&
                StringUtils.isNotBlank(baseRevision);
    }

    /**
     * Returns the module "id" in a "prettier" format.<br> {@link #toString()} will not omit fields like the classifier
     * and type when they are not specified. This results in ugly artifact IDs.<br> This implementation will simply omit
     * fields which are not specified.
     *
     * @return Summarized module info
     */
    @Override
    public String getPrettyModuleId() {
        if (StringUtils.isBlank(getOrganization()) || StringUtils.isBlank(getModule()) ||
                StringUtils.isBlank(getBaseRevision())) {
            return "null:null:null";
        }

        StringBuilder moduleIdBuilder = new StringBuilder(getOrganization()).append(":").append(getModule()).
                append(":").append(getBaseRevision());

        if (StringUtils.isNotBlank(getFileIntegrationRevision())) {
            moduleIdBuilder.append("-").append(getFileIntegrationRevision());
        }

        String classifier = getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            moduleIdBuilder.append(":").append(classifier);
        }

        String type = getType();
        if (StringUtils.isNotBlank(type)) {
            moduleIdBuilder.append(":").append(type);
        }
        return moduleIdBuilder.toString();
    }

    @Override
    public boolean isIntegration() {
        return StringUtils.isNotBlank(getFolderIntegrationRevision()) ||
                StringUtils.isNotBlank(getFileIntegrationRevision());
    }

    @Override
    public String toString() {
        return new StringBuilder("organization = ").append(organization).append(", module = ").append(module).
                append(", baseRevision = ").append(baseRevision).append(", folderIntegrationRevision = ").
                append(folderIntegrationRevision).append(", fileIntegrationRevision = ").
                append(fileIntegrationRevision).append(", classifier = ").append(classifier).append(", ext = ").
                append(ext).append(", type = ").append(type).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModuleInfo)) {
            return false;
        }

        ModuleInfo that = (ModuleInfo) o;

        if (fileIntegrationRevision != null ?
                !fileIntegrationRevision.equals(that.fileIntegrationRevision) :
                that.fileIntegrationRevision != null) {
            return false;
        }
        if (classifier != null ? !classifier.equals(that.classifier) : that.classifier != null) {
            return false;
        }
        if (ext != null ? !ext.equals(that.ext) : that.ext != null) {
            return false;
        }
        if (module != null ? !module.equals(that.module) : that.module != null) {
            return false;
        }
        if (organization != null ? !organization.equals(that.organization) : that.organization != null) {
            return false;
        }
        if (folderIntegrationRevision != null ? !folderIntegrationRevision.equals(that.folderIntegrationRevision) :
                that.folderIntegrationRevision != null) {
            return false;
        }
        if (baseRevision != null ? !baseRevision.equals(that.baseRevision) : that.baseRevision != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = organization != null ? organization.hashCode() : 0;
        result = 31 * result + (module != null ? module.hashCode() : 0);
        result = 31 * result + (baseRevision != null ? baseRevision.hashCode() : 0);
        result = 31 * result + (folderIntegrationRevision != null ? folderIntegrationRevision.hashCode() : 0);
        result = 31 * result + (fileIntegrationRevision != null ? fileIntegrationRevision.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
