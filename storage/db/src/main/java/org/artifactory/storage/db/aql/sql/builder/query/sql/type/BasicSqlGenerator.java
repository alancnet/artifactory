package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.model.AqlTableFieldsEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.builder.links.TableLinkBrowser;
import org.artifactory.storage.db.aql.sql.builder.links.TableLinkRelation;
import org.artifactory.storage.db.aql.sql.builder.query.aql.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;
import org.artifactory.util.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph.tablesLinksMap;
import static org.artifactory.storage.db.aql.sql.model.AqlFieldExtensionEnum.getExtensionFor;

/**
 * This is actually the class that contains all the code that converts the AqlQuery to sqlQuery.
 *
 * @author Gidi Shabat
 */
public abstract class BasicSqlGenerator {
    public final Map<SqlTableEnum, Map<SqlTableEnum, List<TableLinkRelation>>> tableRouteMap;
    Function<DomainSensitiveField, DomainSensitiveTable> toTables = new Function<DomainSensitiveField, DomainSensitiveTable>() {
        @Nullable
        @Override
        public DomainSensitiveTable apply(@Nullable DomainSensitiveField input) {
            if (input == null) {
                return null;
            }
            AqlFieldExtensionEnum extension = getExtensionFor(input.getField());
            List<SqlTableEnum> tables = generateTableListFromSubDomainAndField(input.getSubDomains());
            SqlTable table = tablesLinksMap.get(extension.table).getTable();
            return new DomainSensitiveTable(table, tables);
        }
    };
    Function<AqlQueryElement, DomainSensitiveTable> firstTableFromCriteria = new Function<AqlQueryElement, DomainSensitiveTable>() {
        @Nullable
        @Override
        public DomainSensitiveTable apply(@Nullable AqlQueryElement input) {
            SqlTable table = input != null ? ((Criteria) input).getTable1() : null;
            if (table != null) {
                List<SqlTableEnum> tables = generateTableListFromSubDomainAndField(((Criteria) input).getSubDomains());
                return new DomainSensitiveTable(table, tables);
            }
            return null;
        }
    };
    Predicate<DomainSensitiveTable> notNull = new Predicate<DomainSensitiveTable>() {
        @Override
        public boolean apply(@Nullable DomainSensitiveTable input) {
            return input != null;
        }
    };
    Predicate<AqlQueryElement> criteriasOnly = new Predicate<AqlQueryElement>() {

        @Override
        public boolean apply(@Nullable AqlQueryElement input) {
            return input instanceof Criteria;
        }
    };
    Function<DomainSensitiveTable, SqlTableEnum> toTableEnum = new Function<DomainSensitiveTable, SqlTableEnum>() {
        @Nullable
        @Override
        public SqlTableEnum apply(@Nullable DomainSensitiveTable input) {
            return input != null ? input.getTable().getTable() : null;
        }
    };

    /**
     * The constructor scans the table schema and creates a map that contains the shortest route between to tables
     */
    protected BasicSqlGenerator() {
        Map<SqlTableEnum, Map<SqlTableEnum, List<TableLinkRelation>>> routeMap = Maps.newHashMap();
        for (TableLink from : tablesLinksMap.values()) {
            for (TableLink to : tablesLinksMap.values()) {
                List<TableLinkRelation> route = findShortestPathBetween(from, to);
                Map<SqlTableEnum, List<TableLinkRelation>> toRouteMap = routeMap.get(from.getTableEnum());
                if (toRouteMap == null) {
                    toRouteMap = Maps.newHashMap();
                    routeMap.put(from.getTableEnum(), toRouteMap);
                }
                toRouteMap.put(to.getTableEnum(), route);
            }
        }
        tableRouteMap = routeMap;
    }

    /**
     * The method generates the result part of the SQL query
     */
    public String results(AqlQuery aqlQuery) {
        StringBuilder result = new StringBuilder();
        result.append(" ");
        Iterator<DomainSensitiveField> iterator = aqlQuery.getResultFields().iterator();
        while (iterator.hasNext()) {
            DomainSensitiveField nextField = iterator.next();
            AqlFieldEnum fieldEnum = nextField.getField();
            AqlFieldExtensionEnum next = getExtensionFor(fieldEnum);
            SqlTable table = tablesLinksMap.get(next.table).getTable();
            result.append(table.getAlias()).append(next.tableField);
            if (iterator.hasNext()) {
                result.append(",");
            } else {
                result.append(" ");
            }
        }
        return result.toString();
    }

