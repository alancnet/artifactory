package org.artifactory.aql.api.internal;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gidi Shabat
 */
public class AqlApiDynamicFieldsDomains {
    public static class AqlApiItemDynamicFieldsDomains<T extends AqlBase> {

        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiItemDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> size() {
            return new AqlApiComparator(AqlFieldEnum.itemSize, domains);
        }

        public AqlApiComparator<T> updated() {
            return new AqlApiComparator(AqlFieldEnum.itemUpdated, domains);
        }

        public AqlApiComparator<T> repo() {
            return new AqlApiComparator(AqlFieldEnum.itemRepo, domains);
        }

        public AqlApiComparator<T> path() {
            return new AqlApiComparator(AqlFieldEnum.itemPath, domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator(AqlFieldEnum.itemName, domains);
        }

        public AqlApiComparator<T> type() {
            return new AqlApiComparator(AqlFieldEnum.itemType, domains);
        }

        public AqlApiComparator<T> created() {
            return new AqlApiComparator(AqlFieldEnum.itemCreated, domains);
        }

        public AqlApiComparator<T> createdBy() {
            return new AqlApiComparator(AqlFieldEnum.itemCreatedBy, domains);
        }

        public AqlApiComparator<T> modified() {
            return new AqlApiComparator(AqlFieldEnum.itemModified, domains);
        }

        public AqlApiComparator<T> modifiedBy() {
            return new AqlApiComparator(AqlFieldEnum.itemModifiedBy, domains);
        }

        public AqlApiComparator<T> sha1Actual() {
            return new AqlApiComparator(AqlFieldEnum.itemActualSha1, domains);
        }

        public AqlApiComparator<T> sha1Orginal() {
            return new AqlApiComparator(AqlFieldEnum.itemOriginalSha1, domains);
        }

        public AqlApiComparator<T> md5Actual() {
            return new AqlApiComparator(AqlFieldEnum.itemActualMd5, domains);
        }

        public AqlApiComparator<T> md5Orginal() {
            return new AqlApiComparator(AqlFieldEnum.itemOriginalMd5, domains);
        }

        public AqlApiArchiveDynamicFieldsDomains<T> archive() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.archives);
            return new AqlApiArchiveDynamicFieldsDomains(tempDomains);
        }

