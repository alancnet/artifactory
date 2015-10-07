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

package org.artifactory.api.config;

import org.artifactory.descriptor.DescriptorAware;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;

/**
 * User: freds Date: Aug 3, 2008 Time: 6:22:39 PM
 */
public interface CentralConfigService extends DescriptorAware<CentralConfigDescriptor>, ImportableExportable {
    DateTimeFormatter getDateFormatter();

    void saveEditedDescriptorAndReload(CentralConfigDescriptor descriptor);

    /**
     * Reload configuration from storage
     *
     * @return {@code true} if succeeded reloading
     */
    boolean reloadConfiguration(boolean saveConfiguration);

    String getServerName();

    /**
     * Formats the given milliseconds date with the date format configured in the central config.
     *
     * @param date Date in milliseconds
     * @return Formatted date string
     */
    String format(long date);

    VersionInfo getVersionInfo();

    /**
     * @return A mutable COPY of the central config descriptor.
     */
    MutableCentralConfigDescriptor getMutableDescriptor();

    String getConfigXml();

    void setConfigXml(String xmlConfig,boolean saveConfiguration);

    void setLogo(File logo) throws IOException;

    boolean defaultProxyDefined();
}