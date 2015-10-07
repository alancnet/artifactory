package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlBuild;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiBuild extends AqlBase<AqlApiBuild, AqlBuild> {

    public AqlApiBuild() {
        super(AqlBuild.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> number() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildNumber, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> url() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildUrl, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> created() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildCreated, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> createdBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildCreatedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> modified() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildModified, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuild> modifiedBy() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildModifiedBy, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains<AqlApiBuild> module() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds, AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildPropertyDynamicFieldsDomains<AqlApiBuild> property() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.builds, AqlDomainEnum.buildProperties);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildPropertyDynamicFieldsDomains(subDomains);
    }

    public static AqlApiBuild create() {
        return new AqlApiBuild();
    }
}