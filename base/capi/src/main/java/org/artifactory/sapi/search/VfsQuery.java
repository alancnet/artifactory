package org.artifactory.sapi.search;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Collection;

/**
 * Date: 8/5/11
 * Time: 6:04 PM
 *
 * @author Fred Simon
 */
public interface VfsQuery {
    String ALL_PATH_VALUE = "**";

    VfsQuery expectedResult(@Nonnull VfsQueryResultType itemType);

    VfsQuery setSingleRepoKey(String repoKey);

    VfsQuery setRepoKeys(Collection<String> repoKeys);

    VfsQuery orderByAscending(@Nonnull String propertyName);

    VfsQuery orderByDescending(@Nonnull String propertyName);

    VfsQuery name(@Nonnull String nodeName);

    VfsQuery archiveName(@Nonnull String entryName);

    VfsQuery archivePath(@Nonnull String entryPath);

    VfsQuery prop(@Nonnull String propertyName);

    VfsQuery comp(@Nonnull VfsComparatorType comparator);

    VfsQuery func(@Nonnull VfsFunctionType function);

    VfsQuery val(String... values);

    VfsQuery val(@Nonnull Long value);

    VfsQuery val(@Nonnull Calendar value);

    VfsQuery nextBool(@Nonnull VfsBoolType bool);

    VfsQuery startGroup();

    VfsQuery endGroup(@Nullable VfsBoolType bool);

    VfsQuery endGroup();

    VfsQuery addPathFilters(String... folderNames);

    VfsQuery addPathFilter(String pathSearch);

    @Nonnull
    VfsQueryResult execute(int limit);
}
