package org.artifactory.storage.db.aql.sql.builder.links;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * The TableLinkBrowser scans recursively the table objects jumping from one table to other through links provided
 * by the TableLink and find the shortest route between two tables.
 * We needs does routes in order to generate the minimal joins needed to connect between two tables
 *
 * @author Gidi Shabat
 */
public class TableLinkBrowser {
    private Set<TableLink> tableLinkSet = Sets.newHashSet();

    /**
     * Find the shortest route between two tables
     *
     * @param from
     * @param to
     * @param exclude
     * @return
     */
    public List<TableLinkRelation> findPathTo(TableLink from, TableLink to, List<TableLink> exclude) {
        if (tableLinkSet.contains(from) || exclude.contains(from)) {
            return null;
        }
        if (to == from) {
            List<TableLinkRelation> relations = from.getRelations();
            for (TableLinkRelation relation : relations) {
                if (relation.getToTable().getTable().getTable() == to.getTable().getTable()) {
                    return Lists.newArrayList(relation);
                }
            }
            return Lists.newArrayList();
        }
        tableLinkSet.add(from);
        List<TableLinkRelation> relations = from.getRelations();
        List<TableLinkRelation> list = null;
        for (TableLinkRelation relation : relations) {
            List<TableLinkRelation> results;
            results = findPathTo(relation.getToTable(), to, exclude);
            if (results != null) {
                results.add(0, relation);
            }
            if (results != null && (list == null || list.size() == 0 || results.size() > 0 && list.size() > results.size())) {
                list = results;
            }
        }
        tableLinkSet.remove(from);
        return list;
    }

    public static TableLinkBrowser create() {
        return new TableLinkBrowser();
    }
}
