package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.closeBrackets;
import static org.artifactory.storage.db.aql.parser.AqlParser.openBrackets;

/**
 * @author Gidi Shabat
 */
public class IncludeExtensionElement extends DomainSensitiveParserElement {
    @Override
    protected ParserElement init() {
        IncludeField includeField = provide(IncludeField.class);
        IncludeFieldTrail includeFieldTrail = provide(IncludeFieldTrail.class);
        ParserElement includeFields = forward(includeField,includeFieldTrail);
        return forward(new InternalNameElement("include"), openBrackets, includeFields, closeBrackets);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
