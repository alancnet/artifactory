package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlDomainEnum.properties;
import static org.artifactory.storage.db.aql.parser.AqlParser.dot;
import static org.artifactory.storage.db.aql.parser.AqlParser.itemValues;

/**
 * @author Gidi Shabat
 */
public class PropertyValuesElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithSubDomains(list);
        fillWithDomainFields(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        list.add(AqlParser.value);
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(items.signatue), dot, itemValues));
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