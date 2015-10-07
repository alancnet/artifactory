package org.artifactory.storage.db.aql.parser.elements.high.level.basic.language;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.items;
import static org.artifactory.aql.model.AqlDomainEnum.statistics;
import static org.artifactory.storage.db.aql.parser.AqlParser.dot;
import static org.artifactory.storage.db.aql.parser.AqlParser.itemValues;

/**
 * @author Gidi Shabat
 */
public class StatisticsValuesElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(forward(new InternalNameElement(items.signatue), dot, itemValues)));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return statistics;
    }
}
