package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalSignElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.*;
import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 */
public class BuildModuleValuesElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(builds.signatue), dot, buildValues));
        list.add(forward(new InternalNameElement(artifacts.signatue), dot, buildArtifactValues));
        list.add(forward(new InternalNameElement(dependencies.signatue), dot, buildDependenciesValues));
        list.add(forward(new InternalSignElement("@"), buildModulePropertiesValues));
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
