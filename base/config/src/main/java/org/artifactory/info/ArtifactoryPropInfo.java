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

import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 * An information group for all the artifactory properties
 *
 * @author Noam Tenne
 */
public class ArtifactoryPropInfo extends BasePropInfoGroup {

    /**
     * Returns all the info objects from the current group
     *
     * @return InfoObject[] - Collection of info objects from current group
     */
    @Override
    public InfoObject[] getInfo() {
        //Make a copy of the artifactory properties
        Properties propertiesCopy = ArtifactoryHome.get().getArtifactoryProperties().getPropertiesCopy();
        ArrayList<InfoObject> infoList = new ArrayList<>();
        ConstantValues[] constants = ConstantValues.values();

        //Returns all the properties form ConstantsValue
        for (ConstantValues constantsValue : constants) {
            String value = constantsValue.getString();
            if (value != null) {
                InfoObject infoObject =
                        new InfoObject(constantsValue.getPropertyName(), value);
                infoList.add(infoObject);
                //Remove duplicates from artifactoryProperties copy
                propertiesCopy.remove(constantsValue.getPropertyName());
            }
        }

        //Iterate over artifactoryProperties copy to get the rest of the properties that were not in ConstantsValue
        Enumeration keys = propertiesCopy.keys();
        while (keys.hasMoreElements()) {
            String propertyName = (String) keys.nextElement();
            InfoObject infoObject =
                    new InfoObject(propertyName, propertiesCopy.getProperty(propertyName));
            infoList.add(infoObject);
        }
        return infoList.toArray(new InfoObject[infoList.size()]);
    }
}
