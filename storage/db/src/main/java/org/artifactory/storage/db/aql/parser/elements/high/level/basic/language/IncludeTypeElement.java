package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class IncludeTypeElement extends LazyParserElement {
    private String name;

    public IncludeTypeElement(String name) {
        this.name = name;
    }

    @Override
    protected ParserElement init() {
        return forward(new InternalNameElement(name));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}