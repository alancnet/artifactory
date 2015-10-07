package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.*;
import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 *         Acepts the modules fields and its sub domain
 */
public class BuildModuleFieldsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        AqlFieldEnum[] fields = AqlFieldEnum.getFieldByDomain(modules);
        for (AqlFieldEnum field1 : fields) {
            list.add(new RealFieldElement(field1.signature, modules));
        }
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(builds.signatue), dot, buildFields));
        list.add(forward(new InternalNameElement(artifacts.signatue), dot, buildArtifactFields));
        list.add(forward(new InternalNameElement(dependencies.signatue), dot, buildDependenciesFields));
        list.add(forward(new InternalNameElement(moduleProperties.signatue), dot, buildModulePropertiesFields));
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
