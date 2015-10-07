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

package org.artifactory.info;

import com.google.common.collect.Maps;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 * An information group for all the host properties
 *
 * @author Noam Tenne
 */
public class HostPropInfo extends BasePropInfoGroup {

    /**
     * Property map
     */
    private TreeMap<String, String> propertyMap;
    /**
     * Operating system bean
     */
    private OperatingSystemMXBean systemBean;
    /**
     * Memory bean
     */
    private MemoryMXBean memoryBean;

    /**
     * Main constructor
     */
    public HostPropInfo() {
        //Get operating system bean
        systemBean = ManagementFactory.getOperatingSystemMXBean();
        //Get memory bean
        memoryBean = ManagementFactory.getMemoryMXBean();
        setPropertyMap();
    }

    /**
     * Sets the map with all the host property names and values
     */
    private void setPropertyMap() {
        propertyMap = Maps.newTreeMap();
        propertyMap.put("os.arch", systemBean.getArch());
        propertyMap.put("os.name", systemBean.getName());
        propertyMap.put("os.version", systemBean.getVersion());
        propertyMap
                .put("Available Processors", Integer.toString(systemBean.getAvailableProcessors()));
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        propertyMap
                .put("Heap Memory Usage-Commited", Long.toString(heapMemoryUsage.getCommitted()));
        propertyMap.put("Heap Memory Usage-Init", Long.toString(heapMemoryUsage.getInit()));
        propertyMap.put("Heap Memory Usage-Max", Long.toString(heapMemoryUsage.getMax()));
        propertyMap.put("Heap Memory Usage-Used", Long.toString(heapMemoryUsage.getUsed()));
        MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();
        propertyMap.put("Non-Heap Memory Usage-Commited",
                Long.toString(nonHeapMemoryUsage.getCommitted()));
        propertyMap.put("Non-Heap Memory Usage-Init", Long.toString(nonHeapMemoryUsage.getInit()));
        propertyMap.put("Non-Heap Memory Usage-Max", Long.toString(nonHeapMemoryUsage.getMax()));
        propertyMap.put("Non-Heap Memory Usage-Used", Long.toString(nonHeapMemoryUsage.getUsed()));
    }

    /**
     * Returns all the info objects from the current group
     *
     * @return InfoObject[] - Collection of info objects from current group
     */
    @Override
    public InfoObject[] getInfo() {
        ArrayList<InfoObject> infoList = new ArrayList<>();

        Set<String> keys = propertyMap.keySet();
        for (String key : keys) {
            infoList.add(new InfoObject(key, propertyMap.get(key)));
        }
        return infoList.toArray(new InfoObject[infoList.size()]);
    }
}