package org.artifactory.storage.db.aql.sql.builder.links;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlTableFieldsEnum;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

/**
 * The TableLink wraps the SqlTable object  and provides/contains the relations between the tables.
 * It is being used by the TableLinkBrowser to find the shortest route between to tables
 * @author Gidi Shabat
 */
public class TableLink {
    private final List<TableLinkRelation> relations = Lists.newArrayList();
    private final SqlTable table;

    public TableLink(SqlTableEnum tableEnum) {
        this.table = new SqlTable(tableEnum);
    }

    /**
     * Adds  link between to tables in both directions
     * @param fromField
     * @param toTable
     * @param toFiled
     */
    public void addLink(AqlTableFieldsEnum fromField, TableLink toTable, AqlTableFieldsEnum toFiled) {
        TableLinkRelation tableLinkRelation = new TableLinkRelation(this, fromField, toTable, toFiled);
        relations.add(tableLinkRelation);
        tableLinkRelation = new TableLinkRelation(toTable, toFiled, this, fromField);
        toTable.relations.add(tableLinkRelation);
    }

    public SqlTable getTable() {
        return table;
    }

    public SqlTableEnum getTableEnum() {
        return table.getTable();
    }

    public List<TableLinkRelation> getRelations() {
        return relations;
    }

    @Override
    public String toString() {
        return "TableLink{" +
                "table=" + table +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableLink tableLink = (TableLink) o;

        if (!table.equals(tableLink.table)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return table.hashCode();
    }
}
