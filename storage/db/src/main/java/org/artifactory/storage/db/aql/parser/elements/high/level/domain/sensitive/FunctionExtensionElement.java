package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class FunctionExtensionElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        FilterComplexTailElement filterComplexTail = provide(FilterComplexTailElement.class);
        FilterComplexElement filterComplex = provide(FilterComplexElement.class);
        return forward(quotes, provide(FunctionElement.class), quotes, colon, openParenthesis,
                fork(filterComplex, forward(filterComplex, filterComplexTail)), closeParenthesis);
    }
}
