package org.artifactory.storage.db.aql.sql.builder.query.sql.type;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.List;

import static org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph.tablesLinksMap;

/**
 * The class contains tweaking information and optimizations for Build dependencies queries.
 *
 * @author Gidi Shabat
 */
public class BuildDependenciesSqlGenerator extends BasicSqlGenerator {

    @Override
    protected List<TableLink> getExclude() {
        return Lists.newArrayList(tablesLinksMap.get(SqlTableEnum.build_artifacts));
    }

    @Override
    protected SqlTableEnum getMainTable() {
        return SqlTableEnum.build_dependencies;
    }
}
