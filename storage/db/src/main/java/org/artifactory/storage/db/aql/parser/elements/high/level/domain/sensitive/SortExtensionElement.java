package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class SortExtensionElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        DynamicField dynamicField = provide(DynamicField.class);
        DynamicFieldTrail dynamicFieldTrail = provide(DynamicFieldTrail.class);
        ParserElement json = forward(quotes, sortType, quotes, colon, openParenthesis,
                forward(quotes, dynamicField, quotes, dynamicFieldTrail), closeParenthesis);
        return forward(new InternalNameElement("sort"), openBrackets, openCurlyBrackets, json, closedCurlyBrackets,
                closeBrackets);
    }
}
