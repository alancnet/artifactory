package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.comma;
import static org.artifactory.storage.db.aql.parser.AqlParser.empty;

/**
 * @author Gidi Shabat
 */
public class FilterComplexTailElement extends DomainSensitiveParserElement {
    @Override
    protected ParserElement init() {
        return fork(empty,
                forward(comma, provide(FilterComplexElement.class), provide(FilterComplexTailElement.class)));
    }
}
