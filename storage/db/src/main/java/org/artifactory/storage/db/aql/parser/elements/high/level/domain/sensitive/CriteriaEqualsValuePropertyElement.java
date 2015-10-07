package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class CriteriaEqualsValuePropertyElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        return forward(quotes, provide(DynamicStar.class), quotes, colon, quotes, value, quotes);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
