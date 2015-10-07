package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class NullElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        return fork(new InternalNameElement("null"), new InternalNameElement("NULL"));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}