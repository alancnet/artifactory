package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.*;
import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class ItemDomainsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        list.add(new IncludeDomainElement(items));
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(archives.signatue),
                fork(new EmptyIncludeDomainElement(archives), forward(dot, archiveDomains))));
        list.add(forward(new InternalNameElement(artifacts.signatue),
                fork(new EmptyIncludeDomainElement(artifacts), forward(dot, buildArtifactDomains))));
        list.add(forward(new InternalNameElement(dependencies.signatue),
                fork(new EmptyIncludeDomainElement(dependencies), forward(dot, buildDependenciesDomains))));
        list.add(forward(new InternalNameElement(statistics.signatue),
                fork(new EmptyIncludeDomainElement(statistics), forward(dot, statisticsDomains))));
        list.add(forward(new InternalNameElement(properties.signatue),
                fork(new EmptyIncludeDomainElement(properties), forward(dot, propertiesDomains))));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return items;
    }
}
