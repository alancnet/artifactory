package org.artifactory.aql.model;

import java.util.ArrayList;
import java.util.Arrays;

import static org.artifactory.aql.model.AqlFieldEnum.*;

/**
 * @author Gidi Shabat
 */
public enum AqlDomainEnum {


    items("item", new String[]{"items"}, itemRepo, itemPath, itemName, itemType, itemSize, itemCreated, itemCreatedBy,
            itemModified, itemModifiedBy, itemUpdated),
    properties("property", new String[]{"properties"}, propertyKey, propertyValue),
    statistics("stat", new String[]{"stats"}, statDownloads, statDownloaded,
            statDownloadedBy),
    archives("archive", new String[]{"archives"}, archiveEntryPath, archiveEntryName),
    builds("build", new String[]{"builds"}, buildNumber, buildName, buildUrl, buildCreated, buildCreatedBy,
            buildModified, buildModifiedBy),
    artifacts("artifact", new String[]{"artifacts"}, buildArtifactName, buildArtifactType),
    dependencies("dependency", new String[]{"dependencies"}, buildDependencyName,
            buildDependencyType,
            buildDependencyScope),
    modules("module", new String[]{"modules"}, moduleName),
    buildProperties("property", new String[]{"build", "properties"}, buildPropertyKey, buildPropertyValue),
    moduleProperties("property", new String[]{ "module","properties"}, modulePropertyKey,
            modulePropertyValue);

    public String signatue;
    public String[] subDomains;
    public AqlFieldEnum[] fields;

    AqlDomainEnum(String signature, String[] domainPath, AqlFieldEnum... fields) {
        this.signatue = signature;
        this.subDomains = domainPath;
        this.fields = fields;
    }

    public static AqlDomainEnum valueFromSubDomains(ArrayList<String> subDomains) {
        String[] externalSubDomain = subDomains.toArray(new String[subDomains.size()]);
        for (AqlDomainEnum aqlDomainEnum : values()) {
            if (Arrays.equals(aqlDomainEnum.subDomains, externalSubDomain)) {
                return aqlDomainEnum;
            }
        }
        return null;
    }
}