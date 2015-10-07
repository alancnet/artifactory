package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlItem;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiItem extends AqlBase<AqlApiItem, AqlItem> {

    public AqlApiItem() {
        super(AqlItem.class);
    }

    public static AqlApiItem create() {
        return new AqlApiItem();
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> repo() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemRepo, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> path() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemPath, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> type() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemType, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> size() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemSize, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> created() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemCreated, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> createdBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemCreatedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> modified() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemModified, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> modifiedBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemModifiedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> sha1Actual() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemActualSha1, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> sha1Orginal() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemOriginalSha1, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> md5Actual() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemActualMd5, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiItem> md5Orginal() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.itemOriginalMd5, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArchiveDynamicFieldsDomains<AqlApiItem> archive() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.archives);
        return new AqlApiDynamicFieldsDomains.AqlApiArchiveDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemPropertyDynamicFieldsDomains<AqlApiItem> property() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiItemPropertyDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiStatisticDynamicFieldsDomains<AqlApiItem> statistic() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiStatisticDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains<AqlApiItem> artifact() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains<AqlApiItem> dependency() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.items, AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains(subDomains);
    }
}
