package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlBuildArtifact;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiArtifact extends AqlBase<AqlApiArtifact, AqlBuildArtifact> {

    public AqlApiArtifact() {
        super(AqlBuildArtifact.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiArtifact> type() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildArtifactType, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiArtifact> name() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildArtifactName, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiArtifact> sha1() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildArtifactSha1, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiArtifact> md5() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.artifacts);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildArtifactMd5, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains<AqlApiArtifact> module() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.artifacts, AqlDomainEnum.modules);
        return new AqlApiDynamicFieldsDomains.AqlApiModuleDynamicFieldsDomains(subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiArtifact> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.artifacts, AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }

    public static AqlApiArtifact create() {
        return new AqlApiArtifact();
    }
}