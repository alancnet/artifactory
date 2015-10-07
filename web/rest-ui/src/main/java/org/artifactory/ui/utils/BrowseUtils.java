package org.artifactory.ui.utils;

import org.artifactory.api.repo.BaseBrowsableItem;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yoav Luft
 * @author Yossi Shaul
 */
public class BrowseUtils {

    /**
     * @return A filtered list without the checksum items (md5 and sha1).
     */
    public static List<BaseBrowsableItem> filterChecksums(Collection<BaseBrowsableItem> items) {
        List<BaseBrowsableItem> filtered = items.stream().filter(i -> i != null && i.getName() != null &&
                !i.getName().endsWith(".sha1") && !i.getName().endsWith(".sha1"))
                .collect(Collectors.toList());
        return filtered;
    }
}
