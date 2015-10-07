package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalFieldElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class FieldElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return forward(new InternalFieldElement());
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
