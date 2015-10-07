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

package org.artifactory.addon.wicket;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.spring.InternalArtifactoryContext;
import org.easymock.EasyMock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the behavior of {@link WicketAddonsImpl}
 *
 * @author Natan Schochet
 */
@Test
public class WicketAddonsImplTest {

    private WicketAddonsImpl wicketAddons = new WicketAddonsImpl();
    private AddonsManager addonsManager;

    @BeforeClass
    public void setUp() {
        addonsManager = EasyMock.createMock(AddonsManager.class);
        InternalArtifactoryContext contextMock = EasyMock.createMock(InternalArtifactoryContext.class);
        EasyMock.expect(contextMock.beanForType(AddonsManager.class)).andReturn(addonsManager).anyTimes();
        EasyMock.replay(contextMock);
        ArtifactoryContextThreadBinder.bind(contextMock);
    }

    @AfterClass
    public void tearDown() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @BeforeMethod
    public void reset() {
        EasyMock.reset(addonsManager);
    }

    @Test
    public void testValidateLicenseTargetIsTrial() throws Exception {
        EasyMock.expect(addonsManager.isLicenseInstalled()).andReturn(true);
        EasyMock.expect(addonsManager.getLicenseDetails()).andReturn(new String[]{"", "", "Trial"});
        EasyMock.replay(addonsManager);
        wicketAddons.validateTargetHasDifferentLicenseKeyHash("123", null);
        EasyMock.verify(addonsManager);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            ".*open-source Artifactory.*")
    public void testValidateLicenseTargetIsnullOSS() throws Exception {
        EasyMock.expect(addonsManager.isLicenseInstalled()).andReturn(true);
        EasyMock.expect(addonsManager.getLicenseDetails()).andReturn(new String[]{"", "", "Commercial"});
        EasyMock.replay(addonsManager);
        wicketAddons.validateTargetHasDifferentLicenseKeyHash(null, null);
        EasyMock.verify(addonsManager);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            ".*user must have deploy permissions.*")
    public void testValidateLicenseTargetIsnullPro() throws Exception {
        EasyMock.expect(addonsManager.isLicenseInstalled()).andReturn(true);
        EasyMock.expect(addonsManager.getLicenseDetails()).andReturn(new String[]{"", "", "Commercial"});
        EasyMock.replay(addonsManager);
        wicketAddons.validateTargetHasDifferentLicenseKeyHash(null, Lists.newArrayList("replication"));
        EasyMock.verify(addonsManager);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp =
            ".*same-license servers.*")
    public void testValidateLicenseTargetISame() throws Exception {
        EasyMock.expect(addonsManager.isLicenseInstalled()).andReturn(true);
        EasyMock.expect(addonsManager.getLicenseDetails()).andReturn(new String[]{"", "", "Commercial"});
        EasyMock.expect(addonsManager.getLicenseKeyHash()).andReturn("123");
        EasyMock.replay(addonsManager);
        wicketAddons.validateTargetHasDifferentLicenseKeyHash("123", null);
        EasyMock.verify(addonsManager);
    }
}
