package org.artifactory.storage.db.aql.parser;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.util.Pair;

import java.util.List;

/**
 * Each element in The parser peels off a part from the query string.
 * The ParserElementResultContainer contains the the leftover part of the query string
 * and the components that has been used to peel of the query string until this element
 *
 * @author Gidi Shabat
 */
public class ParserElementResultContainer {
    /**
     * Remainder of the original query strong to parse.
     */
    private final String queryRemainder;
    /**
     * The current element peeled off (can be empty)
     */
    private final String element;
    /**
     * The query element path leading to this intermediate result. In reverse order.
     */
    private final List<Pair<ParserElement, String>> list = Lists.newArrayList();

    public ParserElementResultContainer(String queryRemainder, String element) {
        this.queryRemainder = queryRemainder;
        this.element = element;
    }

    public String getQueryRemainder() {
        return queryRemainder;
    }

    public String getElement() {
        return element;
    }

    public void add(ParserElement element, String part) {
        list.add(new Pair<>(element, part));
    }

    public List<Pair<ParserElement, String>> getAll() {
        return list;
    }

}
