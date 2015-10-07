package org.artifactory.storage.db.aql.parser.elements.low.level;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.AqlParserContext;
import org.artifactory.storage.db.aql.parser.ParserElementResultContainer;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class InternalNumberElement extends InternalParserElement {
    @Override
    public ParserElementResultContainer[] peelOff(String queryRemainder, AqlParserContext context) {
        int min = Integer.MAX_VALUE;
        String string;
        // It is not string wrapped by semicolon therefore try to resolve string using delimiters
        for (String delimiter : AqlParser.DELIMITERS) {
            if (".".equals(delimiter)) {
                continue;
            }
            int i = queryRemainder.indexOf(delimiter);
            if (i >= 0 && i < min) {
                min = i;
            }
        }
        if ((min == Integer.MAX_VALUE && StringUtils.isBlank(queryRemainder)) || min == 0) {
            return new ParserElementResultContainer[0];
        }

        if (min != Integer.MAX_VALUE) {
            string = queryRemainder.substring(0, min).trim();

        } else {
            string = queryRemainder.trim();
        }
        for (String usedKey : AqlParser.USED_KEYS) {
            if (string.equals(usedKey)) {
                return new ParserElementResultContainer[0];
            }
        }
        try {
            // Just to make sure the number format is valid.
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(string);
        } catch (Exception e) {
            return new ParserElementResultContainer[0];
        }
        String trim = StringUtils.replaceOnce(queryRemainder, string, "").trim();
        context.update(trim);
        return new ParserElementResultContainer[]{new ParserElementResultContainer(trim, string)};
    }

    @Override
    public List<String> next() {
        List<String> result = Lists.newArrayList();
        result.add("<number>");
        return result;
    }
}
