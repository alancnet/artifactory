package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.aql.model.AqlOperatorEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;

/**
 * @author Gidi Shabat
 */
public class FunctionElement extends DomainSensitiveParserElement {
    @Override
    protected ParserElement init() {
        AqlOperatorEnum[] values = AqlOperatorEnum.values();
        ParserElement[] parserElements = new ParserElement[values.length];
        for (int i = 0; i < parserElements.length; i++) {
            parserElements[i] = forward(new InternalNameElement(values[i].signature));
        }
        return fork(parserElements);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
