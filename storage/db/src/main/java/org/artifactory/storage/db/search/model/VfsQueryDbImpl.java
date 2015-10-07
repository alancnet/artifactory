/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.storage.db.search.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.sapi.search.InvalidQueryRuntimeException;
import org.artifactory.sapi.search.VfsBoolType;
import org.artifactory.sapi.search.VfsComparatorType;
import org.artifactory.sapi.search.VfsFunctionType;
import org.artifactory.sapi.search.VfsQuery;
import org.artifactory.sapi.search.VfsQueryResult;
import org.artifactory.sapi.search.VfsQueryResultType;
import org.artifactory.sapi.search.VfsQueryRow;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.DbType;
import org.artifactory.storage.db.fs.entity.NodePath;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.spring.StorageContextHelper;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.EmptyStackException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Date: 8/5/11
 * Time: 6:07 PM
 *
 * @author Fred Simon
 */
public class VfsQueryDbImpl implements VfsQuery {
    private static final Logger log = LoggerFactory.getLogger(VfsQueryDbImpl.class);

    private VfsQueryResultType expectedResultType = VfsQueryResultType.ANY_ITEM;
    private final Set<String> repoKeys = Sets.newHashSet();
    private final BaseGroupCriterion defaultGroup;
    protected final List<VfsQueryPathCriterionDbImpl> pathCriteria = Lists.newArrayList();
    private final List<OrderBy> orders = Lists.newArrayList();

    private Stack<BaseGroupCriterion> groups = new Stack<>();
    private VfsQueryCriterionDbImpl currentCriteria = null;

    public VfsQueryDbImpl() {
        defaultGroup = new BaseGroupCriterion();
    }

    @Override
    public VfsQuery expectedResult(@Nonnull VfsQueryResultType itemType) {
        expectedResultType = itemType;
        return this;
    }

    @Override
    public VfsQuery setSingleRepoKey(String repoKey) {
        this.repoKeys.clear();
        this.repoKeys.add(repoKey);
        return this;
    }

    @Override
    public VfsQuery setRepoKeys(Collection<String> repoKeys) {
        if (repoKeys != null) {
            this.repoKeys.clear();
            this.repoKeys.addAll(repoKeys);
        }
        return this;
    }

    @Override
    public VfsQuery orderByAscending(@Nonnull String propertyName) {
        orders.add(new OrderBy(propertyName, true));
        return this;
    }

    @Override
    public VfsQuery orderByDescending(@Nonnull String propertyName) {
        orders.add(new OrderBy(propertyName, false));
        return this;
    }

    @Override
    public VfsQuery name(@Nonnull String nodeName) {
        return prop("node_name").val(nodeName);
    }

    @Override
    public VfsQuery archiveName(@Nonnull String entryName) {
        return prop("entry_name").val(entryName);
    }

    @Override
    public VfsQuery archivePath(@Nonnull String entryPath) {
        return prop("entry_path").val(entryPath);
    }

    @Override
    public VfsQuery prop(@Nonnull String propertyName) {
        addCurrentCriteriaIfNeeded();
        currentCriteria = new VfsQueryCriterionDbImpl(propertyName);
        return this;
    }

    @Override
    public VfsQuery comp(@Nonnull VfsComparatorType comparator) {
        if (currentCriteria == null) {
            throw new IllegalStateException("Cannot add comparator to non existing property!");
        }
        currentCriteria.setComparator(comparator);
        return this;
    }

    @Override
    public VfsQuery func(@Nonnull VfsFunctionType function) {
        if (currentCriteria == null) {
            throw new IllegalStateException("Cannot add function to non existing property!");
        }
        currentCriteria.setFunction(function);
        return this;
    }

    @Override
    public VfsQuery val(String... values) {
        if (currentCriteria == null) {
            throw new IllegalStateException("Cannot add filter value to non existing property!");
        }
        currentCriteria.setValue(values);
        return this;
    }

    @Override
    public VfsQuery val(@Nonnull Long value) {
        if (currentCriteria == null) {
            throw new IllegalStateException("Cannot add filter value to non existing property!");
        }
        currentCriteria.setValue(value);
        return this;
    }

