package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlBuildDependency;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiDependency extends AqlBase<AqlApiDependency, AqlBuildDependency> {

    public AqlApiDependency() {
        super(AqlBuildDependency.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiDependency> type() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildDependencyType, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiDependency> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildDependencyName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiDependency> sha1() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildDependencySha1, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiDependency> md5() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildDependencyMd5, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiDependency> scope() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildDependencyScope, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains<AqlApiDependency> module() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies, AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiDependency> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.dependencies, AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDependency create() {
        return new AqlApiDependency();
    }
}