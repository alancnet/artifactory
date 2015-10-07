package org.artifactory.aql.model;

import static org.artifactory.aql.model.AqlVariableTypeEnum.*;

/**
 * @author Gidi Shabat
 *
 * This class contains all the Fields (domain, native name and type) supported by AQL
 * In order to add new Field to AQL, just add new field to this class and update acordinatlly the AqlFieldExtensionEnum class
 */
public enum AqlFieldEnum {
    // node
    itemRepo("repo", "items", string),
    itemPath("path", "items", string),
    itemName("name", "items", string),
    itemCreated("created", "items", date),
    itemModified("modified", "items", date),
    itemUpdated("updated", "items", date),
    itemCreatedBy("created_by", "items", string),
    itemModifiedBy("modified_by", "items", string),
    itemType("type", "items", AqlVariableTypeEnum.itemType),
    itemDepth("depth", "items", integer),
    itemId("id", "items", longInt),
    itemOriginalMd5("original_md5", "items", string),
    itemActualMd5("actual_md5", "items", string),
    itemOriginalSha1("original_sha1", "items", string),
    itemActualSha1("actual_sha1", "items", string),
    itemSize("size", "items", longInt),
    // stats
    statDownloaded("downloaded", "statistics", date),
    statDownloads("downloads", "statistics", integer),
    statDownloadedBy("downloaded_by", "statistics", string),
    statId("id", "statistics", longInt),
    // properties
    propertyKey("key", "properties", string),
    propertyValue("value", "properties", string),
    propertyId("id", "properties", longInt),
    // archive entries
    archiveEntryName("entry_name", "archives", string),
    archiveEntryPath("entry_path", "archives", string),
    archiveEntryPathId("entry_path_id", "archives", longInt),
    archiveEntryNameId("entry_name_id", "archives", longInt),
    // builds
    moduleName("name", "modules", string),
    moduleId("id","modules",longInt),
    buildDependencyName("name", "dependencies", string),
    buildDependencyScope("scope", "dependencies", string),
    buildDependencyType("type", "dependencies", string),
    buildDependencySha1("sha1", "dependencies", string),
    buildDependencyMd5("md5", "dependencies", string),
    buildDependencyId("id", "dependencies", longInt),
    buildArtifactName("name", "artifacts", string),
    buildArtifactType("type", "artifacts", string),
    buildArtifactSha1("sha1", "artifacts", string),
    buildArtifactMd5("md5", "artifacts", string),
    buildArtifactId("id", "artifacts", longInt),
    buildPropertyKey("key", "buildProperties", string),
    buildPropertyValue("value", "buildProperties", string),
    buildPropertyId("id", "buildProperties", longInt),
    modulePropertyKey("key", "moduleProperties", string),
    modulePropertyValue("value", "moduleProperties", string),
    modulePropertyId("id", "moduleProperties", longInt),
    buildUrl("url", "builds", string),
    buildName("name", "builds", string),
    buildNumber("number", "builds", string),
    buildCreated("created", "builds", date),
    buildCreatedBy("created_by", "builds", string),
    buildModified("modified", "builds", date),
    buildModifiedBy("modified_by", "builds", string),
    buildId("id","builds",longInt);
    public String signature;
    public String domainName;
    public AqlVariableTypeEnum type;

    AqlFieldEnum(String signature, String domainName, AqlVariableTypeEnum type) {
        this.signature = signature;
        this.domainName = domainName;
        this.type = type;
    }

    public static AqlFieldEnum value(String signature) {
        signature = signature.toLowerCase();
        for (AqlFieldEnum field : values()) {
            if (field.signature.equals(signature)) {
                return field;
            }
        }
        return null;
    }

    public static AqlFieldEnum[] getFieldByDomain(AqlDomainEnum domain) {
        switch (domain) {
            case items:
                return new AqlFieldEnum[]{
                        itemRepo,
                        itemPath,
                        itemName,
                        itemCreated,
                        itemModified,
                        itemUpdated,
                        itemCreatedBy,
                        itemModifiedBy,
                        itemType,
                        itemDepth,
                        itemId,
                        itemOriginalMd5,
                        itemActualMd5,
                        itemOriginalSha1,
                        itemActualSha1,
                        itemSize
                };
            case statistics:
                return new AqlFieldEnum[]{
                        statDownloaded,
                        statDownloads,
                        statDownloadedBy,
                        statId
                };
            case properties:
                return new AqlFieldEnum[]{
                        propertyKey,
                        propertyValue,
                        propertyId
                };
            case archives:
                return new AqlFieldEnum[]{
                        archiveEntryName,
                        archiveEntryNameId,
                        archiveEntryPath,
                        archiveEntryPathId
                };
            case artifacts:
                return new AqlFieldEnum[]{
                        buildArtifactName,
                        buildArtifactType,
                        buildArtifactSha1,
                        buildArtifactMd5,
                        buildArtifactId
                };
            case dependencies:
                return new AqlFieldEnum[]{
                        buildDependencyName,
                        buildDependencyScope,
                        buildDependencyType,
                        buildDependencySha1,
                        buildDependencyMd5,
                        buildDependencyId
                };
            case modules:
                return new AqlFieldEnum[]{
                        moduleName,
                        moduleId
                };
            case moduleProperties:
                return new AqlFieldEnum[]{
                        modulePropertyKey,
                        modulePropertyValue,
                        modulePropertyId
                };
            case buildProperties:
                return new AqlFieldEnum[]{
                        buildPropertyKey,
                        buildPropertyValue,
                        buildPropertyId
                };
            case builds:
                return new AqlFieldEnum[]{
                        buildUrl,
                        buildName,
                        buildNumber,
                        buildCreated,
                        buildCreatedBy,
                        buildModified,
                        buildModifiedBy,
                        buildId
                };
        }
        throw new UnsupportedOperationException("Unsupported domain: " + domain);
    }

    public static AqlFieldEnum resolveFieldBySignatureAndDomain(String fieldSignatue, AqlDomainEnum domain) {
        for (AqlFieldEnum aqlField : values()) {
            if (aqlField.signature.equals(fieldSignatue) &&
                    AqlDomainEnum.valueOf(aqlField.domainName) == domain) {
                return aqlField;
            }
        }
        return null;
    }

    public boolean isId() {
        return this==itemId || this==propertyId || this==statId || this==buildArtifactId || this==buildDependencyId ||
                this==moduleId || this==modulePropertyId || this==buildId || this==buildPropertyId;
    }
}