    @Override
    public VfsQuery val(@Nonnull Calendar value) {
        if (currentCriteria == null) {
            throw new IllegalStateException("Cannot add filter value to non existing property!");
        }
        currentCriteria.setValue(value);
        return this;
    }

    @Override
    public VfsQuery nextBool(@Nonnull VfsBoolType bool) {
        if (currentCriteria == null) {
            throw new IllegalStateException("Cannot add next bool to non existing property!");
        }
        currentCriteria.nextBool = bool;
        internalAddCriterion(currentCriteria);
        currentCriteria = null;
        return this;
    }

    @Override
    public VfsQuery startGroup() {
        addCurrentCriteriaIfNeeded();
        BaseGroupCriterion newGroup = new BaseGroupCriterion();
        internalAddCriterion(newGroup);
        groups.push(newGroup);
        return this;
    }

    private void addCurrentCriteriaIfNeeded() {
        if (currentCriteria != null) {
            internalAddCriterion(currentCriteria);
            currentCriteria = null;
        }
    }

    @Override
    public VfsQuery endGroup(@Nullable VfsBoolType bool) {
        try {
            BaseGroupCriterion groupCriterion = groups.pop();
            if (currentCriteria != null) {
                groupCriterion.addCriterion(currentCriteria);
                currentCriteria = null;
            }
            if (bool != null) {
                groupCriterion.nextBool = bool;
            }
            return this;
        } catch (EmptyStackException e) {
            throw new IllegalStateException("Cannot end group that did not start!", e);
        }
    }

    @Override
    public VfsQuery endGroup() {
        return endGroup(null);
    }

    private void internalAddCriterion(BaseVfsQueryCriterion criterion) {
        if (!groups.isEmpty()) {
            groups.peek().addCriterion(criterion);
        } else {
            defaultGroup.addCriterion(criterion);
        }
    }

    @Override
    @Nonnull
    public VfsQueryResult execute(int limit) {
        if (!groups.isEmpty()) {
            throw new IllegalStateException("Cannot execute while group still active!");
        }
        addCurrentCriteriaIfNeeded();
        DbSqlQueryBuilder query = new DbSqlQueryBuilder();
        fillSelectBase(query);
        fillExpectedNodeType(query);
        fillRepoFilter(query);
        fillPathCriterion(query);
        fillLimitCriterion(query, limit);
        fillCriteria(query);
        fillOrderBy(query);

        return executeSelect(query, limit);
    }

    private VfsQueryResult executeSelect(DbSqlQueryBuilder query, int limit) {
        if (limit != Integer.MAX_VALUE) {
            DbService dbService = StorageContextHelper.get().beanForType(DbService.class);
            DbType databaseType = dbService.getDatabaseType();
            boolean supportsLimitSyntax = databaseType == DbType.MYSQL;
            if (supportsLimitSyntax) {
                query.append(" LIMIT ").append(String.valueOf(limit));
            }
        }

        ResultSet rs = null;
        try {
            log.debug("Executing search query: {}", query);
            List<Object> params = query.params;
            JdbcHelper jdbcHelper = StorageContextHelper.get().beanForType(JdbcHelper.class);
            rs = jdbcHelper.executeSelect(query.sqlQuery.toString(),
                    (Object[]) params.toArray(new Object[params.size()]));
            LinkedHashMap<NodePath, VfsQueryRow> map = Maps.newLinkedHashMap();
            int nbLines = 0;
            while (rs.next()) {
                if (nbLines >= limit) {
                    break;
                }
                nbLines++;
                int pos = 1;
                long nodeId = rs.getLong(pos++);
                boolean file = rs.getBoolean(pos++);
                NodePath nodePath = new NodePath(rs.getString(pos++),
                        BaseDao.emptyIfNullOrDot(rs.getString(pos++)),
                        BaseDao.emptyIfNullOrDot(rs.getString(pos++)),
                        file);
                VfsQueryRow row = map.get(nodePath);
                if (row == null) {
                    if (hasArchiveEntries()) {
                        row = new VfsQueryRowDbImpl(nodeId, file, nodePath,
                                rs.getString(pos++), rs.getString(pos++));
                    } else {
                        row = new VfsQueryRowDbImpl(nodeId, file, nodePath);
                    }
                    map.put(nodePath, row);
                } else {
                    if (hasArchiveEntries()) {
                        ((VfsQueryRowDbImpl) row).addArchiveEntry(rs.getString(pos++), rs.getString(pos++));
                    } else {
                        log.warn("Got multiple times the same node " + nodePath + " in query " + query);
                    }
                }
                log.debug("Used {} params", pos);
            }
            if (nbLines >= limit) {
                nbLines = -1;
            }
            return new VfsQueryResultDbImpl(map.values(), nbLines);
        } catch (SQLException e) {
            throw new StorageException("Could not execute query '" + query + "' due to:" + e.getMessage(), e);
        } finally {
            DbUtils.close(rs);
        }
    }

