package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class RealFieldElement extends LazyParserElement implements DomainProviderElement {
    private String signature;
    // The Field domain represent the domain that contains the field
    // Example the fieldDomain of "repo" is "item" but the fieldDomain of name might be "item","archive","artifact"...
    // Please note that the fieldDomain is not the query domain which is declared in the beginning of the query
    private AqlDomainEnum fieldDomain;

    public RealFieldElement(String signature, AqlDomainEnum domain) {
        this.signature = signature;
        this.fieldDomain = domain;
    }

    @Override
    protected ParserElement init() {
        return new InternalNameElement(signature);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return fieldDomain;
    }
}