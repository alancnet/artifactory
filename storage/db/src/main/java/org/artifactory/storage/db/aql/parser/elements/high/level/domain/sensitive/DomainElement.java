package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.basic.language.DomainSubPathElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.basic.language.SectionEndElement;

import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class DomainElement extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        ParserElement tail = forward(
                fork(empty, forward(dot, provide(FindElement.class), new SectionEndElement())),
                fork(empty, forward(dot, provide(IncludeExtensionElement.class), new SectionEndElement())),
                fork(empty, forward(dot, provide(SortExtensionElement.class), new SectionEndElement())),
                fork(empty, forward(dot, offset), new SectionEndElement()),
                fork(empty, forward(dot, limit), new SectionEndElement()));
        return createDomainParserElement(domain, tail);
    }

    private ParserElement createDomainParserElement(AqlDomainEnum value, ParserElement tail) {
        String[] domainSubPaths = value.subDomains;
        ParserElement[] domainNameElement = new ParserElement[domainSubPaths.length * 2];
        for (int j = 0; j < domainSubPaths.length; j++) {
            ParserElement parserElement = new DomainSubPathElement(domainSubPaths[j]);
            domainNameElement[j * 2] = parserElement;
            domainNameElement[j * 2 + 1] = dot;
        }
        domainNameElement[domainNameElement.length - 1] = tail;
        return forward(domainNameElement);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}
