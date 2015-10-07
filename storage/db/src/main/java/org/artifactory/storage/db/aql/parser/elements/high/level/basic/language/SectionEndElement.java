package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalEmptyElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class SectionEndElement extends LazyParserElement {


    @Override
    protected ParserElement init() {
        return new InternalEmptyElement();
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}