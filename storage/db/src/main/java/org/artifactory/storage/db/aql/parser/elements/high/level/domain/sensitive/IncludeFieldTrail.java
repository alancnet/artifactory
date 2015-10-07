package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class IncludeFieldTrail extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        return fork(AqlParser.empty, forward(AqlParser.comma, provide(IncludeField.class), this));
    }

}
