package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalValueElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class ValueElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return new InternalValueElement();
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
