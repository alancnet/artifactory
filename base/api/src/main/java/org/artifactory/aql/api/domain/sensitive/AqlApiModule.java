package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlBuildModule;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiModule extends AqlBase<AqlApiArtifact, AqlBuildModule> {

    public AqlApiModule() {
        super(AqlBuildModule.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiModule> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.moduleName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains<AqlApiModule> artifact() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules, AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiArtifactDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains<AqlApiModule> dependency() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules, AqlDomainEnum.dependencies);
        return new AqlApiDynamicFieldsDomains.AqlApiDependencyDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains<AqlApiModule> build() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.modules, AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains(subDomains);
    }

    public static AqlApiModule create() {
        return new AqlApiModule();
    }
}