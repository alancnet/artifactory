package org.artifactory.storage.db.aql.sql.builder.query.aql;

import com.google.common.collect.Lists;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.util.Pair;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class ParserToAqlAdapterContext extends AdapterContext {
    private int index;
    private List<Pair<ParserElement, String>> elements = Lists.newArrayList();

    public ParserToAqlAdapterContext(List<Pair<ParserElement, String>> elements) {
        this.elements = elements;
        index = elements.size() - 1;
    }

    public Pair<ParserElement, String> getElement() {
        return elements.get(index);
    }

    public void decrementIndex(int i) {
        index = index - i;
    }

    public int getIndex() {
        return index;
    }

    public void resetIndex() {
        index = elements.size() - 1;
    }

    public boolean hasNext() {
        return index >= 0;
    }
}
