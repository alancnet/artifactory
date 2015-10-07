package org.artifactory.repo.cache.expirable.vcs;

import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.cache.expirable.CacheExpirable;
import org.springframework.stereotype.Component;

/**
 * @author Shay Yaakov
 */
@Component
public class GitRefsCacheExpirable implements CacheExpirable {
    @Override
    public boolean isExpirable(LocalCacheRepo localCacheRepo, String path) {
        return path.endsWith("gitrefs");
    }
}
