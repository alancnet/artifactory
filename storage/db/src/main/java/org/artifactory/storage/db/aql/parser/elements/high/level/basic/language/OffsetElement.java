package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class OffsetElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return forward(new InternalNameElement("offset"), openBrackets, offsetValue, closeBrackets);
    }

    @Override
    public boolean isVisibleInResult() {
        return false;
    }
}