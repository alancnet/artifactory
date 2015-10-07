package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class DynamicFieldTrail extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        return fork(empty,
                forward(comma, quotes, provide(DynamicField.class), quotes, provide(DynamicFieldTrail.class)));
    }
}
