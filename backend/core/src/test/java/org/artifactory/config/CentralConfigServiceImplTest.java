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

package org.artifactory.config;

import com.google.common.collect.Maps;
import org.artifactory.descriptor.config.CentralConfigDescriptorImpl;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.property.PredefinedValue;
import org.artifactory.descriptor.property.Property;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the CentralConfigServiceImpl.
 *
 * @author Yossi Shaul
 */
@Test
public class CentralConfigServiceImplTest {

    public void idRefsUseTheSameObject() throws Exception {
        MutableCentralConfigDescriptor cc = new CentralConfigDescriptorImpl();
        cc.setServerName("mymy");
        cc.setDateFormat("dd-MM-yy HH:mm:ss z");

        LocalRepoDescriptor local1 = new LocalRepoDescriptor();
        local1.setKey("local1");
        Map<String, LocalRepoDescriptor> localReposMap = Maps.newLinkedHashMap();
        localReposMap.put(local1.getKey(), local1);
        cc.setLocalRepositoriesMap(localReposMap);

        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setHost("localhost");
        proxy.setKey("proxy");
        proxy.setPort(8987);
        cc.setProxies(Arrays.asList(proxy));

        HttpRepoDescriptor httpRepo = new HttpRepoDescriptor();
        httpRepo.setKey("http");
        httpRepo.setProxy(proxy);
        httpRepo.setUrl("http://blabla");
        Map<String, RemoteRepoDescriptor> map = Maps.newLinkedHashMap();
        map.put(httpRepo.getKey(), httpRepo);
        cc.setRemoteRepositoriesMap(map);

        // property sets
        PropertySet propSet = new PropertySet();
        propSet.setName("propSet1");
        Property prop = new Property();
        prop.setName("prop1");
        PredefinedValue value1 = new PredefinedValue();
        value1.setValue("value1");
        prop.addPredefinedValue(value1);
        PredefinedValue value2 = new PredefinedValue();
        value2.setValue("value2");
        prop.addPredefinedValue(value2);
        propSet.addProperty(prop);
        cc.addPropertySet(propSet);

        local1.addPropertySet(propSet);

        // set and duplicate the descriptor 
        CentralConfigServiceImpl configService = new CentralConfigServiceImpl();
        ReflectionTestUtils.setField(configService, "descriptor", cc);
        MutableCentralConfigDescriptor copy = configService.getMutableDescriptor();

        // make sure proxy object was not duplicated
        proxy = copy.getProxies().get(0);
        ProxyDescriptor httpProxy = ((HttpRepoDescriptor) copy.getRemoteRepositoriesMap().get("http")).getProxy();
        assertTrue(proxy == httpProxy, "Proxy object was duplicated!");

        // make sure the property set was not duplicated
        PropertySet propSetCopy = copy.getPropertySets().get(0);
        LocalRepoDescriptor local1Copy = copy.getLocalRepositoriesMap().get("local1");
        PropertySet propSetCopyFromRepo = local1Copy.getPropertySet("propSet1");
        assertTrue(propSetCopy == propSetCopyFromRepo, "Proxy set object was duplicated!");
    }
}
