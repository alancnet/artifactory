package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import com.google.common.collect.Maps;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.Map;

import static org.artifactory.aql.model.AqlTableFieldsEnum.*;
import static org.artifactory.storage.db.aql.sql.model.SqlTableEnum.*;

/**
 * The class contains the relations between the tables.
 * It is being used to find the shortest path between to tables to minimize the number of joins.
 *
 * @author Gidi Shabat
 */
public class AqlTableGraph {
    public static final Map<SqlTableEnum, TableLink> tablesLinksMap;

    static {
        //Create Tables and links
        TableLink nodesTable = new TableLink(SqlTableEnum.nodes);
        TableLink statisticsTable = new TableLink(SqlTableEnum.stats);
        TableLink nodesProps = new TableLink(SqlTableEnum.node_props);
        TableLink archiveNames = new TableLink(SqlTableEnum.archive_names);
        TableLink archivePaths = new TableLink(SqlTableEnum.archive_paths);
        TableLink indexedArchives = new TableLink(SqlTableEnum.indexed_archives);
        TableLink indexedArchiveEntries = new TableLink(SqlTableEnum.indexed_archives_entries);
        TableLink builds = new TableLink(SqlTableEnum.builds);
        TableLink buildsProps = new TableLink(SqlTableEnum.build_props);
        TableLink buildsModules = new TableLink(SqlTableEnum.build_modules);
        TableLink buildsArtifacts = new TableLink(SqlTableEnum.build_artifacts);
        TableLink buildsDependencies = new TableLink(SqlTableEnum.build_dependencies);
        TableLink buildModuleProperties = new TableLink(SqlTableEnum.module_props);
        nodesTable.addLink(node_id, nodesProps, node_id);
        nodesTable.addLink(sha1_actual, buildsArtifacts, sha1);
        nodesTable.addLink(sha1_actual, buildsDependencies, sha1);
        nodesTable.addLink(sha1_actual, indexedArchives, archive_sha1);
        nodesTable.addLink(node_id, statisticsTable, node_id);
        indexedArchives.addLink(indexed_archives_id, indexedArchiveEntries, indexed_archives_id);
        indexedArchiveEntries.addLink(entry_name_id, archiveNames, name_id);
        indexedArchiveEntries.addLink(entry_path_id, archivePaths, path_id);
        buildsModules.addLink(module_id, buildsArtifacts, module_id);
        buildsModules.addLink(module_id, buildsDependencies, module_id);
        buildsModules.addLink(module_id, buildModuleProperties, module_id);
        buildsModules.addLink(build_id, builds, build_id);
        builds.addLink(build_id, buildsProps, build_id);
        nodesProps.addLink(node_id, nodesProps, node_id);
        buildsProps.addLink(build_id, buildsProps, build_id);
        buildModuleProperties.addLink(module_id, buildModuleProperties, module_id);
        //Fill the tables map
        Map<SqlTableEnum, TableLink> map = Maps.newHashMap();
        map.put(indexed_archives, indexedArchives);
        map.put(indexed_archives_entries, indexedArchiveEntries);
        map.put(archive_names, archiveNames);
        map.put(archive_paths, archivePaths);
        map.put(stats, statisticsTable);
        map.put(nodes, nodesTable);
        map.put(node_props, nodesProps);
        map.put(build_dependencies, buildsDependencies);
        map.put(build_artifacts, buildsArtifacts);
        map.put(build_modules, buildsModules);
        map.put(module_props, buildModuleProperties);
        map.put(SqlTableEnum.builds, builds);
        map.put(build_props, buildsProps);
        tablesLinksMap = map;
    }
}
