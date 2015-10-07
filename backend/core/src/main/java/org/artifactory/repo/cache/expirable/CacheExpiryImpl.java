/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.repo.cache.expirable;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.spring.Reloadable;
import org.artifactory.storage.db.DbService;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

/**
 * Aggregates and polls all the cache expirable components when needing to determine if a path can expire
 *
 * @author Noam Y. Tenne
 */
@Service
@Reloadable(beanClass = CacheExpiry.class, initAfter = DbService.class)
public class CacheExpiryImpl implements CacheExpiry, BeanNameAware, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(CacheExpiryImpl.class);

    private ArtifactoryApplicationContext context;
    private Set<CacheExpirable> expirable = Sets.newHashSet();
    private String beanName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = ((ArtifactoryApplicationContext) applicationContext);
    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void init() {
        Collection<CacheExpirable> allExpirableIncludingMe = context.beansForType(CacheExpirable.class).values();
        Object thisAsBean = context.getBean(beanName);
        for (CacheExpirable t : allExpirableIncludingMe) {
            if (t != thisAsBean) {
                expirable.add(t);
            }
        }
        log.debug("Loaded expirable: {}", expirable);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    public boolean isExpirable(LocalCacheRepo localCacheRepo, String path) {
        if (StringUtils.isNotBlank(path)) {
            for (CacheExpirable cacheExpirable : expirable) {
                if (cacheExpirable.isExpirable(localCacheRepo, path)) {
                    return true;
                }
            }
        }

        return false;
    }
}
