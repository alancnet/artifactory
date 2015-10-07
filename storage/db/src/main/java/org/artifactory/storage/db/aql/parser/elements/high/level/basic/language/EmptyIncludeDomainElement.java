package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.empty;

/**
 * @author Gidi Shabat
 */
public class EmptyIncludeDomainElement extends LazyParserElement implements DomainProviderElement {
    // The Field domain represent the domain that contains the field
    // Example the fieldDomain of "repo" is "item" but the fieldDomain of name might be "item","archive","artifact"...
    // Please note that the fieldDomain is not the query domain which is declared in the beginning of the query
    private AqlDomainEnum domain;

    public EmptyIncludeDomainElement(AqlDomainEnum domain) {
        this.domain = domain;
    }

    @Override
    protected ParserElement init() {
        return empty;
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return domain;
    }
}