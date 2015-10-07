package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.closedCurlyBrackets;
import static org.artifactory.storage.db.aql.parser.AqlParser.openCurlyBrackets;

/**
 * @author Gidi Shabat
 */
public class FilterComplexElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        FilterTailElement filterTail = provide(FilterTailElement.class);
        FilterElement filter = provide(FilterElement.class);
        return fork(forward(openCurlyBrackets, fork(forward(filter, filterTail)), closedCurlyBrackets));
    }
}