    protected void fillSelectBase(DbSqlQueryBuilder query) {
        query.append("SELECT nodes.node_id, nodes.node_type, nodes.repo, nodes.node_path, nodes.node_name");
        if (hasArchiveEntries()) {
            query.append(", archive_paths.entry_path, archive_names.entry_name");
        }
        query.append(" FROM nodes");
        if (hasArchiveEntries()) {
            query.append(", indexed_archives, indexed_archives_entries, archive_paths, archive_names");
        }
        if (defaultGroup.hasPropertyFilter()) {
            query.append(", node_props");
        }
        if (defaultGroup.hasStatisticFilter()) {
            query.append(" LEFT JOIN stats ON stats.node_id = nodes.node_id");
        }
        query.append(" WHERE ");
        if (defaultGroup.hasPropertyFilter()) {
            query.addNextBoolIfNeeded();
            query.append("node_props.node_id = nodes.node_id");
            query.nextBool = VfsBoolType.AND;
        }
        if (hasArchiveEntries()) {
            query.addNextBoolIfNeeded();
            query.append("indexed_archives.archive_sha1 = nodes.sha1_actual");
            query.nextBool = VfsBoolType.AND;
            query.addNextBoolIfNeeded();
            query.append("indexed_archives.indexed_archives_id = indexed_archives_entries.indexed_archives_id");
            query.nextBool = VfsBoolType.AND;
            query.addNextBoolIfNeeded();
            query.append("indexed_archives_entries.entry_path_id = archive_paths.path_id");
            query.nextBool = VfsBoolType.AND;
            query.addNextBoolIfNeeded();
            query.append("indexed_archives_entries.entry_name_id = archive_names.name_id");
            query.nextBool = VfsBoolType.AND;
        }
    }

    private boolean hasArchiveEntries() {
        return expectedResultType == VfsQueryResultType.ARCHIVE_ENTRY;
    }

    protected void fillExpectedNodeType(DbSqlQueryBuilder query) {
        switch (expectedResultType) {
            case ANY_ITEM:
                // No filter here
                break;
            case FILE:
            case ARCHIVE_ENTRY:
                query.addNextBoolIfNeeded();
                query.append("nodes.node_type=1");
                query.nextBool = VfsBoolType.AND;
                break;
            case FOLDER:
                query.addNextBoolIfNeeded();
                query.append("nodes.node_type=0");
                query.nextBool = VfsBoolType.AND;
                break;
            default:
                throw new IllegalStateException("Query node type " + expectedResultType + " not supported yet!");
        }
    }

    protected void fillRepoFilter(DbSqlQueryBuilder query) {
        if (!repoKeys.isEmpty()) {
            query.addNextBoolIfNeeded();
            query.append(" nodes.repo ");
            if (repoKeys.size() == 1) {
                // Simple path
                String singleRepoKey = repoKeys.iterator().next();
                if (!StringUtils.isBlank(singleRepoKey)) {
                    query.append(" = ");
                    query.addParam(singleRepoKey);
                } else {
                    throw new InvalidQueryRuntimeException("Cannot accept null or empty repo key!");
                }
            } else {
                query.append(' ').append(VfsComparatorType.IN.str).append(' ');
                query.addListParam(repoKeys);
            }
            query.nextBool = VfsBoolType.AND;
        }
    }

