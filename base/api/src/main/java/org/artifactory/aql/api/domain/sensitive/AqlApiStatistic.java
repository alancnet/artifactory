package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlStatistics;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiStatistic extends AqlBase<AqlApiStatistic, AqlStatistics> {

    public AqlApiStatistic() {
        super(AqlStatistics.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> downloads() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.statDownloads, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> downloadBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.statDownloadedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiStatistic> downloaded() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.statDownloaded, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiStatistic> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.statistics, AqlDomainEnum.items);
        subDomains.add(AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }

    public static AqlApiStatistic create() {
        return new AqlApiStatistic();
    }
}
