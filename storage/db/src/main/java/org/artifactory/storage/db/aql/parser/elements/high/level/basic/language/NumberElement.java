package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNumberElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class NumberElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return new InternalNumberElement();
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
