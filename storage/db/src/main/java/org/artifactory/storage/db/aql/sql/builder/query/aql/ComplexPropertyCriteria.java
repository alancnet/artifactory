package org.artifactory.storage.db.aql.sql.builder.query.aql;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class ComplexPropertyCriteria extends Criteria {
    public ComplexPropertyCriteria(List<AqlDomainEnum> subDomains, AqlVariable variable1, SqlTable table1,
            String comparatorName, AqlVariable variable2, SqlTable table2) {
        super(subDomains, variable1, table1, comparatorName, variable2, table2);
    }

    /**
     * Converts propertyCriteria to Sql criteria
     *
     * @param params
     * @return
     * @throws AqlException
     */
    @Override
    public String toSql(List<Object> params) throws AqlException {
        // Get both variable which are Values (this is property criteria)
        AqlVariable value1 = getVariable1();
        AqlVariable value2 = getVariable2();
        // Get both tables which are node_props tables (this is property criteria)
        SqlTable table1 = getTable1();
        // SqlTable table2 = getTable2();
        // update the Sql input param list
        // Get the ComparatorEnum
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(getComparatorName());
        return createSqlComplexPropertyCriteria(comparatorEnum, value1, table1, value2, params);
    }
}
