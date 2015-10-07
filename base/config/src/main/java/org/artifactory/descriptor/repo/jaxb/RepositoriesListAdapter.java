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

import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * The sole purpose of this adapter is to make jaxb call the setter: org.artifactory.repo.virtual.VirtualRepo#setRepositories(java.util.Set<org.artifactory.repo.Repo>)
 * (see: com.sun.xml.bind.v2.runtime.reflect.Lister.CollectionLister#endPacking(T, BeanT,
 * com.sun.xml.bind.v2.runtime.reflect.Accessor<BeanT,T>))
 *
 * @author yoavl
 */
public class RepositoriesListAdapter
        extends XmlAdapter<RepositoriesListAdapter.Wrappper, List<RepoDescriptor>> {

    @Override
    public List<RepoDescriptor> unmarshal(Wrappper wrappper) throws Exception {
        return wrappper.getList();
    }

    @Override
    public Wrappper marshal(List<RepoDescriptor> list) throws Exception {
        return new RepositoriesListAdapter.Wrappper(list);
    }

    @XmlType(name = "RepositoryRefsType", namespace = Descriptor.NS)
    public static class Wrappper {
        //TODO: There seems to be a bug of referencing an ID from within an ID - this does not work
        //(list always empty)
        @XmlIDREF
        @XmlElement(name = "repositoryRef", type = RepoBaseDescriptor.class,
                namespace = Descriptor.NS)
        private List<RepoDescriptor> list = new ArrayList<>();

        public Wrappper() {
        }

        public Wrappper(List<RepoDescriptor> list) {
            this.list = list;
        }

        public List<RepoDescriptor> getList() {
            return list;
        }
    }
}