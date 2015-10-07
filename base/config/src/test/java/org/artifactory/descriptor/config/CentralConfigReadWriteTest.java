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

package org.artifactory.descriptor.config;

import com.google.common.collect.Maps;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.cleanup.CleanupConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.gc.GcConfigDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.reader.CentralConfigReader;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.descriptor.security.ldap.LdapSetting;
import org.artifactory.jaxb.JaxbHelper;
import org.artifactory.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Tests the read and write of CentralConfig using the JaxbHelper.
 */
@Test
public class CentralConfigReadWriteTest {
    private static final Logger log = LoggerFactory.getLogger(CentralConfigReadWriteTest.class);

    private File testDir;

    @BeforeTest
    public void createTestOutputDir() {
        testDir = new File("target/config-test");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
    }

    public void writeCentralConfig() throws Exception {
        MutableCentralConfigDescriptor desc = new CentralConfigDescriptorImpl();
        desc.setServerName("mymy");
        desc.setDateFormat("dd-MM-yy HH:mm:ss z");

        RepoLayout repoLayout = new RepoLayout();
        repoLayout.setName("layout");
        repoLayout.setArtifactPathPattern("artifactPathPattern");
        repoLayout.setDistinctiveDescriptorPathPattern(true);
        repoLayout.setDescriptorPathPattern("descriptorPathPattern");
        desc.addRepoLayout(repoLayout);

        LocalRepoDescriptor local1 = new LocalRepoDescriptor();
        local1.setKey("local1");
        local1.setRepoLayout(repoLayout);
        local1.setBlackedOut(false);
        local1.setDescription("local repo 1");
        local1.setNotes("note1");
        local1.setExcludesPattern("");
        local1.setIncludesPattern("**/*");
        local1.setHandleReleases(true);
        local1.setHandleSnapshots(true);

        LocalRepoDescriptor local2 = new LocalRepoDescriptor();
        local2.setKey("local2");
        local2.setRepoLayout(repoLayout);
        local2.setBlackedOut(false);
        local2.setDescription("local repo 2");
        local2.setNotes("note2");
        local2.setExcludesPattern("**/*");
        local2.setIncludesPattern("**/*");
        local2.setHandleReleases(true);
        local2.setHandleSnapshots(true);
        local2.setSuppressPomConsistencyChecks(true);

        Map<String, LocalRepoDescriptor> localRepositoriesMap = Maps.newLinkedHashMap();
        localRepositoriesMap.put(local1.getKey(), local1);
        localRepositoriesMap.put(local2.getKey(), local2);
        desc.setLocalRepositoriesMap(localRepositoriesMap);

        // security
        SecurityDescriptor securityDescriptor = new SecurityDescriptor();
        securityDescriptor.setAnonAccessEnabled(true);
        desc.setSecurity(securityDescriptor);

        // ldap settings
        LdapSetting ldap = new LdapSetting();
        ldap.setKey("ldap1");
        ldap.setLdapUrl("ldap://blabla");
        securityDescriptor.setLdapSettings(Arrays.asList(ldap));

        // backups
        BackupDescriptor backup = new BackupDescriptor();
        backup.setKey("backup1");
        backup.setEnabled(false);
        backup.setCronExp("* * * * *");
        desc.setBackups(Arrays.asList(backup));

        // proxy
        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setKey("proxy1");
        proxy.setHost("localhost");
        proxy.setUsername("1111");
        proxy.setPassword("1111");
        proxy.setNtHost("nthost");
        desc.addProxy(proxy, false);

        //Property sets
        PropertySet propertySet1 = new PropertySet();
        propertySet1.setName("propertySet1");
        propertySet1.setVisible(true);
        desc.addPropertySet(propertySet1);

        PropertySet propertySet2 = new PropertySet();
        propertySet2.setName("propertySet2");
        propertySet2.setVisible(false);
        desc.addPropertySet(propertySet2);

        LocalReplicationDescriptor localReplication = new LocalReplicationDescriptor();
        localReplication.setEnabled(true);
        localReplication.setCronExp("0 0/7 * * * ?");
        localReplication.setRepoKey("local1");
        localReplication.setUrl("http://momo.com");
        localReplication.setUsername("user1");
        localReplication.setPassword("password1");
        localReplication.setEnableEventReplication(true);
        desc.addLocalReplication(localReplication);

        GcConfigDescriptor gcConfigDescriptor = new GcConfigDescriptor();
        gcConfigDescriptor.setCronExp("0 0 4 * * ?");
        desc.setGcConfig(gcConfigDescriptor);

        CleanupConfigDescriptor cleanupConfigDescriptor = new CleanupConfigDescriptor();
        cleanupConfigDescriptor.setCronExp("0 12 5 * * ?");
        desc.setCleanupConfig(cleanupConfigDescriptor);

        CleanupConfigDescriptor virtualCleanupConfigDescriptor = new CleanupConfigDescriptor();
        virtualCleanupConfigDescriptor.setCronExp("0 12 5 * * ?");
        desc.setVirtualCacheCleanupConfig(virtualCleanupConfigDescriptor);

        FolderDownloadConfigDescriptor folderDownloadConfigDescriptor = new FolderDownloadConfigDescriptor();
        desc.setFolderDownloadConfig(folderDownloadConfigDescriptor);

        File outputConfig = new File(testDir, "central.config.test.xml");
        JaxbHelper.writeConfig(desc, outputConfig);
    }

