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

package org.artifactory.repo.cahce.expirable;

import com.google.common.collect.Maps;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.cache.expirable.CacheExpirable;
import org.artifactory.repo.cache.expirable.CacheExpiryImpl;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Noam Y. Tenne
 */
public class CacheExpiryImplTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testInitWithNullContext() throws Exception {
        new CacheExpiryImpl().init();
    }

    @Test
    public void testInitWithNullBeanName() throws Exception {
        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);
        EasyMock.expect(context.beansForType(CacheExpirable.class))
                .andReturn(Maps.<String, CacheExpirable>newHashMap());
        EasyMock.expect(context.getBean(EasyMock.<String>isNull())).andReturn(null);

        EasyMock.replay(context);
        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        cacheExpiry.setApplicationContext(context);
        cacheExpiry.init();
        EasyMock.verify(context);
    }

    @Test
    public void testIsExpirableWithNullPath() throws Exception {
        assertFalse(new CacheExpiryImpl().isExpirable(null, null), "Null path should never be expirable.");
    }

    @Test
    public void testIsExpirableWithValidPathAndNoExpirables() throws Exception {
        assertFalse(new CacheExpiryImpl().isExpirable(null, "afdafasdf"),
                "Nothing should be expirable when no expiry criteria is set.");
    }

    @Test
    public void testIsNotExpirable() throws Exception {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);
        CacheExpirable rootExpirable = EasyMock.createMock(CacheExpirable.class);
        CacheExpirable denyingExpirable = EasyMock.createMock(CacheExpirable.class);
        EasyMock.expect(denyingExpirable.isExpirable(EasyMock.anyObject(LocalCacheRepo.class),
                EasyMock.<String>anyObject())).andReturn(false);

        Map<String, CacheExpirable> expirableHashMap = Maps.newHashMap();
        expirableHashMap.put("root", rootExpirable);
        expirableHashMap.put("denying", denyingExpirable);

        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);
        EasyMock.expect(context.beansForType(CacheExpirable.class)).andReturn(expirableHashMap);
        EasyMock.expect(context.getBean("root")).andReturn(rootExpirable);

        EasyMock.replay(localCacheRepo, rootExpirable, denyingExpirable, context);
        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        cacheExpiry.setApplicationContext(context);
        cacheExpiry.setBeanName("root");
        cacheExpiry.init();
        assertFalse(cacheExpiry.isExpirable(localCacheRepo, "asdfasdf"), "Path should not be expirable.");
        EasyMock.verify(localCacheRepo, rootExpirable, denyingExpirable, context);
    }

    @Test
    public void testIsExpirable() throws Exception {
        LocalCacheRepo localCacheRepo = EasyMock.createMock(LocalCacheRepo.class);
        CacheExpirable rootExpirable = EasyMock.createMock(CacheExpirable.class);
        CacheExpirable acceptingExpirable = EasyMock.createMock(CacheExpirable.class);
        EasyMock.expect(acceptingExpirable.isExpirable(EasyMock.anyObject(LocalCacheRepo.class),
                EasyMock.<String>anyObject())).andReturn(true);

        Map<String, CacheExpirable> expirableHashMap = Maps.newHashMap();
        expirableHashMap.put("root", rootExpirable);
        expirableHashMap.put("accepting", acceptingExpirable);

        ArtifactoryApplicationContext context = EasyMock.createMock(ArtifactoryApplicationContext.class);
        EasyMock.expect(context.beansForType(CacheExpirable.class)).andReturn(expirableHashMap);
        EasyMock.expect(context.getBean("root")).andReturn(rootExpirable);

        EasyMock.replay(localCacheRepo, rootExpirable, acceptingExpirable, context);
        CacheExpiryImpl cacheExpiry = new CacheExpiryImpl();
        cacheExpiry.setApplicationContext(context);
        cacheExpiry.setBeanName("root");
        cacheExpiry.init();
        assertTrue(cacheExpiry.isExpirable(localCacheRepo, "asdfasdf"), "Path should be expirable.");
        EasyMock.verify(localCacheRepo, rootExpirable, acceptingExpirable, context);
    }
}