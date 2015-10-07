package org.artifactory.storage.db.aql.service.optimizer;

import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.ComplexPropertyCriteria;
import org.artifactory.storage.db.aql.sql.builder.query.aql.Criteria;
import org.artifactory.storage.db.aql.sql.builder.query.aql.SimpleCriteria;
import org.artifactory.storage.db.aql.sql.builder.query.aql.SimplePropertyCriteria;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gidi Shabat
 */
public class PropertyCriteriaRelatedWithOr extends OptimizationStrategy {
    private final Pattern pattern = Pattern.compile("([c,p,k,v]o)+[c,p,k,v]");

    /**
     * Foe each sequence of criterias with properties connected with "OR" the method replaces the tables in the
     * criterias to only one table (from the the first criteria).
     * Note that this action is legal since we never mix tables that are separated by brackets and in AQL each block
     * might contain one of the following combination of criterias tables
     * 1. all property criterias are on the same table as result of "$msp" (Not include criterias in internal blocks)
     * 2. all property criterias are on the result table as result of "$rf" (Not include criterias in internal blocks)
     * 3. all property criterias are dynamic (contains different property tables)
     * The only case that wil be effected by this method is case (3) and this is exactly the case that we want to optimize
     *
     * @param aqlQuery
     */
    @Override
    public void optimize(AqlQuery aqlQuery, String transformation) {
        // Try to find matching sub strings
        Matcher matcher = pattern.matcher(transformation);
        // Each match represent sub query that can be optimize
        while (matcher.find()) {
            // Get sub query bounds
            int start = matcher.start();
            int end = matcher.end();
            // The start index must point on criteria according to the pattern
            SqlTable table = getFirstPropertyTable(aqlQuery, transformation, start, end);
            if (table == null) {
                continue;
            }
            // Scan the sub query and replace the tables in "CRITERIAS WITH PROPERTIES" to the first table we just found
            // Note after replacing all the tables to a single table the SQL generator will bind all criterias to a single table
            // and no inner join will be generated for this criterias.
            for (int i = start; i < end; i++) {
                if (transformation.charAt(i) == 'p') {
                    ComplexPropertyCriteria criteria = (ComplexPropertyCriteria) aqlQuery.getAqlElements().get(i);
                    ComplexPropertyCriteria newCriteria = new ComplexPropertyCriteria(criteria.getSubDomains(),
                            criteria.getVariable1(), table, criteria.getComparatorName(), criteria.getVariable2(),
                            table);
                    aqlQuery.getAqlElements().remove(i);
                    aqlQuery.getAqlElements().add(i, newCriteria);

                }
                if (transformation.charAt(i) == 'k' || transformation.charAt(i) == 'v') {
                    Criteria criteria = (Criteria) aqlQuery.getAqlElements().get(i);
                    Criteria newCriteria = null;
                    if (criteria instanceof SimpleCriteria) {
                        newCriteria = new SimpleCriteria(criteria.getSubDomains(),
                                criteria.getVariable1(), table, criteria.getComparatorName(), criteria.getVariable2(),
                                table);
                    }
                    if (criteria instanceof SimplePropertyCriteria) {
                        newCriteria = new SimplePropertyCriteria(criteria.getSubDomains(),
                                criteria.getVariable1(), table, criteria.getComparatorName(), criteria.getVariable2(),
                                table);
                    }
                    aqlQuery.getAqlElements().remove(i);
                    aqlQuery.getAqlElements().add(i, newCriteria);
                }
            }
        }
    }

    private SqlTable getFirstPropertyTable(AqlQuery aqlQuery, String transformation, int start, int end) {
        for (int i = start; i < end; i++) {
            if (transformation.charAt(i) == 'p' || transformation.charAt(i) == 'k' || transformation.charAt(i) == 'v') {
                Criteria criteria = (Criteria) aqlQuery.getAqlElements().get(i);
                return criteria.getTable1();
            }
        }
        return null;
    }
}
