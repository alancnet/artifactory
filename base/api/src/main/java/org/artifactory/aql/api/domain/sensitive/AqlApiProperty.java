package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.result.rows.AqlProperty;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiProperty extends AqlBase<AqlApiProperty, AqlProperty> {

    public AqlApiProperty() {
        super(AqlProperty.class);
    }

    public static AqlApiProperty create() {
        return new AqlApiProperty();
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiProperty> key() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.propertyKey, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiProperty> value() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlFieldEnum.propertyValue, subDomains);
    }

    public static AqlBase.PropertyCriteriaClause<AqlApiProperty> property(String key, AqlComparatorEnum comparator,
            String value) {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlBase.PropertyCriteriaClause(key, comparator, value, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiProperty> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties, AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }
}
