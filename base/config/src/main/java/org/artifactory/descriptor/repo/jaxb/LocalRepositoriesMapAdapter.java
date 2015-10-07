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
import org.artifactory.descriptor.repo.LocalRepoDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Yoav Landman
 */
public class LocalRepositoriesMapAdapter extends
        XmlAdapter<LocalRepositoriesMapAdapter.Wrappper, Map<String, LocalRepoDescriptor>> {

    @Override
    public Map<String, LocalRepoDescriptor> unmarshal(Wrappper wrapper) throws Exception {
        Map<String, LocalRepoDescriptor> localRepositoriesMap = Maps.newLinkedHashMap();
        for (LocalRepoDescriptor repository : wrapper.getList()) {
            String key = repository.getKey();
            LocalRepoDescriptor repo = localRepositoriesMap.put(key, repository);
            //Test for repositories with the same key
            if (repo != null) {
                //Throw an error since jaxb swallows exceptions
                throw new Error(
                        "Duplicate repository key in configuration: " + key + ".");
            }
        }
        return localRepositoriesMap;
    }

    @Override
    public Wrappper marshal(Map<String, LocalRepoDescriptor> map) throws Exception {
        return new Wrappper(map);
    }

    @XmlType(name = "LocalRepositoriesType", namespace = Descriptor.NS)
    public static class Wrappper {
        @XmlElement(name = "localRepository", required = true, namespace = Descriptor.NS)
        private List<LocalRepoDescriptor> list = new ArrayList<>();

        public Wrappper() {
        }

        public Wrappper(Map<String, LocalRepoDescriptor> map) {
            for (LocalRepoDescriptor repo : map.values()) {
                list.add(repo);
            }
        }

        public List<LocalRepoDescriptor> getList() {
            return list;
        }
    }
}
