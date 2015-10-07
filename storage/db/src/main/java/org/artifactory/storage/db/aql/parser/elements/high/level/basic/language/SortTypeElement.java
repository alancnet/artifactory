package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.aql.model.AqlSortTypeEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class SortTypeElement extends LazyParserElement {
    @Override
    protected ParserElement init() {
        AqlSortTypeEnum[] values = AqlSortTypeEnum.values();
        ParserElement[] parserElements = new ParserElement[values.length];
        for (int i = 0; i < parserElements.length; i++) {
            parserElements[i] = new InternalNameElement(values[i].getAqlName(), true);
        }
        return fork(parserElements);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}