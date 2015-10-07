package org.artifactory.storage.db.aql.sql.builder.links;

import org.artifactory.aql.model.AqlTableFieldsEnum;

/**
 * The TableLinkRelation represent link between two tables
 *
 * @author Gidi Shabat
 */
public class TableLinkRelation {
    private TableLink fromTable;
    private AqlTableFieldsEnum fromField;
    private TableLink toTable;
    private AqlTableFieldsEnum toFiled;

    public TableLinkRelation(TableLink fromTable, AqlTableFieldsEnum fromField, TableLink toTable,
            AqlTableFieldsEnum toFiled) {
        this.fromTable = fromTable;
        this.fromField = fromField;
        this.toTable = toTable;
        this.toFiled = toFiled;
    }

    public TableLink getFromTable() {
        return fromTable;
    }

    public AqlTableFieldsEnum getFromField() {
        return fromField;
    }

    public TableLink getToTable() {
        return toTable;
    }

    @Override
    public String toString() {
        return "TableLinkRelation{" +
                "fromTable=" + fromTable +
                ", toTable=" + toTable +
                '}';
    }

    public AqlTableFieldsEnum getToFiled() {
        return toFiled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableLinkRelation that = (TableLinkRelation) o;

        if (fromField != that.fromField) {
            return false;
        }
        if (fromTable != null ? !fromTable.equals(that.fromTable) : that.fromTable != null) {
            return false;
        }
        if (toFiled != that.toFiled) {
            return false;
        }
        if (toTable != null ? !toTable.equals(that.toTable) : that.toTable != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromTable != null ? fromTable.hashCode() : 0;
        result = 31 * result + (fromField != null ? fromField.hashCode() : 0);
        result = 31 * result + (toTable != null ? toTable.hashCode() : 0);
        result = 31 * result + (toFiled != null ? toFiled.hashCode() : 0);
        return result;
    }
}
