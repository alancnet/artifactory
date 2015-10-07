package org.artifactory.storage.db.aql.sql.builder.query.aql;

import com.google.common.collect.Lists;
import org.artifactory.aql.AqlException;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.aql.model.AqlSortTypeEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.basic.language.*;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.*;
import org.artifactory.storage.db.aql.sql.builder.query.sql.AqlToSqlQueryBuilderException;
import org.artifactory.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.artifactory.aql.model.AqlFieldEnum.*;

/**
 * Converts the parser results into AqlQuery
 *
 * @author Gidi Shabat
 */
public class ParserToAqlAdapter extends AqlAdapter {
    private final ResultFilterAqlElement resultFilter = new ResultFilterAqlElement();

    /**
     * Converts the parser results into AqlQuery
     * @throws AqlException
     */
    public AqlQuery toAqlModel(ParserElementResultContainer parserResult) throws AqlException {
        // Initialize the context
        ParserToAqlAdapterContext context = new ParserToAqlAdapterContext(parserResult.getAll());
        // Set the default operator that is being used if no other operator has been declared.
        context.push(and);
        // Resolve domain inf.
        handleDomainFields(context);
        // Resolve include fields info.
        handleIncludeFields(context);
        // Resolve sort info.
        handleSort(context);
        // Resolve limit info.
        handleLimit(context);
        // Resolve offset info.
        handleOffset(context);
        // Resolve Filter info
        handleFilter(context);
        // Add default filters
        injectDefaultValues(context);
        // Finally the AqlQuery is ready;
        return context.getAqlQuery();
    }

    /**
     * Converts the Criterias from the parser results into Aql criterias
     */
    private void handleFilter(ParserToAqlAdapterContext context) {
        context.resetIndex();
        while (context.hasNext()) {
            Pair<ParserElement, String> element = context.getElement();
            if (element.getFirst() instanceof EqualsCriteriaElement) {
                handleCriteriaEquals(context);
            }
            if (element.getFirst() instanceof DefaultCriteriaElement) {
                handleDefaultCriteria(context);
            }
            if (element.getFirst() instanceof CriteriaEqualsPropertyElement) {
                handleEqualsCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaEqualsKeyPropertyElement) {
                handleEqualsKeyCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaEqualsValuePropertyElement) {
                handleEqualsValueCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaKeyPropertyElement) {
                handleKeyCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaValuePropertyElement) {
                handleValueCriteriaProperty(context);
            }
            if (element.getFirst() instanceof CriteriaDefaultPropertyElement) {
                handleDefaultCriteriaProperty(context);
            }
            if (element.getFirst() instanceof FunctionElement) {
                handleFunction(context);
            }
            if (element.getFirst() instanceof CloseParenthesisElement) {
                handleCloseParenthesis(context);
            }
            if (element.getFirst() instanceof OpenParenthesisElement) {
                handleOpenParenthesis(context);
            }
            if (element.getFirst() instanceof SectionEndElement ||
                    element.getFirst() instanceof IncludeTypeElement) {
                return;
            }
            // Promote element
            context.decrementIndex(1);
        }
    }

    private void handleOpenParenthesis(ParserToAqlAdapterContext context) {
        // Add parenthesis element to the AqlQuery
        context.addAqlQueryElements(open);
    }

