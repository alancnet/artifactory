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

package org.artifactory.traffic;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.traffic.entry.DownloadEntry;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;

/**
 * @author Noam Tenne
 */
public class TrafficTest {

    @BeforeClass
    public void setUp() throws JoranException {
        URL logConfigResource = getClass().getResource("/org/artifactory/traffic/logback.xml");
        File logConfigFile = new File(logConfigResource.getFile());
        Assert.assertNotNull(logConfigFile, "Cannot locate logback configuration file.");
        Assert.assertTrue(logConfigFile.exists(), "Cannot locate logback configuration file.");

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.stop();
        configurator.doConfigure(logConfigFile);
        Logger logger = lc.getLogger(TrafficLogger.class);
        Appender<ILoggingEvent> appender = logger.getAppender("TRAFFIC");

        System.out.println(appender);
    }

    @Test
    public void testTrafficLog() {
        TrafficLogger.logTransferEntry(new DownloadEntry(
                InfoFactoryHolder.get().createRepoPath("moo", "moo").getId(), 1L, 0L, "127.0.0.1"));
    }
}