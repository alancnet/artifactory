package org.artifactory.aql.api.internal;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.AqlApiElement;
import org.artifactory.aql.model.*;
import org.artifactory.aql.result.rows.AqlRowResult;
import org.artifactory.aql.result.rows.QueryTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gidi Shabat
 */
public class AqlBase<T extends AqlBase, Y extends AqlRowResult> implements AqlApiElement {

    protected SortApiElement sortApiElement = new SortApiElement();
    protected LimitApiElement limit = new LimitApiElement();
    protected OffsetApiElement offset = new OffsetApiElement();
    protected FilterApiElement filter = new FilterApiElement();
    protected DomainApiElement domain = new DomainApiElement();

    public AqlBase(Class<? extends AqlRowResult> domainClass) {
        QueryTypes annotation = domainClass.getAnnotation(QueryTypes.class);
        AqlDomainEnum domain = annotation.value();
        AqlFieldEnum[] resultField = annotation.fields();
        this.domain.setDomain(domain);
        for (AqlFieldEnum field : domain.fields) {
            this.domain.getFields().add(new DomainSensitiveField(field, Lists.newArrayList(domain)));
        }
        for (AqlFieldEnum field : resultField) {
            this.domain.getFields().add(new DomainSensitiveField(field, Lists.newArrayList(domain)));
        }
    }

    @SafeVarargs
    public static <T extends AqlBase> AndClause<T> and(AqlApiElement<T>... elements) {
        return new AndClause(elements);
    }

    @SafeVarargs
    public static <T extends AqlBase> PropertyResultFilterClause<T> propertyResultFilter(AqlApiElement<T>... elements) {
        return new PropertyResultFilterClause(elements);
    }

    @SafeVarargs
    public static <T extends AqlBase> OrClause<T> or(AqlApiElement<T>... elements) {
        return new OrClause(elements);
    }

    @SafeVarargs
    public static <T extends AqlBase> FreezeJoin<T> freezeJoin(AqlApiElement<T>... elements) {
        return new FreezeJoin(elements);
    }

    public T filter(AqlApiElement<T> filter) {
        this.filter.setFilter(filter);
        return (T) this;
    }

