package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.DomainElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive.ParserElementsProvider;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import static org.artifactory.aql.model.AqlDomainEnum.*;


/**
 * Represent the AQL Language structure
 *
 * @author Gidi Shabat
 */
public class RootElement extends LazyParserElement {

    @Override
    protected ParserElement init() {
        ParserElementsProvider provider = new ParserElementsProvider();
        return fork(
                provider.provide(DomainElement.class, items),
                provider.provide(DomainElement.class, archives),
                provider.provide(DomainElement.class, properties),
                provider.provide(DomainElement.class, statistics),
                provider.provide(DomainElement.class, artifacts),
                provider.provide(DomainElement.class, dependencies),
                provider.provide(DomainElement.class, modules),
                provider.provide(DomainElement.class, moduleProperties),
                provider.provide(DomainElement.class, buildProperties),
                provider.provide(DomainElement.class, builds));
    }
}
