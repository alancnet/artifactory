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

import com.google.common.collect.Sets;
import org.apache.commons.lang.SystemUtils;
import org.artifactory.common.ConstantValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


/**
 * An enum of info groups that writes all the information to the log
 *
 * @author Noam Tenne
 */
public enum InfoWriter {
    user(UserPropInfo.class, "User Info"),
    host(HostPropInfo.class, "Host Info"),
    ha(HaPropInfo.class, "High Availability Node Info"),
    artifactory(ArtifactoryPropInfo.class, "Artifactory Info"),
    javaSys(JavaSysPropInfo.class, "Java System Info"),
    classPath(ClassPathPropInfo.class, "Java Class Path Info");

    private static final Logger log = LoggerFactory.getLogger(InfoWriter.class);

    /**
     * A list of property keys for which the value should be masked
     */
    private static final Set<String> maskedKeys = Sets.newHashSet(
            ConstantValues.s3backupAccountId.getPropertyName(),
            ConstantValues.s3backupAccountSecretKey.getPropertyName()
    );

    /**
     * Info group class
     */
    private final Class<? extends BasePropInfoGroup> infoGroup;
    /**
     * Group name (used for title)
     */
    private final String groupName;
    /**
     * The format of the list to be printed
     */
    private static String listFormat = "   %1$-70s| %2$s%n";

    /**
     * Main constructor
     *
     * @param infoGroup InfoGroupClass
     * @param groupName Name of info group
     */
    InfoWriter(Class<? extends BasePropInfoGroup> infoGroup, String groupName) {
        this.infoGroup = infoGroup;
        this.groupName = groupName;
    }

    /**
     * Dumps the info from all the groups in the enum to the log
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static void writeInfo() throws IllegalAccessException, InstantiationException {
        if (log.isInfoEnabled()) {
            String wholeDump = getInfoString();
            log.info(wholeDump);
        }
    }

    public static String getInfoString() throws InstantiationException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n%n SYSTEM INFORMATION DUMP%n"));
        sb.append(String.format(" =======================%n"));
        for (InfoWriter writer : InfoWriter.values()) {
            BasePropInfoGroup group = writer.infoGroup.newInstance();
            if (!group.isInUse()) {
                continue;
            }
            //Create group title
            sb.append(String.format("%n ")).append(writer.groupName).append(String.format("%n"));
            sb.append(String.format(" ========================%n"));
            //Iterate over all info objects
            for (InfoObject infoObject : group.getInfo()) {
                String propertyName = infoObject.getPropertyName();
                String value = infoObject.getPropertyValue();
                if (shouldMaskValue(propertyName)) {
                    value = org.artifactory.util.Strings.mask(value);
                } else if (writer.equals(javaSys) && shouldMaskValue(value)) {
                    value = org.artifactory.util.Strings.maskKeyValue(value);
                }
                if (propertyName.matches(".*class\\.?path.*")) {
                    splitLine(sb, propertyName, value);
                } else {
                    sb.append(String.format(listFormat,  propertyName , value));
                }
            }
        }

        //Dump the info to the log
        return sb.toString();
    }

    private static void splitLine(StringBuilder sb, String propertyName, String value) {
        String multiValueSeparator = SystemUtils.IS_OS_WINDOWS ? ";" : ":";
        String[] separateValues = value.split(multiValueSeparator);
        for (int i = 0; i < separateValues.length; i++) {
            String separateValue = separateValues[i];
            sb.append(String.format(listFormat, (i == 0) ? propertyName : "", separateValue));
        }
    }

    public static boolean shouldMaskValue(String propertyKey) {
        String propKeyLower = propertyKey.toLowerCase();
        return propKeyLower.contains("password")
                || propKeyLower.contains("secret")
                || propKeyLower.contains("key")
                || maskedKeys.contains(propertyKey);
    }
}