    private void handleIncludeFields(ParserToAqlAdapterContext context) {
        // Initialize the context
        gotoElement(IncludeExtensionElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Scan all the include domain anf fields
        context.decrementIndex(1);
        boolean first = false;
        // Prepare the context for include property filter do not worry about empty parenthesis because the AqlOptimizer will clean it
        context.push(or);
        context.push(resultFilter);
        context.addAqlQueryElements(open);
        while (!(context.getElement().getFirst() instanceof SectionEndElement)) {
            // Resolve the field sub domains
            List<AqlDomainEnum> subDomains = resolveSubDomains(context);
            if (context.getElement().getFirst() instanceof RealFieldElement) {
                // Extra field
                first = handleIncludeExtraField(context, first, subDomains);
            } else if (context.getElement().getFirst() instanceof IncludeDomainElement ||
                    context.getElement().getFirst() instanceof EmptyIncludeDomainElement) {
                // Extra domain
                handleIncludeDomain(context, subDomains);
            } else {
                //Extra property result filter
                handleIncludePropertyKeyFilter(context, subDomains);
            }
        }
        context.addAqlQueryElements(close);
        context.pop();
        context.pop();
    }

    /**
     * If the extra field belongs to the main query domain then remove all the default fields and add only the fields from
     * the include section.
     * If the extra field doesn't belongs to the main domain then just add the field to the result fields.
     */
    private boolean handleIncludeExtraField(ParserToAqlAdapterContext context, boolean overrideFields,
            List<AqlDomainEnum> subDomains) {
        // If the extra field belongs to the main domain then remove all the default fields ()
        if (!overrideFields && subDomains.size() == 1) {
            AqlDomainEnum mainDomain = subDomains.get(0);
            List<DomainSensitiveField> resultFields = context.getResultFields();
            Iterator<DomainSensitiveField> iterator = resultFields.iterator();
            while (iterator.hasNext()) {
                DomainSensitiveField next = iterator.next();
                AqlDomainEnum aqlDomainEnum = AqlDomainEnum.valueOf(next.getField().domainName);
                if (aqlDomainEnum.equals(mainDomain)) {
                    iterator.remove();
                }
            }
            overrideFields = true;
        }
        AqlFieldEnum aqlField = resolveField(context);
        DomainSensitiveField field = new DomainSensitiveField(aqlField, subDomains);
        context.addField(field);
        context.decrementIndex(1);
        return overrideFields;
    }

    /**
     * Special case for properties that allows to  add property key to return specific property
     */
    private void handleIncludePropertyKeyFilter(ParserToAqlAdapterContext context, List<AqlDomainEnum> subDomains) {
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlFieldEnum propKeyField=null;
        AqlFieldEnum propValueField=null;
        switch (aqlDomainEnum){
            case properties:propKeyField=propertyKey;propValueField=propertyValue;break;
            case moduleProperties:propKeyField=modulePropertyKey;propValueField=modulePropertyValue;break;
            case buildProperties:propKeyField=buildPropertyKey;propValueField=buildPropertyValue;break;
        }
        context.addField(new DomainSensitiveField(propKeyField, subDomains));
        context.addField(new DomainSensitiveField(propValueField, subDomains));
        String value = context.getElement().getSecond();
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.equals;
        // Only if the user has specify property key to filter then add the filter else just add the fields.
        if (!"*".equals(value)) {
            Criteria criteria = createSimpleCriteria(subDomains, propKeyField, value, comparatorEnum,
                    context);
            addCriteria(context, criteria);
        }
        context.decrementIndex(1);
    }

    /**
     * Allows to add the domain fields to the result fields
     */
    private void handleIncludeDomain(ParserToAqlAdapterContext context, List<AqlDomainEnum> subDomains) {
        DomainProviderElement element = (DomainProviderElement) context.getElement().getFirst();
        AqlDomainEnum aqlDomainEnum = element.getDomain();
        AqlFieldEnum[] fieldByDomain = AqlFieldEnum.getFieldByDomain(aqlDomainEnum);
        for (AqlFieldEnum aqlFieldEnum : fieldByDomain) {
            context.addField(new DomainSensitiveField(aqlFieldEnum, subDomains));
        }
        context.decrementIndex(1);
    }

    /**
     * Allows to add limit to the query in order to limit the number of rows returned
     */
    private void handleLimit(ParserToAqlAdapterContext context) {
        gotoElement(LimitValueElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Get the limit value from the element and set it in the context (AqlQuery)
        Pair<ParserElement, String> element = context.getElement();
        int limit = Double.valueOf(element.getSecond()).intValue();
        context.setLimit(limit);
    }


    /**
     * Allows to add limit to the query in order to limit the number of rows returned
     */
    private void handleOffset(ParserToAqlAdapterContext context) {
        gotoElement(OffsetValueElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Get the limit value from the element and set it in the context (AqlQuery)
        Pair<ParserElement, String> element = context.getElement();
        int offset = Double.valueOf(element.getSecond()).intValue();
        context.setOffset(offset);
    }

    private void handleCloseParenthesis(ParserToAqlAdapterContext context) {
        // Pop operator from operator queue
        context.pop();
        // Push close parenthesis element to context (AqlQuery)
        context.addAqlQueryElements(close);
    }

    /**
     * Handles operator "and"/"or" operators and the "freezJon"/"resultFields" functions
     */
    private void handleFunction(ParserToAqlAdapterContext context) {
        Pair<ParserElement, String> element = context.getElement();
        AqlOperatorEnum function = AqlOperatorEnum.value(element.getSecond());
        // Add leading operator if needed
        addOperatorToAqlQueryElements(context);
        if (AqlOperatorEnum.freezeJoin == function) {
            // In case of freeze join function generate new alias index for the properties tables
            // All the criterias that uses property table inside the function will use the same table.
            // Push freezeJoin to the operators queue
            context.push(new MspAqlElement(context.provideIndex()));
        } else if (AqlOperatorEnum.and == function) {
            // Push or and the operators queue
            context.push(and);
        } else if (AqlOperatorEnum.or == function) {
            // Push or to the operators queue
            context.push(or);
        } else if (AqlOperatorEnum.resultFilter == function) {
            // Push or to the operators queue
            context.push(resultFilter);
        }
    }


    private void handleCriteriaEquals(ParserToAqlAdapterContext context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomain = resolveSubDomains(context);
        // Get the criteria first variable
        AqlFieldEnum aqlField = resolveField(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String variable = resolveVariable(context);
        // Create equals criteria
        if (AqlFieldEnum.propertyKey == aqlField || AqlFieldEnum.propertyValue == aqlField||
                AqlFieldEnum.modulePropertyKey == aqlField || AqlFieldEnum.modulePropertyValue == aqlField||
                AqlFieldEnum.buildPropertyKey == aqlField || AqlFieldEnum.buildPropertyValue == aqlField) {
            Criteria criteria = createSimplePropertyCriteria(subDomain, aqlField, variable, AqlComparatorEnum.equals,
                    context);
            addCriteria(context, criteria);
        } else {
            Criteria criteria = createSimpleCriteria(subDomain, aqlField, variable, AqlComparatorEnum.equals, context);
            addCriteria(context, criteria);
        }
    }

    private void handleDefaultCriteria(ParserToAqlAdapterContext context) throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first field
        AqlFieldEnum aqlField = resolveField(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = resolveVariable(context);
        // Create criteria
        if (AqlFieldEnum.propertyKey == aqlField || AqlFieldEnum.propertyValue == aqlField||
                AqlFieldEnum.buildPropertyKey == aqlField || AqlFieldEnum.buildPropertyValue == aqlField||
                AqlFieldEnum.modulePropertyKey == aqlField || AqlFieldEnum.modulePropertyValue == aqlField) {
            Criteria criteria = createSimplePropertyCriteria(subDomains, aqlField, name2, comparatorEnum, context);
            addCriteria(context, criteria);
        } else {
            Criteria criteria = createSimpleCriteria(subDomains, aqlField, name2, comparatorEnum, context);
            addCriteria(context, criteria);
        }
    }

    private String resolveVariable(ParserToAqlAdapterContext context) {
        String second = context.getElement().getSecond();
        if ("null".equals(second.toLowerCase()) && context.getElement().getFirst() instanceof NullElement) {
            second = null;
        }
        return second;
    }

    private void handleDefaultCriteriaProperty(ParserToAqlAdapterContext context)
            throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        // Create criteria
        Criteria criteria = createComplexPropertyCriteria(subDomains, name1, name2, comparatorEnum, context);
        addCriteria(context, criteria);
    }

    private void handleKeyCriteriaProperty(ParserToAqlAdapterContext context)
            throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlFieldEnum propKeyField=null;
        switch (aqlDomainEnum){
            case properties:propKeyField=propertyKey;break;
            case moduleProperties:propKeyField=modulePropertyKey;break;
            case buildProperties:propKeyField=buildPropertyKey;break;
        }
        // Create criteria
        Criteria criteria = createSimplePropertyCriteria(subDomains, propKeyField, name1, comparatorEnum, context);
        addCriteria(context, criteria);
    }

    private void handleValueCriteriaProperty(ParserToAqlAdapterContext context)
            throws AqlException {
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        context.decrementIndex(1);
        // Get the criteria comparator
        AqlComparatorEnum comparatorEnum = AqlComparatorEnum.value(context.getElement().getSecond());
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlFieldEnum propValueField=null;
        switch (aqlDomainEnum){
            case properties:propValueField=propertyValue;break;
            case moduleProperties:propValueField=modulePropertyValue;break;
            case buildProperties:propValueField=buildPropertyValue;break;
        }
        // Create criteria
        Criteria criteria = createSimplePropertyCriteria(subDomains, propValueField, name2, comparatorEnum, context);
        addCriteria(context, criteria);
    }

    private void handleEqualsCriteriaProperty(ParserToAqlAdapterContext context)
            throws AqlException {

        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        // Create equals criteria
        Criteria criteria = createComplexPropertyCriteria(subDomains, name1, name2, AqlComparatorEnum.equals, context);
        addCriteria(context, criteria);
    }

    private void handleEqualsKeyCriteriaProperty(ParserToAqlAdapterContext context)
            throws AqlException {

        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Get the criteria first variable
        String name1 = context.getElement().getSecond();
        // Remove element from parser result elements
        context.decrementIndex(1);
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlFieldEnum propKeyField=null;
        switch (aqlDomainEnum){
            case properties:propKeyField=propertyKey;break;
            case moduleProperties:propKeyField=modulePropertyKey;break;
            case buildProperties:propKeyField=buildPropertyKey;break;
        }
        // Create equals criteria
        Criteria criteria = createSimplePropertyCriteria(subDomains, propKeyField, name1, AqlComparatorEnum.equals,
                context);
        addCriteria(context, criteria);
    }

    private void handleEqualsValueCriteriaProperty(ParserToAqlAdapterContext context)
            throws AqlException {

        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the field sub domains
        List<AqlDomainEnum> subDomains = resolveSubDomains(context);
        // Remove element from parser result elements
        context.decrementIndex(1);
        // Get the criteria second variable
        String name2 = context.getElement().getSecond();
        AqlDomainEnum aqlDomainEnum = subDomains.get(subDomains.size()-1);
        AqlFieldEnum propValueField=null;
        switch (aqlDomainEnum){
            case properties:propValueField=propertyValue;break;
            case moduleProperties:propValueField=modulePropertyValue;break;
            case buildProperties:propValueField=buildPropertyValue;break;
        }
        // Create equals criteria
        Criteria criteria = createSimplePropertyCriteria(subDomains, propValueField, name2, AqlComparatorEnum.equals,
                context);
        addCriteria(context, criteria);
    }

    private void handleSort(ParserToAqlAdapterContext context) throws AqlToSqlQueryBuilderException {
        gotoElement(SortTypeElement.class, context);
        if (!context.hasNext()) {
            return;
        }
        // Resolve the sortType from the element
        Pair<ParserElement, String> element = context.getElement();
        AqlSortTypeEnum sortTypeEnum = AqlSortTypeEnum.fromAql(element.getSecond());
        SortDetails sortDetails = new SortDetails();
        // Remove two elements from parser result elements
        context.decrementIndex(2);
        Pair<ParserElement, String> currentElement = context.getElement();
        // Get all the sort elements from the following parser elements
        while (!(currentElement.getFirst() instanceof CloseParenthesisElement)) {
            // Resolve the sub domains just to increment the pointer to the field position
            resolveSubDomains(context);
            AqlFieldEnum field = resolveField(context);
            // Remove element from parser result elements
            context.decrementIndex(1);
            sortDetails.addField(field);
            // Get the current element;
            currentElement = context.getElement();
        }
        sortDetails.setSortType(sortTypeEnum);
        // Set the sort details in the context (AqlQuery)
        context.setSort(sortDetails);
    }

    private List<AqlDomainEnum> resolveSubDomains(ParserToAqlAdapterContext context) {
        Pair<ParserElement, String> element = context.getElement();
        List<AqlDomainEnum> list = Lists.newArrayList();
        while (!(element.getFirst() instanceof RealFieldElement || element.getFirst() instanceof ValueElement ||
                element.getFirst() instanceof StarElement ||
                element.getFirst() instanceof IncludeDomainElement
                || element.getFirst() instanceof EmptyIncludeDomainElement)) {
            list.add(((DomainProviderElement) element.getFirst()).getDomain());
            context.decrementIndex(1);
            element = context.getElement();
        }
        if (element.getFirst() instanceof EmptyIncludeDomainElement) {
            list.add(((DomainProviderElement) element.getFirst()).getDomain());
        }
        return list;
    }

    private AqlFieldEnum resolveField(ParserToAqlAdapterContext context) {
        Pair<ParserElement, String> element = context.getElement();
        String fieldName = element.getSecond();
        AqlDomainEnum domain = ((DomainProviderElement) context.getElement().getFirst()).getDomain();
        return AqlFieldEnum.resolveFieldBySignatureAndDomain(fieldName, domain);
    }


    private void handleDomainFields(ParserToAqlAdapterContext context) {
        // resolve the result fields from the domain and add the field to the context (AqlQuery)
        gotoElement(DomainElement.class, context);
        context.decrementIndex(1);
        Pair<ParserElement, String> element = context.getElement();
        ArrayList<String> subdomains = Lists.newArrayList();
        while (element.getFirst() instanceof DomainSubPathElement) {
            subdomains.add(element.getSecond());
            context.decrementIndex(1);
            element = context.getElement();
        }
        AqlDomainEnum domain = AqlDomainEnum.valueFromSubDomains(subdomains);
        context.setDomain(domain);
        for (AqlFieldEnum field : domain.fields) {
            context.addField(new DomainSensitiveField(field, Lists.newArrayList(domain)));
        }
    }

    private void gotoElement(Class<? extends ParserElement> domainElementClass, ParserToAqlAdapterContext context) {
        context.resetIndex();
        while (context.hasNext() &&
                (!context.getElement().getFirst().getClass().equals(domainElementClass))) {
            context.decrementIndex(1);
        }
    }
}