package org.artifactory.aql.api.domain.sensitive;


import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlArchiveItem;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiArchive extends AqlBase<AqlApiArchive, AqlArchiveItem> {

    public AqlApiArchive() {
        super(AqlArchiveItem.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiArchive> path() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.archives);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.archiveEntryPath, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiArchive> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.archives);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.archiveEntryName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiArchive> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.archives, AqlDomainEnum.items);
        subDomains.add(AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }

    public static AqlApiArchive create() {
        return new AqlApiArchive();
    }
}
