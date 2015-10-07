package org.artifactory.sapi.search;

import org.artifactory.fs.ItemInfo;

/**
 * Date: 8/5/11
 * Time: 10:52 PM
 *
 * @author Fred Simon
 */
public interface VfsQueryRow {
    ItemInfo getItem();

    Iterable<ArchiveEntryRow> getArchiveEntries();
}
