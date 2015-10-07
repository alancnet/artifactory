package org.artifactory.storage.db.aql.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlSortTypeEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.SortDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Gidi Shabat
 */
public class AqlQueryDecorator {

    public void decorate(AqlQuery aqlQuery) {
        useDefaultSortIfNeeded(aqlQuery);
    }

    private void useDefaultSortIfNeeded(AqlQuery aqlQuery) {
        if(isMultiDomainResult(aqlQuery)) {
            AqlDomainEnum mainDomain = aqlQuery.getDomain();
            List<AqlFieldEnum> fieldEnums = resolveIdFieldFromDomain(aqlQuery);
            SortDetails sort = new SortDetails();
            sort.setSortType(AqlSortTypeEnum.asc);
            Set<DomainSensitiveField> set = Sets.newHashSet(aqlQuery.getResultFields());
            for (AqlFieldEnum fieldEnum : fieldEnums) {
                sort.addField(fieldEnum);
                set.add(new DomainSensitiveField(fieldEnum, Lists.newArrayList(mainDomain)));
            }
            aqlQuery.setSort(sort);
            aqlQuery.getResultFields().clear();
            aqlQuery.getResultFields().addAll(set);
        }
    }

    private boolean isMultiDomainResult(AqlQuery aqlQuery) {
        List<DomainSensitiveField> resultFields = aqlQuery.getResultFields();
        Set<String>set=Sets.newHashSet();
        set.addAll(resultFields.stream().map(resultField -> resultField.getField().domainName).collect(
                Collectors.toList()));
        return set.size() > 1;
    }

    public static List<AqlFieldEnum> resolveIdFieldFromDomain(AqlQuery aqlQuery) {
        AqlDomainEnum domain = aqlQuery.getDomain();
        switch (domain){
            case items:return Lists.newArrayList(AqlFieldEnum.itemId);
            case properties:return Lists.newArrayList(AqlFieldEnum.propertyId);
            case statistics:return Lists.newArrayList(AqlFieldEnum.statId);
            case artifacts:return Lists.newArrayList(AqlFieldEnum.buildArtifactId);
            case dependencies:return Lists.newArrayList(AqlFieldEnum.buildDependencyId);
            case modules:return Lists.newArrayList(AqlFieldEnum.moduleId);
            case moduleProperties:return Lists.newArrayList(AqlFieldEnum.modulePropertyId);
            case builds:return Lists.newArrayList(AqlFieldEnum.buildId);
            case buildProperties:return Lists.newArrayList(AqlFieldEnum.buildPropertyId);
            case archives:{
                // since archive represent two tables, in case of archive we might have two keys
                ArrayList<AqlFieldEnum> result = Lists.newArrayList();
                for (DomainSensitiveField field : aqlQuery.getResultFields()) {
                    if(field.getField()==AqlFieldEnum.archiveEntryPath){
                        result.add(AqlFieldEnum.archiveEntryPathId);
                    }
                    if(field.getField()==AqlFieldEnum.archiveEntryName){
                        result.add(AqlFieldEnum.archiveEntryNameId);
                    }
                }
                return result;
            }
            default: return null;
        }
    }
}
