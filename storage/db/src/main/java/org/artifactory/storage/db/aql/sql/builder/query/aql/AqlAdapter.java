package org.artifactory.storage.db.aql.sql.builder.query.aql;

import com.google.common.collect.Lists;
import org.artifactory.aql.AqlFieldResolver;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.model.AqlValue;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.aql.model.AqlVariableTypeEnum;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;
import org.artifactory.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.aql.model.AqlComparatorEnum.equals;
import static org.artifactory.aql.model.AqlFieldEnum.itemType;
import static org.artifactory.aql.model.AqlItemTypeEnum.file;
import static org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph.tablesLinksMap;

/**
 * The class contains common methods tha are being used by the AqlApiToAqlAdapter and the ParserToAqlAdapter
 * to convert the API or the parser result to AqlQuery
 *
 * @author Gidi Shabat
 */
public abstract class AqlAdapter {
    public static final OpenParenthesisAqlElement open = new OpenParenthesisAqlElement();
    public static final CloseParenthesisAqlElement close = new CloseParenthesisAqlElement();
    public static final OperatorQueryElement and = new OperatorQueryElement(AqlOperatorEnum.and);
    public static final OperatorQueryElement or = new OperatorQueryElement(AqlOperatorEnum.or);

    /**
     * Create Simple Property criteria: A property criteria is actually two criterias on the same property table
     * Example "key" "$matches" "license*"
     * The above criteria is being converted to the following SQL query
     * node_props.key equals 'license' and node_props.value like 'GPL'
     */
    public static Criteria createSimplePropertyCriteria(List<AqlDomainEnum> subDomains, AqlFieldEnum aqlField,
            String name2,
            AqlComparatorEnum comparatorEnum, AdapterContext context) {
        Pair<AqlVariable, AqlVariable> variables = new Pair<>(new AqlField(aqlField), new AqlValue(aqlField.type, name2));
        Pair<SqlTable, SqlTable> tables = resolveTableForSimpleCriteria(variables, context);
        return new SimplePropertyCriteria(subDomains, variables.getFirst(), tables.getFirst(), comparatorEnum.signature,
                variables.getSecond(), tables.getSecond());
    }

    /**
     * Create Property criteria: A property criteria is actually two criterias on the same property table
     * Example "license" "$matches" "GPL"
     * The above criteria is being converted to the following SQL query
     * node_props.key equals 'license' and node_props.value like 'GPL'
     */
    public static Criteria createComplexPropertyCriteria(List<AqlDomainEnum> subDomains, String name1, String name2,
            AqlComparatorEnum comparatorEnum,
            AdapterContext tableReference) {
        AqlVariable variable1 = AqlFieldResolver.resolve(name1, AqlVariableTypeEnum.string);
        AqlVariable variable2 = AqlFieldResolver.resolve(name2, AqlVariableTypeEnum.string);
        Pair<SqlTable, SqlTable> tables = resolveTableForPropertyCriteria(tableReference,subDomains);
        return new ComplexPropertyCriteria(subDomains, variable1, tables.getFirst(),
                comparatorEnum.signature, variable2, tables.getSecond());
    }

    /**
     * Create Simple criteria: A simple criteria is actually single criteria constructed as following
     * Field Comparator Value
     * Example the "artifact_repo" "$matches" "libs-release-local"
     * The above criteria is being converted to the following SQL query * node.repo like 'libs-release-local'
     */
    public static Criteria createSimpleCriteria(List<AqlDomainEnum> subDomains, AqlFieldEnum aqlField, String name2,
            AqlComparatorEnum comparatorEnum, AdapterContext context) {
        Pair<AqlVariable, AqlVariable> variables = new Pair<>(new AqlField(aqlField), new AqlValue(aqlField.type, name2));
        Pair<SqlTable, SqlTable> tables = resolveTableForSimpleCriteria(variables, context);
        return new SimpleCriteria(subDomains, variables.getFirst(), tables.getFirst(), comparatorEnum.signature,
                variables.getSecond(), tables.getSecond());
    }

    /**
     * Resolving tables is delicate issue.
     * The tables are provided as follow:
     * 1. If the table is not property table then use the default tables from the tablesLinksMap.
     * 2. if the table is property table then by default generate new table with new alias id to each property table
     * unless the criteria is inside freezeJoin function, and in such case use the table index provided in the
     * join operator
     */
    public static Pair<SqlTable, SqlTable> resolveTableForSimpleCriteria(Pair<AqlVariable, AqlVariable> variables,
            AdapterContext context) {
        AqlField field = (AqlField) variables.getFirst();
        AqlFieldExtensionEnum extension = AqlFieldExtensionEnum.getExtensionFor(field.getFieldEnum());
        SqlTableEnum tableEnum = extension.table;
        if (SqlTableEnum.node_props == tableEnum || SqlTableEnum.build_props == tableEnum ||
                SqlTableEnum.module_props == tableEnum) {
            ResultFilterAqlElement resultFieldAqlElement = (ResultFilterAqlElement) getResultFilterOperator(context);
            if (resultFieldAqlElement != null) {
                SqlTable table = tablesLinksMap.get(tableEnum).getTable();
                return new Pair<>(table, table);
            }
            MspAqlElement propertyAqlElement = (MspAqlElement) getMspOperator(context);
            if (propertyAqlElement == null) {
                SqlTable table = new SqlTable(tableEnum, context.provideIndex());
                return new Pair<>(table, table);
            }
            SqlTable table = new SqlTable(tableEnum, propertyAqlElement.getTableId());
            return new Pair<>(table, table);
        } else {
            SqlTable table = tablesLinksMap.get(tableEnum).getTable();
            return new Pair<>(table, table);
        }
    }

