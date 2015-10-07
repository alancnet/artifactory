package org.artifactory.storage.db.aql.parser.elements.low.level;

import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public abstract class LazyParserElement implements ParserElement {
    private ParserElement element;

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        ParserElementResultContainer[] possiblePaths = element.peelOff(queryRemainder, context);
        if (isVisibleInResult()) {
            for (ParserElementResultContainer path : possiblePaths) {
                path.add(this, path.getElement());
            }
        }
        return possiblePaths;
    }

    @Override
    public void initialize() {
        if (element == null) {
            element = init();
            element.initialize();
        }
    }

    protected abstract ParserElement init();

    public boolean isVisibleInResult() {
        return false;
    }

    public ForkParserElement fork(ParserElement... elements) {
        return new ForkParserElement(elements);
    }

    public ParserElement forward(ParserElement... elements) {
        return new ForwardElement(elements);
    }

    public List<String> next() {
        return element.next();
    }
}
