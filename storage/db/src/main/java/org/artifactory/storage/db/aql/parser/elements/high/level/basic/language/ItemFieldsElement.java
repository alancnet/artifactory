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
 *         Acepts the items fields and its sub domain
 */
public class ItemFieldsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        AqlFieldEnum[] fields = AqlFieldEnum.getFieldByDomain(items);
        for (AqlFieldEnum field1 : fields) {
            list.add(new RealFieldElement(field1.signature, items));
        }
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(archives.signatue), dot, archiveFields));
        list.add(forward(new InternalNameElement(artifacts.signatue), dot, buildArtifactFields));
        list.add(forward(new InternalNameElement(dependencies.signatue), dot, buildDependenciesFields));
        list.add(forward(new InternalNameElement(statistics.signatue), dot, statisticsFields));
        list.add(forward(new InternalNameElement(properties.signatue), dot, propertiesFields));
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
