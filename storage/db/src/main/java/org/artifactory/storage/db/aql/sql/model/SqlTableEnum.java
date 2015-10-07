package org.artifactory.storage.db.aql.sql.model;


/**
 * @author Gidi Shabat
 */
public enum SqlTableEnum {
    nodes("n"),
    node_props("np"),
    archive_paths("ap"),
    archive_names("an"),
    stats("s"),
    unknown("u"),
    indexed_archives("ia"),
    indexed_archives_entries("iae"),
    build_modules("bm"),
    module_props("bmp"),
    build_dependencies("bd"),
    build_artifacts("ba"),
    build_props("bp"),
    builds("b");
    public String alias;

    SqlTableEnum(String alias) {
        this.alias = alias;
    }

    public boolean isArchive() {
        return this == indexed_archives || this == indexed_archives_entries || this == archive_names || this == archive_paths;
    }

    public boolean isProperty() {
        return this == node_props;
    }
}
