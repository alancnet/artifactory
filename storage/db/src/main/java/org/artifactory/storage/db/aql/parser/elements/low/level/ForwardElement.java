package org.artifactory.storage.db.aql.parser.elements.low.level;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an ordered list of sub elements that must appear in the query string in the same order.
 *
 * @author Gidi Shabat
 */
public class ForwardElement implements ParserElement {
    private List<ParserElement> elements = Lists.newArrayList();

    public ForwardElement(ParserElement... elements) {
        Collections.addAll(this.elements, elements);
    }

    @Override
    public void initialize() {
        for (ParserElement element : elements) {
            element.initialize();
        }
    }

    @Override
    public List<String> next() {
        List<String> list = Lists.newArrayList();
        for (ParserElement element : elements) {
            List<String> next = element.next();
            if (next.size() > 0) {
                list.addAll(next);
                return list;
            }
        }
        return list;
    }

    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        List<ParserElement> tempElements = Lists.newArrayList(elements);
        ParserElementResultContainer[] results = peelOffRecursive(tempElements, new ParserElementResultContainer(
                queryRemainder, ""), context);
        return results;
    }

    private ParserElementResultContainer[] peelOffRecursive(List<ParserElement> elements,
            ParserElementResultContainer query,
            AqlParserContext context) {
        ParserElement element = elements.remove(0);
        // In order to support next letter prediction we should collect elements that successfully reached to the end of string
        if (query.getQueryRemainder().isEmpty()) {
            context.addElement(element);
        }
        ParserElementResultContainer[] results = element.peelOff(query.getQueryRemainder(), context);
        if (results.length > 0) {
            if (elements.size() == 0) {
                return results;
            }
            List<ParserElementResultContainer> tempQueries = Lists.newArrayList();
            for (ParserElementResultContainer tempQuery : results) {
                ArrayList<ParserElement> tempElements = Lists.newArrayList(elements);
                ParserElementResultContainer[] internalResults = peelOffRecursive(tempElements, tempQuery, context);
                for (ParserElementResultContainer internalResult : internalResults) {
                    for (Pair<ParserElement, String> pair : tempQuery.getAll()) {
                        ParserElement parserElement = pair.getFirst();
                        String value = pair.getSecond();
                        //if (parserElement.isAddable()) {
                        internalResult.add(parserElement, value);
                        //}
                    }
                }
                Collections.addAll(tempQueries, internalResults);
            }
            return tempQueries.toArray(new ParserElementResultContainer[tempQueries.size()]);
        } else {
            return results;
        }
    }
}
