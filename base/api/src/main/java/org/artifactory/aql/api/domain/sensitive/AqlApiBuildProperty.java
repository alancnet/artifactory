package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlBuildProperty;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiBuildProperty extends AqlBase<AqlApiBuildProperty, AqlBuildProperty> {

    public AqlApiBuildProperty() {
        super(AqlBuildProperty.class);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildProperty> key() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildPropertyKey, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiBuildProperty> value() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.buildPropertyValue, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains<AqlApiBuildProperty> build() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.buildProperties, AqlDomainEnum.builds);
        return new AqlApiDynamicFieldsDomains.AqlApiBuildDynamicFieldsDomains(subDomains);
    }

    public static AqlApiBuildProperty create() {
        return new AqlApiBuildProperty();
    }
}