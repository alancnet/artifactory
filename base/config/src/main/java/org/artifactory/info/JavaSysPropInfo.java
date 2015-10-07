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

import com.google.common.collect.Lists;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * An information group for all the java system properties
 *
 * @author Noam Tenne
 */
public class JavaSysPropInfo extends BasePropInfoGroup {

    private SystemPropInfoGroup systemPropInfoGroup;

    private RuntimeMXBean runtimeMXBean;

    public JavaSysPropInfo() {
        systemPropInfoGroup = new SystemPropInfoGroup("java.class.version",
                "java.home",
                "java.io.tmpdir",
                "java.runtime.name",
                "java.runtime.version",
                "java.specification.name",
                "java.specification.vendor",
                "java.specification.version",
                "java.vendor",
                "java.vendor.url",
                "java.vendor.url.bug",
                "java.version",
                "java.vm.info",
                "java.vm.name",
                "java.vm.specification.name",
                "java.vm.specification.vendor",
                "java.vm.specification.version",
                "java.vm.vendor",
                "java.vm.version",
                "sun.arch.data.model",
                "sun.boot.library.path",
                "sun.cpu.endian",
                "sun.cpu.isalist",
                "sun.io.unicode.encoding",
                "sun.java.launcher",
                "sun.jnu.encoding",
                "sun.management.compiler",
                "sun.os.patch.level");
        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    }

    @Override
    public InfoObject[] getInfo() {
        List<InfoObject> infoObjects = Lists.newArrayList(systemPropInfoGroup.getInfo());

        List<String> inputArguments = runtimeMXBean.getInputArguments();

        boolean addedFirstValue = false;
        String key = "JVM Input arguments";
        for (String inputArgument : inputArguments) {
            infoObjects.add(new InfoObject(key, inputArgument));
            if (!addedFirstValue) {
                addedFirstValue = true;
                key = "";
            }
        }

        return infoObjects.toArray(new InfoObject[infoObjects.size()]);
    }
}