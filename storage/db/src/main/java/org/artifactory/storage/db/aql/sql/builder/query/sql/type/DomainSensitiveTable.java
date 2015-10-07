package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DomainSensitiveTable {
    private SqlTable table;
    private List<SqlTableEnum> tables;

    public DomainSensitiveTable(SqlTable table, List<SqlTableEnum> tables) {
        this.tables = tables;
        this.table = table;
    }

    public List<SqlTableEnum> getTables() {
        return tables;
    }

    public SqlTable getTable() {
        return table;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainSensitiveTable that = (DomainSensitiveTable) o;

        if (table != null ? !table.equals(that.table) : that.table != null) {
            return false;
        }
        if (tables != null ? !tables.equals(that.tables) : that.tables != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = table != null ? table.hashCode() : 0;
        result = 31 * result + (tables != null ? tables.hashCode() : 0);
        return result;
    }
}
