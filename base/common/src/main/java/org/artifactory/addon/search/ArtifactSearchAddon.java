package org.artifactory.addon.search;

import org.artifactory.addon.Addon;
import org.artifactory.api.search.ItemSearchResult;
import org.artifactory.api.search.SavedSearchResults;

import java.util.List;

/**
 * @author Chen Keinan
 */
public interface ArtifactSearchAddon extends Addon {

    SavedSearchResults getSearchResults(String name,List<? extends ItemSearchResult> itemSearchResults,boolean completeVersion);
}
