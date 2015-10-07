package org.artifactory.storage.db.aql.parser;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

import java.util.List;

/**
 * This context is being used in case of parser syntax error it provide an accurate location of the syntax error.
 *
 * @author Gidi Shabat
 */
public class AqlParserContext {
    private String queryRemainder;
    private List<ParserElement> elements = Lists.newArrayList();

    /**
     * Each time a parser element success(matches sub string), the parser peels off the relevant sub string from the string query
     * and update the context with the remaining query
     * @param query
     */
    public void update(String query) {
        if (this.queryRemainder == null || query.length() < this.queryRemainder.length()) {
            this.queryRemainder = query;
        }
    }

    public String getQueryRemainder() {
        return queryRemainder;
    }

    public void addElement(ParserElement element) {
        elements.add(element);
    }

    public List<ParserElement> getElements() {
        return elements;
    }
}