    /**
     * This is one of the most important and complicates parts in the Aql mechanism
     * Its task is to create the tables declaration part in the SQL query
     * the method does this with the help "sub domains" : Each field in the result fields and in the criteria
     * contains a list of domain that represent the route to the main domain, so basically, in order to bind one field
     * to the other we can trace the sub domains and bind each field to the "Main Table"
     * The problem with tracing the sub domain is that there is no injective match match between the tables and the domains
     * therefore we use the tablesLinksMap that contain the shortest route between to tabales and help us to ensures
     * that in "threaded form" we will bind all the tables needed from the
     * "Field table" to the "Main table"
     *
     * @param aqlQuery
     * @return
     */
    public String tables(AqlQuery aqlQuery) {
        Set<SqlTable> usedTables = Sets.newHashSet();
        StringBuilder join = new StringBuilder();
        join.append(" ");
        // Get all Result tables
        Iterable<DomainSensitiveTable> resultTables = Iterables.transform(aqlQuery.getResultFields(), toTables);
        // Find all the criterias
        Iterable<AqlQueryElement> filter = Iterables.filter(aqlQuery.getAqlElements(), criteriasOnly);
        // Get the tables from the criterias
        Iterable<DomainSensitiveTable> criteriasTables = Iterables.transform(filter, firstTableFromCriteria);
        // Concatenate the resultTables and the criteriasTables
        Iterable<DomainSensitiveTable> allTables = Iterables.concat(resultTables, criteriasTables);
        // Resolve  Join type (inner join or left outer join) for better performance
        AqlJoinTypeEnum joinTypeEnum = resolveJoinType(allTables);
        // Clean null tables if exists
        allTables = Iterables.filter(allTables, notNull);
        SqlTable mainTable = tablesLinksMap.get(getMainTable()).getTable();
        // Join the main table as first table (not join)
        joinTable(mainTable, null, null, null, usedTables, join, true, joinTypeEnum);
        for (DomainSensitiveTable table : allTables) {
            TableLink to;
            // Resolve the first table : which is always the "Main Table"
            SqlTableEnum fromTableEnum = table.getTables().get(0);
            // Find the route to the target ("to") table and add a join for each table in the route
            TableLink from = tablesLinksMap.get(fromTableEnum);
            for (int i = 1; i < table.getTables().size(); i++) {
                SqlTableEnum toTableEnum = table.getTables().get(i);
                to = tablesLinksMap.get(toTableEnum);
                List<TableLinkRelation> relations = tableRouteMap.get(from.getTableEnum()).get(to.getTableEnum());
                generateJoinTables(relations, usedTables, join, joinTypeEnum);
                from = to;
            }
            // Finally add a join to the field table
            to = tablesLinksMap.get(table.getTable().getTable());
            List<TableLinkRelation> relations = tableRouteMap.get(from.getTableEnum()).get(to.getTableEnum());
            generateJoinTables(relations, usedTables, join, joinTypeEnum, table.getTable());
        }
        return join.toString()+ " ";
    }

    /**
     * The method create the where part of the SQL query.
     * It actually scan all the criterias and Parenthesis elements in the AQL Query
     * and transform does elements into SQL syntax.
     *
     * @param aqlQuery
     * @return
     * @throws AqlException
     */
    public Pair<String, List<Object>> conditions(AqlQuery aqlQuery)
            throws AqlException {
        StringBuilder condition = new StringBuilder();
        List<Object> params = Lists.newArrayList();
        for (AqlQueryElement aqlQueryElement : aqlQuery.getAqlElements()) {
            if (aqlQueryElement instanceof ComplexPropertyCriteria || aqlQueryElement instanceof SimpleCriteria
                    || aqlQueryElement instanceof SimplePropertyCriteria) {
                Criteria criteria = (Criteria) aqlQueryElement;
                condition.append(criteria.toSql(params));
            }
            if (aqlQueryElement instanceof OperatorQueryElement) {
                AqlOperatorEnum operatorEnum = ((OperatorQueryElement) aqlQueryElement).getOperatorEnum();
                condition.append(" ").append(operatorEnum.name());
            }
            if (aqlQueryElement instanceof OpenParenthesisAqlElement) {
                condition.append("(");
            }
            if (aqlQueryElement instanceof CloseParenthesisAqlElement) {
                condition.append(")");
            }
        }
        return new Pair(condition.toString()+" ", params);
    }

    private List<TableLinkRelation> findShortestPathBetween(TableLink from, TableLink to) {
        List<TableLinkRelation> relations = TableLinkBrowser.create().findPathTo(from, to, getExclude());
        if (relations == null) {
            ArrayList<TableLink> excludes = Lists.newArrayList();
            relations = TableLinkBrowser.create().findPathTo(from, to, excludes);
        }
        relations = overrideRoute(relations);
        return relations;
    }

    protected abstract List<TableLink> getExclude();

    protected List<TableLinkRelation> overrideRoute(List<TableLinkRelation> route) {
        return route;
    }

