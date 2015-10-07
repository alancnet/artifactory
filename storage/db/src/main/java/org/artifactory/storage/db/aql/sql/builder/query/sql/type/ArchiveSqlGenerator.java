package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlTableFieldsEnum;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.builder.links.TableLinkRelation;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

import static org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph.tablesLinksMap;

/**
 * The class contains tweaking information and optimizations for Archives queries.
 *
 * @author Gidi Shabat
 */
public class ArchiveSqlGenerator extends BasicSqlGenerator {

    private TableLink indexedArchiveEntries;
    private TableLinkRelation nameToIndex;
    private TableLinkRelation pathToIndex;
    private TableLinkRelation indexToPath;
    private TableLinkRelation indexToName;
    private TableLinkRelation indexToIndexArchive;
    private TableLinkRelation indexArchiveToIndex;
    private TableLinkRelation indexArchiveToNode;
    private TableLinkRelation nodeToIndexArchive;
    private List<TableLinkRelation> nameToIndexToPath;
    private List<TableLinkRelation> pathToIndexToName;


    @Override
    protected List<TableLink> getExclude() {
        return Lists.newArrayList(tablesLinksMap.get(SqlTableEnum.build_dependencies));
    }

    @Override
    protected List<TableLinkRelation> overrideRoute(List<TableLinkRelation> route) {
        init();
        if (nameToIndexToPath.equals(route)) {
            return Lists.newArrayList(nameToIndex, indexToIndexArchive, indexArchiveToNode, nodeToIndexArchive,
                    indexArchiveToIndex, indexToPath);
        }
        if (pathToIndexToName.equals(route)) {
            return Lists.newArrayList(pathToIndex, indexToIndexArchive, indexArchiveToNode, nodeToIndexArchive,
                    indexArchiveToIndex, indexToName);
        }
        return route;
    }

    private void init() {
        if (indexedArchiveEntries == null) {
            TableLink archivePaths = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.archive_paths);
            TableLink archiveNames = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.archive_names);
            TableLink nodes = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.nodes);
            TableLink indexedArchives = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.indexed_archives);
            indexedArchiveEntries = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.indexed_archives_entries);
            indexToPath = new TableLinkRelation(indexedArchiveEntries, AqlTableFieldsEnum.entry_path_id,
                    archivePaths, AqlTableFieldsEnum.path_id);
            nameToIndex = new TableLinkRelation(archiveNames, AqlTableFieldsEnum.name_id,
                    indexedArchiveEntries, AqlTableFieldsEnum.entry_name_id);
            nameToIndexToPath = Lists.newArrayList(nameToIndex, indexToPath);
            indexToName = new TableLinkRelation(indexedArchiveEntries, AqlTableFieldsEnum.entry_name_id,
                    archiveNames, AqlTableFieldsEnum.name_id);
            pathToIndex = new TableLinkRelation(archivePaths, AqlTableFieldsEnum.path_id,
                    indexedArchiveEntries, AqlTableFieldsEnum.entry_path_id);
            pathToIndexToName = Lists.newArrayList(pathToIndex, indexToName);

            nodeToIndexArchive = new TableLinkRelation(nodes, AqlTableFieldsEnum.sha1_actual,
                    indexedArchives, AqlTableFieldsEnum.archive_sha1);
            indexArchiveToNode = new TableLinkRelation(indexedArchives, AqlTableFieldsEnum.archive_sha1,
                    nodes, AqlTableFieldsEnum.sha1_actual);
            indexArchiveToIndex = new TableLinkRelation(indexedArchives, AqlTableFieldsEnum.indexed_archives_id,
                    indexedArchiveEntries, AqlTableFieldsEnum.indexed_archives_id);
            indexToIndexArchive = new TableLinkRelation(indexedArchiveEntries, AqlTableFieldsEnum.indexed_archives_id,
                    indexedArchives, AqlTableFieldsEnum.indexed_archives_id);
        }
    }

    @Override
    protected SqlTableEnum getMainTable() {
        return SqlTableEnum.archive_names;
    }

}
