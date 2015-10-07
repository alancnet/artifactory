package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.comparator;
import static org.artifactory.storage.db.aql.parser.AqlParser.quotes;

/**
 * @author Gidi Shabat
 */
public class QuotedComparator extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return forward(quotes, comparator, quotes);
    }
}