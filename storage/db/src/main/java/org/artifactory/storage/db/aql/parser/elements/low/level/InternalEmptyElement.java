package org.artifactory.storage.db.aql.parser.elements.low.level;


import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class InternalEmptyElement extends InternalParserElement {

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        context.update(queryRemainder);
        return new ParserElementResultContainer[]{new ParserElementResultContainer(queryRemainder, "")};
    }

    @Override
    public List<String> next() {
        return Lists.newArrayList("<empty>");
    }
}
