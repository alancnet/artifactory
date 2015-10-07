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

package org.artifactory.webapp.wicket.page.config.advanced;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.ha.HaNodeProperties;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.label.highlighter.SyntaxHighlighter;
import org.artifactory.info.InfoWriter;
import org.artifactory.storage.StorageProperties;
import org.artifactory.util.Strings;
import org.artifactory.webapp.wicket.page.base.AuthenticatedPage;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import static org.artifactory.storage.StorageProperties.*;

/**
 * Displays a list of all the system properties and their values as well as memory information and JVM arguments
 *
 * @author Noam Y. Tenne
 */
@AuthorizeInstantiation(AuthorizationService.ROLE_ADMIN)
public class SystemInfoPage extends AuthenticatedPage {

    private static final String listFormat = "%1$-50s| %2$s%n";

    /**
     * Default Constructor
     */
    public SystemInfoPage() {
        TitledBorder border = new TitledBorder("border");
        add(border);

        SyntaxHighlighter infoPanel = new SyntaxHighlighter("sysInfo", collectSystemInfo(), Syntax.plain);
        infoPanel.setEscapeModelStrings(true);
        border.add(infoPanel);
    }

    /**
     * Return a formatted string of the system info to display
     *
     * @return
     */
    private String collectSystemInfo() {
        StringBuilder infoBuilder = new StringBuilder();

        StorageProperties storageProperties = ContextHelper.get().beanForType(StorageProperties.class);
        infoBuilder.append("Storage Info:").append("\n");
        addInfo(infoBuilder, "Database Type", storageProperties.getDbType().toString());
        BinaryProviderType binariesStorageType = storageProperties.getBinariesStorageType();
        addInfo(infoBuilder, "Storage Type", binariesStorageType.toString());
        if (BinaryProviderType.S3 == binariesStorageType) {
            // S3 properties
            addInfo(infoBuilder, "s3.bucket.name", storageProperties.getS3BucketName());
            addInfo(infoBuilder, "s3.bucket.path", storageProperties.getS3BucketPath());
            addInfo(infoBuilder, "s3.endpoint", storageProperties.getS3Entpoint());
            // Retry properties
            addInfo(infoBuilder, "retry.max.retries.number", Integer.toString(
                    storageProperties.getMaxRetriesNumber()));
            addInfo(infoBuilder, "retry.delay.between.retries", Integer.toString(
                    storageProperties.getDelayBetweenRetries()));
            // Eventually persisted properties
            addInfo(infoBuilder, "eventually.persisted.max.number.of.threads", Integer.toString(
                    storageProperties.getEventuallyPersistedMaxNumberOfThread()));
            addInfo(infoBuilder, "eventually.persisted.timeout", Integer.toString(
                    storageProperties.getEventuallyPersistedTimeOut()));
            addInfo(infoBuilder, "eventually.dispatcher.sleep.time", Long.toString(
                    storageProperties.getEventuallyPersistedDispatcherSleepTime()));
        }

        infoBuilder.append("\n").append("System Properties:").append("\n");
        Properties properties = System.getProperties();
        //// add Artifactory version to the properties, will be alphabetically sorted later.
        properties.setProperty(ConstantValues.artifactoryVersion.getPropertyName(),
                ConstantValues.artifactoryVersion.getString());
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        addInfo(infoBuilder, "artifactory.running.mode", addonsManager.getArtifactoryRunningMode().name());
        addInfo(infoBuilder, "artifactory.running.state", ContextHelper.get().isOffline() ? "Offline" : "Online");
        addFromProperties(infoBuilder, properties);
        addHaProperties(infoBuilder);
        infoBuilder.append("\n").append("General JVM Info:").append("\n");
        OperatingSystemMXBean systemBean = ManagementFactory.getOperatingSystemMXBean();
        addInfo(infoBuilder, "Available Processors", Integer.toString(systemBean.getAvailableProcessors()));

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();

        addInfo(infoBuilder, "Heap Memory Usage-Committed", Long.toString(heapMemoryUsage.getCommitted()));
        addInfo(infoBuilder, "Heap Memory Usage-Init", Long.toString(heapMemoryUsage.getInit()));
        addInfo(infoBuilder, "Heap Memory Usage-Max", Long.toString(heapMemoryUsage.getMax()));
        addInfo(infoBuilder, "Heap Memory Usage-Used", Long.toString(heapMemoryUsage.getUsed()));

        MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();
        addInfo(infoBuilder, "Non-Heap Memory Usage-Committed", Long.toString(nonHeapMemoryUsage.getCommitted()));
        addInfo(infoBuilder, "Non-Heap Memory Usage-Init", Long.toString(nonHeapMemoryUsage.getInit()));
        addInfo(infoBuilder, "Non-Heap Memory Usage-Max", Long.toString(nonHeapMemoryUsage.getMax()));
        addInfo(infoBuilder, "Non-Heap Memory Usage-Used", Long.toString(nonHeapMemoryUsage.getUsed()));

        RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
        StringBuilder vmArgumentBuilder = new StringBuilder();
        List<String> vmArguments = RuntimemxBean.getInputArguments();
        if (vmArguments != null) {
            for (String vmArgument : vmArguments) {
                if (InfoWriter.shouldMaskValue(vmArgument)) {
                    vmArgument = Strings.maskKeyValue(vmArgument);
                }
                vmArgumentBuilder.append(vmArgument);
                if (vmArguments.indexOf(vmArgument) != (vmArguments.size() - 1)) {
                    vmArgumentBuilder.append("\n");
                }
            }
        }

        infoBuilder.append("\nJVM Arguments:\n").append(vmArgumentBuilder.toString());

        return StringUtils.removeEnd(infoBuilder.toString(), "\n");
    }

    private void addFromProperties(StringBuilder infoBuilder, Properties properties) {
        TreeSet sortedSystemPropKeys = new TreeSet<>(properties.keySet());
        for (Object key : sortedSystemPropKeys) {
            addInfo(infoBuilder, String.valueOf(key), String.valueOf(properties.get(key)));
        }
    }

    private void addHaProperties(StringBuilder infoBuilder) {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        if (artifactoryHome.isHaConfigured()) {
            infoBuilder.append("\n").append("HA Node Properties:").append("\n");
            HaNodeProperties haNodeProperties = artifactoryHome.getHaNodeProperties();
            if (haNodeProperties != null) {
                addFromProperties(infoBuilder, haNodeProperties.getProperties());
            }
        }
    }

    /**
     * Append a property key and value to the info builder
     *
     * @param infoBuilder   Target builder
     * @param propertyKey   Key of property to display
     * @param propertyValue Value of property to display
     */
    private void addInfo(StringBuilder infoBuilder, String propertyKey, String propertyValue) {
        if (InfoWriter.shouldMaskValue(propertyKey)) {
            propertyValue = Strings.mask(propertyValue);
        } else {
            propertyValue = StringEscapeUtils.escapeJava(propertyValue);
        }
        infoBuilder.append(String.format(listFormat, propertyKey, propertyValue));
    }

    @Override
    public String getPageName() {
        return "System Info";
    }
}
