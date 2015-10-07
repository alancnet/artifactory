package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.ForkParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class FindElement extends DomainSensitiveParserElement {
    @Override
    protected ParserElement init() {
        FilterComplexElement filterComplexElement = provide(FilterComplexElement.class);
        FilterComplexTailElement complexTailElement = provide(FilterComplexTailElement.class);
        ForkParserElement filter = fork(empty, forward(filterComplexElement, complexTailElement));
        return forward(new InternalNameElement("find"), openBrackets, filter, closeBrackets);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
