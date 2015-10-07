package org.artifactory.storage.db.aql.sql.builder.query.aql;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.DomainSensitiveField;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;

import java.util.List;
import java.util.Stack;

/**
 * The context is being used by the AqlApiToAqlQuery converter and the Parser to AqlQuery converter
 * It contains the AqlQuery that is being filled and other information needed to build the AqlQuery
 *
 * @author Gidi Shabat
 */
public class AdapterContext {
    private AqlQuery aqlQuery = new AqlQuery();
    private int tableId = SqlTable.MINIMAL_DYNAMIC_TABLE_ID;
    private Stack<AqlQueryElement> functions = new Stack<>();

    public void push(AqlQueryElement aqlQueryElement) {
        functions.push(aqlQueryElement);
    }

    public void setDomain(AqlDomainEnum domainEnum) {
        aqlQuery.setDomain(domainEnum);
    }

    public void setSort(SortDetails sortDetails) {
        aqlQuery.setSort(sortDetails);
    }

    public void addAqlQueryElements(AqlQueryElement aqlQueryElement) {
        aqlQuery.getAqlElements().add(aqlQueryElement);
    }

    public AqlQueryElement peek() {
        return functions.peek();
    }

    public AqlQueryElement pop() {
        return functions.pop();
    }

    public int provideIndex() {
        int temp = tableId;
        tableId = tableId + 1;
        return temp;
    }

    public List<AqlQueryElement> getAqlQueryElements() {
        return aqlQuery.getAqlElements();
    }

    public void addField(DomainSensitiveField field) {
        if (!aqlQuery.getResultFields().contains(field)) {
            aqlQuery.getResultFields().add(field);
        }
    }


    public Stack<AqlQueryElement> getFunctions() {
        return functions;
    }

    public AqlQuery getAqlQuery() {
        return aqlQuery;
    }

    public List<DomainSensitiveField> getResultFields() {
        return aqlQuery.getResultFields();
    }

    public void setLimit(long limit) {
        aqlQuery.setLimit(limit);
    }

    public void setOffset(long offset) {
        aqlQuery.setOffset(offset);
    }
}
