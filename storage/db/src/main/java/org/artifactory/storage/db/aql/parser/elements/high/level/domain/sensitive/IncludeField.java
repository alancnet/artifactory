package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class IncludeField extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        DynamicField dynamicField = provide(DynamicField.class);
        DynamicDomain dynamicDomain = provide(DynamicDomain.class);
        DynamicValue dynamicValue = provide(DynamicValue.class);
        return forward(AqlParser.quotes, fork(dynamicField, dynamicDomain, dynamicValue), AqlParser.quotes);
    }
}