    protected void fillCriteria(DbSqlQueryBuilder query) {
        defaultGroup.fill(query);
    }

    protected void fillOrderBy(DbSqlQueryBuilder query) {
        for (OrderBy order : orders) {
            query.append("ORDER BY ").append(order.propertyName);
            if (order.ascending) {
                query.append(" ASC");
            } else {
                query.append(" DESC");
            }
        }
    }

    @Override
    public VfsQuery addPathFilters(String... pathFilters) {
        for (String pathFilter : pathFilters) {
            if (StringUtils.isBlank(pathFilter)) {
                internalAddPathCriterion(createSmartPathCriterion("*"));
            } else {
                internalAddPathCriterion(createSmartPathCriterion(pathFilter));
            }
        }
        return this;
    }

    @Override
    public VfsQuery addPathFilter(String relativePathFilter) {
        // We can have double // in this path,
        // Be careful when we split with slash // will return an empty string
        // IMPORTANT NOTE... Big bug in spilt, splitting "//g1*//" return {"", "", "g1*"} instead of {"", "g1*", ""}
        String[] split = relativePathFilter.split(DbQueryHelper.FORWARD_SLASH);
        for (String path : split) {
            internalAddPathCriterion(createSmartPathCriterion(path));
        }
        return this;
    }

    private VfsQueryPathCriterionDbImpl createSmartPathCriterion(String pathFilter) {
        if (pathFilter == null || pathFilter.contains(DbQueryHelper.FORWARD_SLASH)) {
            throw new InvalidQueryRuntimeException(
                    "Path filter element cannot be null or contain slash: " + pathFilter);
        }
        if (pathFilter.length() == 0 || ALL_PATH_VALUE.equals(pathFilter)) {
            return new VfsQueryPathCriterionDbImpl(VfsComparatorType.ANY, ALL_PATH_VALUE);
        } else {
            if (DbQueryHelper.hasWildcards(pathFilter)) {
                return new VfsQueryPathCriterionDbImpl(VfsComparatorType.CONTAINS, pathFilter);
            } else {
                return new VfsQueryPathCriterionDbImpl(VfsComparatorType.EQUAL, pathFilter);
            }
        }
    }

    private void internalAddPathCriterion(VfsQueryPathCriterionDbImpl criterion) {
        if (criterion != null && criterion.isValid()) {
            pathCriteria.add(criterion);
        }
    }