    protected void generateJoinTables(List<TableLinkRelation> relations, Set<SqlTable> usedTables, StringBuilder join,
            AqlJoinTypeEnum joinTypeEnum) {
        if (relations == null) {
            return;
        }
        for (TableLinkRelation relation : relations) {
            AqlTableFieldsEnum fromField = relation.getFromField();
            SqlTable fromTable = relation.getFromTable().getTable();
            AqlTableFieldsEnum toFiled = relation.getToFiled();
            SqlTable toTable = relation.getToTable().getTable();
            joinTable(toTable, toFiled, fromTable, fromField, usedTables, join, false, joinTypeEnum);
        }
    }

    protected void generateJoinTables(List<TableLinkRelation> relations, Set<SqlTable> usedTables, StringBuilder join,
            AqlJoinTypeEnum joinTypeEnum, SqlTable sqlTable) {
        if (relations == null) {
            return;
        }
        for (TableLinkRelation relation : relations) {
            AqlTableFieldsEnum fromField = relation.getFromField();
            SqlTable fromTable = relation.getFromTable().getTable();
            AqlTableFieldsEnum toFiled = relation.getToFiled();
            SqlTable toTable = relation.getToTable().getTable();
            toTable = toTable.getTable() == sqlTable.getTable() ? sqlTable : toTable;
            joinTable(toTable, toFiled, fromTable, fromField, usedTables, join, false, joinTypeEnum);
        }
    }

    protected void joinTable(SqlTable table, AqlTableFieldsEnum tableJoinField, SqlTable onTable,
            AqlTableFieldsEnum onJoinFiled,
            Set<SqlTable> declaredTables, StringBuilder join, boolean first, AqlJoinTypeEnum joinTypeEnum) {

        if (!declaredTables.contains(table)) {
            if (first) {
                join.append(table.getTableName()).append(" ").append(table.getAliasDeclaration());
            } else {
                join.append(" ").append(joinTypeEnum.signature).append(" ").append(table.getTableName()).append(
                        " ").append(
                        table.getAliasDeclaration());
                join.append(" on ").append(table.getAlias()).append(tableJoinField).
                        append(" = ").append(onTable.getAlias()).append(onJoinFiled);
            }
        }
        declaredTables.add(table);
    }

    public String sort(AqlQuery aqlQuery) {
        SortDetails sortDetails = aqlQuery.getSort();
        if (sortDetails == null || sortDetails.getFields().size() == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<AqlFieldEnum> fields = sortDetails.getFields();
        Iterator<AqlFieldEnum> iterator = fields.iterator();
        while (iterator.hasNext()) {
            AqlFieldEnum sortField = iterator.next();
            AqlFieldExtensionEnum extension = getExtensionFor(sortField);
            SqlTable table = tablesLinksMap.get(extension.table).getTable();
            stringBuilder.append(table.getAlias()).append(extension.tableField);
            stringBuilder.append(" ").append(sortDetails.getSortType().getSqlName());
            if (iterator.hasNext()) {
                stringBuilder.append(",");
            }else {
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Query performance optimisation:
     * In case of single table join such as multiple properties table join
     * without the usage of any other table we can use inner join for better performance.
     *
     * @param allTables
     * @return
     */
    private AqlJoinTypeEnum resolveJoinType(Iterable<DomainSensitiveTable> allTables) {
        Iterable<SqlTableEnum> tables = Iterables.transform(allTables, toTableEnum);
        HashSet<SqlTableEnum> tableEnums = Sets.newHashSet();
        for (SqlTableEnum table : tables) {
            if (table != null) {
                tableEnums.add(table);
            }
        }
        if (tableEnums.size() == 1) {
            return AqlJoinTypeEnum.innerJoin;
        } else {
            return AqlJoinTypeEnum.leftOuterJoin;
        }
    }

    protected abstract SqlTableEnum getMainTable();

    private List<SqlTableEnum> generateTableListFromSubDomainAndField(List<AqlDomainEnum> subDomains) {
        List<SqlTableEnum> result = Lists.newArrayList();
        if (subDomains.size() > 1) {
            for (int i = 0; i < subDomains.size() - 1; i++) {
                result.add(domainToTable(subDomains.get(i)));
            }
        } else {
            result.add(domainToTable(subDomains.get(0)));
        }
        return result;
    }

    private SqlTableEnum domainToTable(AqlDomainEnum domainEnum) {
        switch (domainEnum) {
            case archives:
                return SqlTableEnum.archive_names;
            case items:
                return SqlTableEnum.nodes;
            case properties:
                return SqlTableEnum.node_props;
            case statistics:
                return SqlTableEnum.stats;
            case builds:
                return SqlTableEnum.builds;
            case buildProperties:
                return SqlTableEnum.build_props;
            case artifacts:
                return SqlTableEnum.build_artifacts;
            case dependencies:
                return SqlTableEnum.build_dependencies;
            case modules:
                return SqlTableEnum.build_modules;
            case moduleProperties:
                return SqlTableEnum.module_props;
        }
        return null;
    }
}
