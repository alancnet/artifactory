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
public class BuildModuleDomainsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        list.add(new IncludeDomainElement(modules));
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(builds.signatue),
                fork(new EmptyIncludeDomainElement(builds), forward(dot, buildDomains))));
        list.add(forward(new InternalNameElement(artifacts.signatue),
                fork(new EmptyIncludeDomainElement(artifacts), forward(dot, buildArtifactDomains))));
        list.add(forward(new InternalNameElement(dependencies.signatue),
                fork(new EmptyIncludeDomainElement(dependencies), forward(dot, buildDependenciesDomains))));
        list.add(forward(new InternalNameElement(moduleProperties.signatue),
                fork(new EmptyIncludeDomainElement(moduleProperties), forward(dot, buildModulePropertiesDomains))));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return modules;
    }
}
