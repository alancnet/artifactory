package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.*;
import static org.artifactory.storage.db.aql.parser.AqlParser.buildModuleFields;
import static org.artifactory.storage.db.aql.parser.AqlParser.dot;

/**
 * @author Gidi Shabat
 */
public class BuildModulePropertyFieldsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        AqlFieldEnum[] fields = AqlFieldEnum.getFieldByDomain(moduleProperties);
        for (AqlFieldEnum field : fields) {
            list.add(new RealFieldElement(field.signature, moduleProperties));
        }
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(modules.signatue), dot, buildModuleFields));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return moduleProperties;
    }
}
