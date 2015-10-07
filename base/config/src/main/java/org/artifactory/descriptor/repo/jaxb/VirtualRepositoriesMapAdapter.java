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

package org.artifactory.descriptor.repo.jaxb;

import com.google.common.collect.Maps;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yoav Landman
 */
public class VirtualRepositoriesMapAdapter
        extends XmlAdapter<VirtualRepositoriesMapAdapter.Wrappper, Map<String, VirtualRepoDescriptor>> {

    @Override
    public Map<String, VirtualRepoDescriptor> unmarshal(Wrappper wrapper)
            throws Exception {
        Map<String, VirtualRepoDescriptor> virtualRepositoriesMap = Maps.newLinkedHashMap();
        for (VirtualRepoDescriptor repository : wrapper.getList()) {
            String key = repository.getKey();
            VirtualRepoDescriptor repo = virtualRepositoriesMap.put(key, repository);
            //Test for repositories with the same key
            if (repo != null) {
                //Throw an error since jaxb swallows exceptions
                throw new Error(
                        "Duplicate virtual repository key in configuration: " + key + ".");
            }
        }
        return virtualRepositoriesMap;
    }

    @Override
    public VirtualRepositoriesMapAdapter.Wrappper marshal(Map<String, VirtualRepoDescriptor> map) throws Exception {
        return new VirtualRepositoriesMapAdapter.Wrappper(map);
    }


    @XmlType(name = "VirtualRepositoriesType", namespace = Descriptor.NS)
    public static class Wrappper {
        @XmlElement(name = "virtualRepository", required = true, namespace = Descriptor.NS)
        private List<VirtualRepoDescriptor> list = new ArrayList<>();


        public Wrappper() {
        }

        public Wrappper(Map<String, VirtualRepoDescriptor> map) {
            for (VirtualRepoDescriptor repo : map.values()) {
                list.add(repo);
            }
        }

        public List<VirtualRepoDescriptor> getList() {
            return list;
        }
    }
}