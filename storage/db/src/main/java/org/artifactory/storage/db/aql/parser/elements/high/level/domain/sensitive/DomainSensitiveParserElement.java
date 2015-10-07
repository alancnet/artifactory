package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public abstract class DomainSensitiveParserElement extends LazyParserElement {
    private ParserElementsProvider provider;
    protected AqlDomainEnum domain;

    protected <T extends DomainSensitiveParserElement> T provide(Class<T> elementClass) {
        return provider.provide(elementClass, domain);
    }

    public void setDomain(AqlDomainEnum domain) {
        this.domain = domain;
    }

    public void setProvider(ParserElementsProvider provider) {
        this.provider = provider;
    }
}