    @Override
    public List<AqlApiElement> get() {
        ArrayList<AqlApiElement> elements = Lists.newArrayList();
        elements.add(domain);
        elements.add(sortApiElement);
        elements.add(filter);
        elements.add(limit);
        elements.add(offset);
        return elements;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public T asc() {
        this.sortApiElement.setSortType(AqlSortTypeEnum.asc);
        return (T) this;
    }

    public T desc() {
        this.sortApiElement.setSortType(AqlSortTypeEnum.desc);
        return (T) this;
    }

    public T sortBy(AqlFieldEnum... fields) {
        this.sortApiElement.setFields(fields);
        return (T) this;
    }

    public T limit(int limit) {
        this.limit.setLimit(limit);
        return (T) this;
    }

    public T offset(int offset) {
        this.offset.setOffset(offset);
        return (T) this;
    }

    public T include(AqlApiDynamicFieldsDomains.AqlApiComparator... comparator) {
        for (AqlApiDynamicFieldsDomains.AqlApiComparator aqlApiComparator : comparator) {
            domain.getFields().add(new DomainSensitiveField(aqlApiComparator.fieldEnum, aqlApiComparator.domains));
        }

        return (T) this;
    }


    public static class FilterApiElement implements AqlApiElement {

        private AqlApiElement filter;


        @Override
        public List<AqlApiElement> get() {
            return Lists.newArrayList(filter);
        }

        @Override
        public boolean isEmpty() {
            return filter != null;
        }

        public AqlApiElement getFilter() {
            return filter;
        }

        public void setFilter(AqlApiElement filter) {
            this.filter = filter;
        }

    }

    public static class DomainApiElement implements AqlApiElement {
        private AqlDomainEnum domain;
        private List<DomainSensitiveField> fields = Lists.newArrayList();

        public List<DomainSensitiveField> getFields() {
            return fields;
        }


        @Override
        public List<AqlApiElement> get() {
            return Lists.newArrayList();
        }

        public AqlDomainEnum getDomain() {
            return domain;
        }

        public void setDomain(AqlDomainEnum domain) {
            this.domain = domain;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    public static class LimitApiElement implements AqlApiElement {
        private long limit = Long.MAX_VALUE;

        public long getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        @Override
        public List<AqlApiElement> get() {
            return Lists.newArrayList();
        }

        @Override
        public boolean isEmpty() {
            return limit < 0 && limit < Integer.MAX_VALUE;
        }
    }

    public static class OffsetApiElement implements AqlApiElement {
        private long offset = 0;

        public long getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        @Override
        public List<AqlApiElement> get() {
            return Lists.newArrayList();
        }

        @Override
        public boolean isEmpty() {
            return offset <= 0 && offset < Integer.MAX_VALUE;
        }
    }

    public static class SortApiElement implements AqlApiElement {
        private AqlSortTypeEnum sortType = AqlSortTypeEnum.desc;
        private AqlFieldEnum[] fields;

        public AqlSortTypeEnum getSortType() {
            return sortType;
        }

        public void setSortType(AqlSortTypeEnum sortType) {
            this.sortType = sortType;
        }

        public AqlFieldEnum[] getFields() {
            return fields;
        }

        public void setFields(AqlFieldEnum[] fields) {
            this.fields = fields;
        }

        @Override
        public List<AqlApiElement> get() {
            return Lists.newArrayList();
        }

        @Override
        public boolean isEmpty() {
            return sortType == null || fields == null || fields.length == 0;
        }
    }

    public static class AndClause<T extends AqlBase> implements AqlApiElement<T> {

        private final ArrayList<AqlApiElement<T>> andElements;

        public AndClause(AqlApiElement<T>[] elements) {
            this.andElements = Lists.newArrayList(elements);
        }

        @Override
        public List<AqlApiElement<T>> get() {
            return andElements;
        }

        @Override
        public boolean isEmpty() {
            return andElements.isEmpty();
        }

        public void append(AqlApiElement<T> aqlApiElement) {
            andElements.add(aqlApiElement);
        }
    }

    public static class OrClause<T extends AqlBase> implements AqlApiElement<T> {

        private final ArrayList<AqlApiElement<T>> orElements;

        public OrClause(AqlApiElement<T>[] elements) {
            this.orElements = Lists.newArrayList(elements);
        }

        @Override
        public List<AqlApiElement<T>> get() {
            return orElements;
        }

        @Override
        public boolean isEmpty() {
            return orElements.isEmpty();
        }

        public void append(AqlApiElement aqlApiElement) {
            orElements.add(aqlApiElement);
        }
    }

    public static class PropertyResultFilterClause<T extends AqlBase> implements AqlApiElement<T> {

        private final ArrayList<AqlApiElement<T>> flatElements;

        public PropertyResultFilterClause(AqlApiElement<T>[] elements) {
            this.flatElements = Lists.newArrayList(elements);
        }

        @Override
        public List<AqlApiElement<T>> get() {
            return flatElements;
        }

        @Override
        public boolean isEmpty() {
            return flatElements.isEmpty();
        }

        public void append(AqlApiElement aqlApiElement) {
            flatElements.add(aqlApiElement);
        }
    }

    public static class FreezeJoin<T extends AqlBase> implements AqlApiElement<T> {

        private final ArrayList<AqlApiElement<T>> elements;

        public FreezeJoin(AqlApiElement<T>[] elements) {
            this.elements = Lists.newArrayList(elements);
        }

        @Override
        public List<AqlApiElement<T>> get() {
            return elements;
        }

        @Override
        public boolean isEmpty() {
            return elements.isEmpty();
        }
    }

    public static class CriteriaClause<T extends AqlBase> implements AqlApiElement<T> {
        private AqlFieldEnum fieldEnum;
        private List<AqlDomainEnum> subDomains;
        private AqlComparatorEnum comparator;
        private String value;

        public CriteriaClause(AqlFieldEnum fieldEnum, List<AqlDomainEnum> subDomains, AqlComparatorEnum comparator,
                String value) {
            this.fieldEnum = fieldEnum;
            this.subDomains = subDomains;
            this.comparator = comparator;
            this.value = value;
        }

        public List<AqlDomainEnum> getSubDomains() {
            return subDomains;
        }

        public AqlFieldEnum getFieldEnum() {
            return fieldEnum;
        }

        public AqlComparatorEnum getComparator() {
            return comparator;
        }

        public String getValue() {
            return value;
        }

        @Override
        public List<AqlApiElement<T>> get() {
            return Lists.newArrayList();
        }

        @Override
        public boolean isEmpty() {
            return fieldEnum.signature != null && value != null && comparator != null;
        }
    }


    public static class PropertyCriteriaClause<T extends AqlBase> implements AqlApiElement {
        private String string1;
        private AqlComparatorEnum comparator;
        private String string2;
        private List<AqlDomainEnum> subDomains;

        public PropertyCriteriaClause(String key, AqlComparatorEnum comparator, String value,
                List<AqlDomainEnum> subDomains) {
            this.string1 = key;
            this.comparator = comparator;
            this.string2 = value;
            this.subDomains = subDomains;
        }

        public List<AqlDomainEnum> getSubDomains() {
            return subDomains;
        }

        public String getString1() {
            return string1;
        }

        public AqlComparatorEnum getComparator() {
            return comparator;
        }

        public String getString2() {
            return string2;
        }

        @Override
        public List<AqlApiElement<T>> get() {
            return Lists.newArrayList();
        }

        @Override
        public boolean isEmpty() {
            return string1 != null && string2 != null && comparator != null;
        }
    }
}
