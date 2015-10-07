package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class FilterElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        return fork(provide(CriteriaEqualsKeyPropertyElement.class), provide(CriteriaEqualsValuePropertyElement.class),
                provide(CriteriaKeyPropertyElement.class), provide(CriteriaValuePropertyElement.class),
                provide(EqualsCriteriaElement.class), provide(DefaultCriteriaElement.class),
                provide(CriteriaEqualsPropertyElement.class), provide(CriteriaDefaultPropertyElement.class),
                provide(FunctionExtensionElement.class));
    }

}