    /**
     * Resolving tables is delicate issue, in this case we now that the tables are properties therefore
     * by default generate new table with new alias id to each property table unless the criteria is inside freezeJoin
     * function, and in such case use the table index provided in the
     * join operator
     */
    public static Pair<SqlTable, SqlTable> resolveTableForPropertyCriteria(AdapterContext context,List<AqlDomainEnum> subDomains) {
        ResultFilterAqlElement resultFilterElement = (ResultFilterAqlElement) getResultFilterOperator(context);
        if (resultFilterElement != null) {
            AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size() - 1);
            AqlFieldExtensionEnum extension = AqlFieldExtensionEnum.getExtensionFor(aqlDomainEnum.fields[0]);
            SqlTable table = tablesLinksMap.get(extension.table).getTable();
            return new Pair<>(table, table);
        }
        MspAqlElement propertyAqlElement = (MspAqlElement) getMspOperator(context);
        if (propertyAqlElement == null) {
            AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size() - 1);
            AqlFieldExtensionEnum extension = AqlFieldExtensionEnum.getExtensionFor(aqlDomainEnum.fields[0]);
            SqlTable table = new SqlTable(extension.table, context.provideIndex());
            return new Pair<>(table, table);
        }
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size() - 1);
        AqlFieldExtensionEnum extension = AqlFieldExtensionEnum.getExtensionFor(aqlDomainEnum.fields[0]);
        SqlTable table = new SqlTable(extension.table, propertyAqlElement.getTableId());
        return new Pair<>(table, table);
    }

    /**
     * Scans the context que for leading join operators
     */
    protected static AqlQueryElement getMspOperator(AdapterContext context) {
        if (context.getFunctions().isEmpty()) {
            return null;
        }
        AqlQueryElement peek = context.peek();
        if (peek instanceof MspAqlElement) {
            return peek;
        }
        AqlQueryElement temp = context.pop();
        peek = getMspOperator(context);
        context.push(temp);
        return peek;
    }

    /**
     * Scans the context que for leading join operators
     */
    protected static AqlQueryElement getResultFilterOperator(AdapterContext context) {
        if (context.getFunctions().isEmpty()) {
            return null;
        }
        AqlQueryElement peek = context.peek();
        if (peek instanceof ResultFilterAqlElement) {
            return peek;
        }
        AqlQueryElement temp = context.pop();
        peek = getResultFilterOperator(context);
        context.push(temp);
        return peek;
    }

    /**
     * Scans the context que for leading or/and operators
     */
    protected static AqlQueryElement getOperator(AdapterContext context) {
        if (context.getFunctions().isEmpty()) {
            return null;
        }
        AqlQueryElement peek = context.peek();
        if (peek.isOperator()) {
            return peek;
        }
        AqlQueryElement temp = context.pop();
        peek = getOperator(context);
        context.push(temp);
        return peek;
    }

    /**
     * Adds operator to the AqlQuery if needed
     */
    protected static void addOperatorToAqlQueryElements(AdapterContext context) {
        List<AqlQueryElement> currentAqlQueryElments = context.getAqlQueryElements();
        if (!currentAqlQueryElments.isEmpty() && (currentAqlQueryElments.get(
                currentAqlQueryElments.size() - 1) instanceof Criteria ||
                currentAqlQueryElments.get(currentAqlQueryElments.size() - 1) instanceof CloseParenthesisAqlElement)) {
            context.addAqlQueryElements(getOperator(context));
        }
    }

    /**
     * This is ugly hack that force AQL ITEMS queries to return (by default) on files by
     * Injecting extra  (type=file) filter to the query.
     */
    protected void injectDefaultValues(AdapterContext context) {
        AqlQuery aqlQuery = context.getAqlQuery();
        AqlDomainEnum domain = aqlQuery.getDomain();
        // Check if the user Item type in his criterias
        boolean shouldAddDefailtItemTypeCriteria = shouldAddDefailtItemTypeCriteria(aqlQuery, domain);
        // If the user is not using criterias with type, then add criteria to set the default type = "file"
        if (shouldAddDefailtItemTypeCriteria) {
            ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(domain);
            Criteria criteria = createSimpleCriteria(subDomains, itemType, file.signature, equals, context);
            addCriteria(context, criteria);
        }
    }

    private boolean shouldAddDefailtItemTypeCriteria(AqlQuery aqlQuery, AqlDomainEnum domain) {
        if (domain == AqlDomainEnum.items) {
            for (AqlQueryElement aqlQueryElement : aqlQuery.getAqlElements()) {
                if (aqlQueryElement instanceof SimpleCriteria) {
                    SimpleCriteria criteria = (SimpleCriteria) aqlQueryElement;
                    if (((AqlField) criteria.getVariable1()).getFieldEnum() == itemType) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds criteria to the AqlQuery and its leading operator if needed
     */
    protected void addCriteria(AdapterContext context, Criteria criteria) {
        addOperatorToAqlQueryElements(context);
        context.addAqlQueryElements(criteria);
    }
}
