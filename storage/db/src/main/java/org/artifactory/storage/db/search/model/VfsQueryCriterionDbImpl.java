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

import org.apache.commons.lang.StringUtils;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.sapi.search.VfsBoolType;
import org.artifactory.sapi.search.VfsComparatorType;
import org.artifactory.sapi.search.VfsDateFieldName;
import org.artifactory.sapi.search.VfsFunctionType;
import org.artifactory.sapi.search.VfsQueryFieldType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Date: 8/6/11
 * Time: 12:01 PM
 *
 * @author Fred Simon
 */
class VfsQueryCriterionDbImpl extends BaseVfsQueryCriterion {
    private final VfsQueryFieldType fieldType;
    private final String propertyName;
    private VfsComparatorType comparator = null;
    Object value;
    VfsFunctionType function = VfsFunctionType.NONE;

    VfsQueryCriterionDbImpl(String propertyName) {
        super();
        if (StringUtils.isEmpty(propertyName)) {
            throw new IllegalArgumentException("Cannot accept null or empty property name!");
        }
        this.propertyName = propertyName;
        if ("node_name".equals(propertyName)
                || "node_path".equals(propertyName)
                || VfsDateFieldName.LAST_MODIFIED.propName.equals(propertyName)
                || VfsDateFieldName.CREATED.propName.equals(propertyName)) {
            fieldType = VfsQueryFieldType.BASE_NODE;
        } else if ("entry_path".equals(propertyName)) {
            fieldType = VfsQueryFieldType.ARCHIVE_PATH;
        } else if ("entry_name".equals(propertyName)) {
            fieldType = VfsQueryFieldType.ARCHIVE_NAME;
        } else if (VfsDateFieldName.LAST_DOWNLOADED.propName.equals(propertyName)) {
            fieldType = VfsQueryFieldType.STATISTIC;
        } else if (propertyName.startsWith(ChecksumType.sha1.name())
                || propertyName.startsWith(ChecksumType.md5.name())) {
            fieldType = VfsQueryFieldType.CHECKSUM;
        } else {
            fieldType = VfsQueryFieldType.PROPERTY;
        }
    }

    public VfsComparatorType getComparator() {
        if (comparator == null) {
            if (value == null || StringUtils.isBlank(value.toString())) {
                return VfsComparatorType.ANY;
            } else {
                if (DbQueryHelper.hasWildcards(value.toString())) {
                    return VfsComparatorType.CONTAINS;
                } else {
                    return VfsComparatorType.EQUAL;
                }
            }
        }
        return comparator;
    }

    public void setComparator(@Nonnull VfsComparatorType comparator) {
        this.comparator = comparator;
    }

    public void setValue(Object value) {
        if (value != null && value instanceof String[]) {
            String[] vals = (String[]) value;
            if (vals.length == 0) {
                this.value = null;
            }
            if (vals.length == 1) {
                this.value = vals[0];
            } else {
                this.value = Arrays.asList(vals);
            }
        } else {
            this.value = value;
        }
    }

    public void setFunction(@Nonnull VfsFunctionType function) {
        this.function = function;
    }

    @Override
    public boolean hasPropertyFilter() {
        return fieldType.isProperty();
    }

    @Override
    public boolean hasStatisticFilter() {
        return fieldType.isStatistic();
    }

    @Override
    public boolean isValid() {
        VfsComparatorType comp = getComparator();
        if (comp == VfsComparatorType.NONE || comp == VfsComparatorType.ANY) {
            // No need for value sor always true
            return true;
        }
        if (value == null ||
                !(value instanceof Calendar || value instanceof Long
                        || value instanceof String || value instanceof Iterable)) {
            // Un-recognized type
            return false;
        }
        return !StringUtils.isEmpty(value.toString());
    }

    protected void fillPropertyName(DbSqlQueryBuilder query) {
        switch (fieldType) {
            case PROPERTY:
                query.appendNodePropsTableName().append(".prop_key = ");
                query.addParam(propertyName);
                query.append("AND ").appendNodePropsTableName().append(".prop_value");
                break;
            case CHECKSUM:
            case BASE_NODE:
            case PATH:
                query.append("nodes.").append(propertyName);
                break;
            case ARCHIVE_PATH:
                query.append("archive_paths.").append(propertyName);
                break;
            case ARCHIVE_NAME:
                query.append("archive_names.").append(propertyName);
                break;
            case STATISTIC:
                query.append("stats.").append(propertyName);
                break;
        }
    }

    @Override
    protected VfsBoolType fill(DbSqlQueryBuilder query) {
        VfsComparatorType comp = getComparator();

        query.addNextBoolIfNeeded();

        if (fieldType == VfsQueryFieldType.PROPERTY) {
            query.usedPropertyCriteria++;
            if (query.usedPropertyCriteria > 1) {
                query.append("EXISTS (SELECT ").appendNodePropsTableName().append(".node_id ")
                        .append("FROM node_props ").appendNodePropsTableName()
                        .append(" WHERE nodes.node_id = ").appendNodePropsTableName().append(".node_id")
                        .append(" AND ");
            } else {
                query.append('(');
            }
        }
        fillPropertyName(query);
        query.appendIfNeeded(' ');
        query.append(comp.str).append(' ');
        if (comp.acceptValue()) {
            boolean applyFunction = comp.acceptFunction() && VfsFunctionType.NONE != function;
            if (applyFunction) {
                query.append(function.str).append('(');
            }
            if (isValid()) {
                if (value instanceof String) {
                    query.addParam(DbQueryHelper.convertWildcards((String) value));
                } else if (value instanceof Calendar) {
                    query.addParam(((Calendar) value).getTimeInMillis());
                } else {
                    query.addParam(value);
                }
            }
            if (applyFunction) {
                query.append(')');
            }
        }

        if (fieldType == VfsQueryFieldType.PROPERTY) {
            query.append(')');
            if (nextBool == VfsBoolType.OR) {
                // No need to select exists
                query.usedPropertyCriteria = 0;
            }
        }

        return nextBool;
    }
}
