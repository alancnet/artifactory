package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class DefaultCriteriaElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        return forward(quotes, provide(DynamicField.class), quotes, colon, openCurlyBrackets, quotedComparator, colon,
                valueOrNumberOrNull, closedCurlyBrackets);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