        public AqlApiItemPropertyDynamicFieldsDomains<T> property() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.properties);
            return new AqlApiItemPropertyDynamicFieldsDomains(tempDomains);
        }

        public AqlApiStatisticDynamicFieldsDomains<T> statistic() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.statistics);
            return new AqlApiStatisticDynamicFieldsDomains<T>(tempDomains);
        }

        public AqlApiArtifactDynamicFieldsDomains<T> artifact() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.artifacts);
            return new AqlApiArtifactDynamicFieldsDomains(tempDomains);
        }

        public AqlApiDependencyDynamicFieldsDomains<T> dependency() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.dependencies);
            return new AqlApiDependencyDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiArchiveDynamicFieldsDomains<T extends AqlBase> {
        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiArchiveDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator(AqlFieldEnum.archiveEntryName, domains);
        }

        public AqlApiComparator<T> path() {
            return new AqlApiComparator(AqlFieldEnum.archiveEntryPath, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains(tempDomains);
        }

    }

    public static class AqlApiBuildDynamicFieldsDomains<T extends AqlBase> {

        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiBuildDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator(AqlFieldEnum.buildName, domains);
        }

        public AqlApiComparator<T> number() {
            return new AqlApiComparator(AqlFieldEnum.buildNumber, domains);
        }

        public AqlApiModuleDynamicFieldsDomains<T> module() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.modules);
            return new AqlApiModuleDynamicFieldsDomains(tempDomains);
        }

        public AqlApiBuildPropertyDynamicFieldsDomains<T> property() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.buildProperties);
            return new AqlApiBuildPropertyDynamicFieldsDomains(tempDomains);
        }

    }

    public static class AqlApiArtifactDynamicFieldsDomains<T extends AqlBase> {
        private ArrayList<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiArtifactDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator(AqlFieldEnum.buildArtifactName, domains);
        }

        public AqlApiComparator<T> type() {
            return new AqlApiComparator(AqlFieldEnum.buildArtifactType, domains);
        }

        public AqlApiComparator<T> sha1() {
            return new AqlApiComparator(AqlFieldEnum.buildArtifactSha1, domains);
        }

        public AqlApiComparator<T> md5() {
            return new AqlApiComparator(AqlFieldEnum.buildArtifactMd5, domains);
        }

        public AqlApiModuleDynamicFieldsDomains<T> module() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.modules);
            return new AqlApiModuleDynamicFieldsDomains(tempDomains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiDependencyDynamicFieldsDomains<T extends AqlBase> {

        private ArrayList<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiDependencyDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator(AqlFieldEnum.buildDependencyName, domains);
        }

        public AqlApiComparator<T> scope() {
            return new AqlApiComparator(AqlFieldEnum.buildDependencyScope, domains);
        }

        public AqlApiComparator<T> type() {
            return new AqlApiComparator(AqlFieldEnum.buildDependencyType, domains);
        }

        public AqlApiComparator<T> sha1() {
            return new AqlApiComparator(AqlFieldEnum.buildDependencySha1, domains);
        }

        public AqlApiComparator<T> md5() {
            return new AqlApiComparator(AqlFieldEnum.buildDependencyMd5, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains(tempDomains);
        }

        public AqlApiModuleDynamicFieldsDomains<T> module() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.modules);
            return new AqlApiModuleDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiModuleDynamicFieldsDomains<T extends AqlBase> {
        private List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiModuleDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> name() {
            return new AqlApiComparator(AqlFieldEnum.moduleName, domains);
        }

        public AqlApiBuildDynamicFieldsDomains<T> build() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.builds);
            return new AqlApiBuildDynamicFieldsDomains(tempDomains);
        }

        public AqlApiArtifactDynamicFieldsDomains<T> artifact() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.artifacts);
            return new AqlApiArtifactDynamicFieldsDomains(tempDomains);
        }

        public AqlApiDependencyDynamicFieldsDomains<T> dependecy() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.dependencies);
            return new AqlApiDependencyDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiItemPropertyDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiItemPropertyDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlBase.PropertyCriteriaClause<T> property(String key, AqlComparatorEnum comparator, String value) {
            return new AqlBase.PropertyCriteriaClause(key, comparator, value, domains);
        }

        public AqlApiComparator<T> key() {
            return new AqlApiComparator(AqlFieldEnum.propertyKey, domains);
        }

        public AqlApiComparator<T> value() {
            return new AqlApiComparator(AqlFieldEnum.propertyValue, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiBuildPropertyDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiBuildPropertyDynamicFieldsDomains(List<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> key() {
            return new AqlApiComparator(AqlFieldEnum.buildPropertyKey, domains);
        }

        public AqlApiComparator<T> value() {
            return new AqlApiComparator(AqlFieldEnum.buildPropertyValue, domains);
        }

        public AqlApiBuildDynamicFieldsDomains<T> build() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.builds);
            return new AqlApiBuildDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiStatisticDynamicFieldsDomains<T extends AqlBase> {
        private final List<AqlDomainEnum> domains = Lists.newArrayList();

        public AqlApiStatisticDynamicFieldsDomains(ArrayList<AqlDomainEnum> domains) {
            this.domains.addAll(domains);
        }

        public AqlApiComparator<T> downloads() {
            return new AqlApiComparator(AqlFieldEnum.statDownloads, domains);
        }

        public AqlApiItemDynamicFieldsDomains<T> item() {
            ArrayList<AqlDomainEnum> tempDomains = Lists.newArrayList(domains);
            tempDomains.add(AqlDomainEnum.items);
            return new AqlApiItemDynamicFieldsDomains(tempDomains);
        }
    }

    public static class AqlApiComparator<T extends AqlBase> {
        protected final List<AqlDomainEnum> domains = Lists.newArrayList();
        protected AqlFieldEnum fieldEnum;

        public AqlApiComparator(AqlFieldEnum fieldEnum, List<AqlDomainEnum> domains) {
            this.fieldEnum = fieldEnum;
            this.domains.addAll(domains);
        }

        public AqlBase.CriteriaClause<T> matches(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.matches, "" + value);
        }

        public AqlBase.CriteriaClause<T> matches(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.matches, "" + value);
        }

        public AqlBase.CriteriaClause<T> matches(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.matches, dateString);
        }

        public AqlBase.CriteriaClause<T> matches(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.matches, value);
        }

        public AqlBase.CriteriaClause<T> notMatches(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notMatches, "" + value);
        }

        public AqlBase.CriteriaClause<T> notMatches(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notMatches, "" + value);
        }

        public AqlBase.CriteriaClause<T> notMatches(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notMatches, dateString);
        }

        public AqlBase.CriteriaClause<T> notMatches(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notMatches, value);
        }


        public AqlBase.CriteriaClause<T> equals(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.equals, "" + value);
        }

        public AqlBase.CriteriaClause<T> equals(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.equals, "" + value);
        }

        public AqlBase.CriteriaClause<T> equals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.equals, dateString);
        }

        public AqlBase.CriteriaClause<T> equal(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.equals, value);
        }


        public AqlBase.CriteriaClause<T> notEquals(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> notEquals(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> notEquals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notEquals, dateString);
        }

        public AqlBase.CriteriaClause<T> notEquals(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.notEquals, value);
        }


        public AqlBase.CriteriaClause<T> greater(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greater, "" + value);
        }

        public AqlBase.CriteriaClause<T> greater(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greater, "" + value);
        }

        public AqlBase.CriteriaClause<T> greater(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greater, dateString);
        }

        public AqlBase.CriteriaClause<T> greater(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greater, value);
        }


        public AqlBase.CriteriaClause<T> greaterEquals(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greaterEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> greaterEquals(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greaterEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> greaterEquals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greaterEquals, dateString);
        }

        public AqlBase.CriteriaClause<T> greaterEquals(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.greaterEquals, value);
        }


        public AqlBase.CriteriaClause<T> less(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.less, "" + value);
        }

        public AqlBase.CriteriaClause<T> less(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.less, "" + value);
        }

        public AqlBase.CriteriaClause<T> less(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.less, dateString);
        }

        public AqlBase.CriteriaClause<T> less(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.less, value);
        }

        public AqlBase.CriteriaClause<T> lessEquals(int value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.lessEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> lessEquals(long value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.lessEquals, "" + value);
        }

        public AqlBase.CriteriaClause<T> lessEquals(DateTime value) {
            String dateString = convertDateToString(value);
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.lessEquals, dateString);
        }

        public AqlBase.CriteriaClause<T> lessEquals(String value) {
            return new AqlBase.CriteriaClause(fieldEnum, domains, AqlComparatorEnum.lessEquals, value);
        }

        private static String convertDateToString(DateTime date) {
            return ISODateTimeFormat.dateTime().print(date);
        }
    }
}