    protected void fillPathCriterion(DbSqlQueryBuilder query) {
        if (pathCriteria.isEmpty()) {
            return;
        }

        VfsQueryPathCriterionDbImpl mainCriterion;
        // Count the path depth that the depth will be equal or greater than if some all path value appears
        int fixedDepth;
        boolean hasAllPathValue = false;
        if (pathCriteria.size() == 1) {
            mainCriterion = pathCriteria.get(0);
            String pathValue = (String) mainCriterion.value;
            VfsComparatorType comp = mainCriterion.getComparator();
            if (comp == VfsComparatorType.ANY || ALL_PATH_VALUE.equals(pathValue)) {
                // Basically no path filter only one and full path
                return;
            }

            // If not a single star needs at least one folder level
            if (!"*".equals(pathValue)) {
                fixedDepth = 1;
            } else {
                // Can be 0 or 1
                fixedDepth = 0;
            }
            if (comp == VfsComparatorType.CONTAINS) {
                StringBuilder builder = new StringBuilder();
                DbQueryHelper.addPathValue(builder, pathValue);
                pathValue = builder.toString();
            }
            mainCriterion.value = pathValue;
        } else {
            fixedDepth = pathCriteria.size();
            boolean hasContains = false;
            for (VfsQueryPathCriterionDbImpl criterion : pathCriteria) {
                VfsComparatorType comp = criterion.getComparator();
                if (comp == VfsComparatorType.CONTAINS
                        || comp == VfsComparatorType.ANY) {
                    hasContains = true;
                    if (!hasAllPathValue && ((String) criterion.value).contains(ALL_PATH_VALUE)) {
                        hasAllPathValue = true;
                    }
                }
            }
            if (hasContains) {
                mainCriterion = new VfsQueryPathCriterionDbImpl(VfsComparatorType.CONTAINS, "");
            } else {
                mainCriterion = new VfsQueryPathCriterionDbImpl(VfsComparatorType.EQUAL, "");
            }
            StringBuilder pathFilterValue = new StringBuilder();
            for (VfsQueryPathCriterionDbImpl criterion : pathCriteria) {
                DbQueryHelper.addPathValue(pathFilterValue, (String) criterion.value);
            }
            mainCriterion.value = pathFilterValue.toString();
            mainCriterion.nextBool = pathCriteria.get(pathCriteria.size() - 1).nextBool;
        }

        query.addNextBoolIfNeeded();

        if (expectedResultType == VfsQueryResultType.FILE) {
            fixedDepth++;
        }

        String finalPathFilterValue = (String) PathUtils.trimSlashes((String) mainCriterion.value);
        boolean endsWithAllPathValue = finalPathFilterValue.endsWith("/%") && hasAllPathValue;
        if (endsWithAllPathValue && fixedDepth > 0) {
            fixedDepth--;
        }

        if (hasAllPathValue) {
            // Use greater than in depth
            if (fixedDepth == 0) {
                // Nothing to do depth always positive
            } else {
                query.append("nodes.depth >= ");
                query.addParam(fixedDepth);
                query.nextBool = VfsBoolType.AND;
            }
        } else {
            if (expectedResultType == VfsQueryResultType.ANY_ITEM) {
                query.append("nodes.depth IN ");
                query.addListParam(Lists.asList(fixedDepth, fixedDepth + 1, new Integer[0]));
                query.nextBool = VfsBoolType.AND;
            } else {
                query.append("nodes.depth = ");
                query.addParam(fixedDepth);
                query.nextBool = VfsBoolType.AND;
            }
        }

        boolean addedOrPathFilter = false;
        if (endsWithAllPathValue) {
            // Needs to check also for equals without /% (full path value param)
            String subPathFilter = finalPathFilterValue.substring(0, finalPathFilterValue.length() - 2);
            if ("%".equals(subPathFilter)) {
                // All path goes => No filter
            } else {
                addedOrPathFilter = true;
                query.addNextBoolIfNeeded();
                query.append('(');
                if (DbQueryHelper.hasWildcards(subPathFilter)) {
                    query.append("nodes.node_path LIKE ");
                    query.addParam(subPathFilter);
                } else {
                    query.append("nodes.node_path = ");
                    query.addParam(subPathFilter);
                }
                query.nextBool = VfsBoolType.OR;
            }
        }

        query.addNextBoolIfNeeded();
        query.append("nodes.node_path ");
        query.append(mainCriterion.getComparator().str).append(' ');
        query.addParam(finalPathFilterValue);
        query.nextBool = mainCriterion.nextBool;
        if (addedOrPathFilter) {
            query.append(')');
        }
    }

    private void fillLimitCriterion(DbSqlQueryBuilder query, int limit) {
        if (limit == Integer.MAX_VALUE) {
            return;
        }

        DbService dbService = StorageContextHelper.get().beanForType(DbService.class);
        DbType databaseType = dbService.getDatabaseType();
        if (databaseType == DbType.ORACLE) {
            query.addNextBoolIfNeeded();
            query.append("ROWNUM <= ").append(String.valueOf(limit));
            query.nextBool = VfsBoolType.AND;
        }
    }


    static class OrderBy {
        String propertyName;
        boolean ascending;

        OrderBy(String propertyName, boolean ascending) {
            this.propertyName = propertyName;
            this.ascending = ascending;
        }
    }

}
