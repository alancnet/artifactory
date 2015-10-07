package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlDomainEnum.properties;
import static org.artifactory.storage.db.aql.parser.AqlParser.dot;
import static org.artifactory.storage.db.aql.parser.AqlParser.itemFields;

/**
 * @author Gidi Shabat
 *         Acepts the properties fields and its sub domain
 */
public class PropertyFieldsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        AqlFieldEnum[] fields = AqlFieldEnum.getFieldByDomain(AqlDomainEnum.properties);
        for (AqlFieldEnum field : fields) {
            list.add(new RealFieldElement(field.signature, properties));
        }
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(items.signatue), dot, itemFields));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return properties;
    }
}
