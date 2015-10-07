package org.artifactory.storage.db.aql.parser.elements;


import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * The parser is actually group of parser elements that represent the language possibilities tree.
 * Each element represent intersection and its sub-tree in the possibilities tree.
 *
 * @author Gidi Shabat
 */
public interface ParserElement {


    /**
     * Returns possible matches between the queryRemainder to this parserElement.
     *
     * @param queryRemainder The left over for other parser elements to parse
     * @param context        Path resolution context
     * @return Possible parsing paths
     */
    ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context);

    /**
     * One time initialization of the parsing tree.
     * TODO: should be internal only
     */
    void initialize();

    /**
     * Returns the next possibilities
     *
     * @return Possible parsing elements
     */
    List<String> next();
}
