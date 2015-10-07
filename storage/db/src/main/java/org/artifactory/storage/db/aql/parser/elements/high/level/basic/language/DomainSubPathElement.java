package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

/**
 * @author Gidi Shabat
 */
public class DomainSubPathElement extends LazyParserElement {
    private String domainSubPhat;

    public DomainSubPathElement(String domainSubPhat) {
        this.domainSubPhat = domainSubPhat;
    }

    @Override
    protected ParserElement init() {
        return new InternalNameElement(domainSubPhat);
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }
}