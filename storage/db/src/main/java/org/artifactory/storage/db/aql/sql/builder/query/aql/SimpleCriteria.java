package org.artifactory.storage.db.aql.sql.builder.query.aql;

import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;

/**
 * This class represent simple criteria which contains field comparator and value
 * For example "_artifact_repo" "$equals" "libs-release-local"
 *
 * @author Gidi Shabat
 */
public class SimpleCriteria extends Criteria {

    public SimpleCriteria(List<AqlDomainEnum> subDomains, AqlVariable variable1, SqlTable table1, String comparatorName,
            AqlVariable variable2, SqlTable table2) {
        super(subDomains, variable1, table1, comparatorName, variable2, table2);
    }

    /**
     * Convert simpleCriteria to sql criteria
     *
     * @param params
     * @return
     * @throws AqlException
     */
    @Override
    public String toSql(List<Object> params) throws AqlException {
        // Get both variable which are Field and Value (this is simple criteria)
        AqlVariable variable1 = getVariable1();
        AqlVariable variable2 = getVariable2();
        // Get both tables which are same and equals to the Field table
        SqlTable table1 = getTable1();
        SqlTable table2 = getTable2();
        // Add the variables to the input params if needed
        // Convert criteria into Sql
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(getComparatorName());
        return createSqlCriteria(comparatorEnum, variable1, table1, variable2, params);
    }
}