    @Test(dependsOnMethods = "writeCentralConfig")
    public void readWrittenCentralConfig() {
        File outputConfig = new File(testDir, "central.config.test.xml");
        // This needs to be here because this is for a test of writing and re-reading configs.
        CentralConfigDescriptor cc = JaxbHelper.readConfig(Files.readFileToString(outputConfig));

        assertEquals(cc.getServerName(), "mymy");
        assertTrue(cc.getSecurity().isAnonAccessEnabled());

        assertEquals(cc.getLocalRepositoriesMap().size(), 2,
                "Expecting 2 local repositories");

        List<BackupDescriptor> backups = cc.getBackups();
        assertEquals(backups.size(), 1, "Expecting 1 backup");
        assertEquals(backups.get(0).getKey(), "backup1");
        assertFalse(backups.get(0).isEnabled());

        Map<String, LocalRepoDescriptor> localRepos = cc.getLocalRepositoriesMap();
        assertTrue(localRepos.get("local1").isSuppressPomConsistencyChecks());
        assertTrue(cc.getLocalRepositoriesMap().get("local2").isSuppressPomConsistencyChecks());

        assertEquals(cc.getProxies().size(), 1);

        List<PropertySet> propertySets = cc.getPropertySets();
        assertEquals(propertySets.size(), 2, "The config should contain 2 property sets.");

        PropertySet propertySet1 = propertySets.get(0);
        assertEquals(propertySet1.getName(), "propertySet1", "The first property set in the list should be named " +
                "'propertySet1'.");
        assertTrue(propertySet1.isVisible(), "The first property set in the list should be visible.");

        PropertySet propertySet2 = propertySets.get(1);
        assertEquals(propertySet2.getName(), "propertySet2", "The second property set in the list should be named " +
                "'propertySet2'.");
        assertFalse(propertySet2.isVisible(), "The second property set in the list should not be visible.");

        List<LocalReplicationDescriptor> localReplications = cc.getLocalReplications();
        assertEquals(localReplications.size(), 1, "Expected only 1 replication.");
        assertEquals(localReplications.get(0).getRepoKey(), "local1", "Unexpected replication repo key association.");
        assertTrue(localReplications.get(0).isEnabled(), "Expected the replication to be enabled.");

        GcConfigDescriptor gcConfig = cc.getGcConfig();
        assertNotNull(gcConfig, "Expected to find the GC configuration descriptor.");
        assertEquals(gcConfig.getCronExp(), "0 0 4 * * ?", "Unexpected GC cron");

        log.debug("config = " + cc);
    }

    public void defaultConfigElements() throws FileNotFoundException {
        CentralConfigDescriptorImpl cc = new CentralConfigDescriptorImpl();

        RepoLayout repoLayout = new RepoLayout();
        repoLayout.setName("repoLayout");
        repoLayout.setArtifactPathPattern("artifactPathPattern");
        repoLayout.setDistinctiveDescriptorPathPattern(true);
        repoLayout.setDescriptorPathPattern("descriptorPathPattern");
        cc.addRepoLayout(repoLayout);

        // at least one local repository
        LocalRepoDescriptor localRepo = new LocalRepoDescriptor();
        localRepo.setKey("abc");
        localRepo.setRepoLayout(repoLayout);
        cc.addLocalRepository(localRepo);

        GcConfigDescriptor gcConfigDescriptor = new GcConfigDescriptor();
        gcConfigDescriptor.setCronExp("tyrtrsg");
        cc.setGcConfig(gcConfigDescriptor);

        CleanupConfigDescriptor cleanupConfigDescriptor = new CleanupConfigDescriptor();
        cleanupConfigDescriptor.setCronExp("sdfsdf");
        cc.setCleanupConfig(cleanupConfigDescriptor);

        CleanupConfigDescriptor virtualCleanupConfigDescriptor = new CleanupConfigDescriptor();
        virtualCleanupConfigDescriptor.setCronExp("sdfsdf");
        cc.setVirtualCacheCleanupConfig(virtualCleanupConfigDescriptor);

        FolderDownloadConfigDescriptor folderDownloadConfigDescriptor = new FolderDownloadConfigDescriptor();
        cc.setFolderDownloadConfig(folderDownloadConfigDescriptor);

        File outputConfig = new File(testDir, "config.defaults.test.xml");
        JaxbHelper.writeConfig(cc, outputConfig);

        CentralConfigDescriptor descriptor =
                new CentralConfigReader().read(outputConfig);
        assertNotNull(descriptor.getSecurity(), "Security setting should not be null");
        assertTrue(descriptor.getSecurity().isAnonAccessEnabled(),
                "Annonymous access should be enabled by default");
    }

    public void writeW3cSchema() throws FileNotFoundException {
        JaxbHelper<CentralConfigDescriptorImpl> helper =
                new JaxbHelper<CentralConfigDescriptorImpl>();
        helper.generateSchema(new PrintStream(new File(testDir, "schema.test.xsd")),
                CentralConfigDescriptorImpl.class, Descriptor.NS);
    }
}
