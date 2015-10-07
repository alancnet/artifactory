package org.artifactory.repo.cache.expirable.vcs;

import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.repo.LocalCacheRepo;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class GitRefsCacheExpirableTest {

    public void testExpirable() throws Exception {
        LocalCacheRepoDescriptor descriptor = createMock(LocalCacheRepoDescriptor.class);

        LocalCacheRepo repo = createMock(LocalCacheRepo.class);
        expect(repo.getDescriptor()).andReturn(descriptor).anyTimes();

        replay(descriptor, repo);

        GitRefsCacheExpirable expirable = new GitRefsCacheExpirable();
        assertFalse(expirable.isExpirable(repo, "refs.tar.gz"));
        assertFalse(expirable.isExpirable(repo, "gitrefs.tgz"));
        assertFalse(expirable.isExpirable(repo, "gitrefs.zip"));
        assertTrue(expirable.isExpirable(repo, "gitrefs"));
        assertTrue(expirable.isExpirable(repo, "bla/gitrefs"));
        assertTrue(expirable.isExpirable(repo, "twbs/bootstrap/gitrefs"));

        verify(descriptor, repo);
    }
}