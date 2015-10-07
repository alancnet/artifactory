package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class ValueNumberNullElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return fork(forward(quotes, value, quotes), number, nullElement);
    }
}