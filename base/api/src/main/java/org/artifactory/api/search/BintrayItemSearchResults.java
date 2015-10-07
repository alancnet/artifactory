package org.artifactory.api.search;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.List;

/**
 * Bintray Search result
 *
 * @author Gidi Shabat
 */
public class BintrayItemSearchResults<T> implements Serializable {

    @XStreamImplicit(itemFieldName = "searchResult")
    private List<T> results;
    private long rangeLimitTotal;

    public BintrayItemSearchResults(List<T> results, long rangeLimitTotal) {
        this.results = results;
        this.rangeLimitTotal = rangeLimitTotal;
    }

    public List<T> getResults() {
        return results;
    }

    public long getRangeLimitTotal() {
        return rangeLimitTotal;
    }
}