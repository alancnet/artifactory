package org.artifactory.sapi.search;

/**
 * Date: 8/5/11
 * Time: 6:47 PM
 *
 * @author Fred Simon
 */
public interface VfsQueryResult {
    long getCount();

    Iterable<VfsQueryRow> getAllRows();
}
