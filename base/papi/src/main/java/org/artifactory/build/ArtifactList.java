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

package org.artifactory.build;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Noam Y. Tenne
 */
public class ArtifactList implements List<Artifact> {

    private List<org.jfrog.build.api.Artifact> artifactList;

    ArtifactList(List<org.jfrog.build.api.Artifact> artifactList) {
        this.artifactList = artifactList;
    }

    @Override
    public int size() {
        return artifactList.size();
    }

    @Override
    public boolean isEmpty() {
        return artifactList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return artifactList.contains(((Artifact) o).getBuildArtifact());
    }

    @Override
    public Iterator<Artifact> iterator() {
        final Iterator<org.jfrog.build.api.Artifact> iterator = artifactList.iterator();
        return new Iterator<Artifact>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Artifact next() {
                return new Artifact(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[artifactList.size()];
        for (int i = 0; i < artifactList.size(); i++) {
            array[i] = new Artifact(artifactList.get(i));
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Object[] array = toArray();
        for (int i = 0; i < array.length; i++) {
            a[i] = ((T) array[i]);
        }
        return a;
    }

    @Override
    public boolean add(Artifact artifact) {
        return artifactList.add(artifact.getBuildArtifact());
    }

    @Override
    public boolean remove(Object o) {
        return artifactList.remove(((Artifact) o).getBuildArtifact());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return artifactList.containsAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((Artifact) input).getBuildArtifact();
            }
        })));
    }

    @Override
    public boolean addAll(Collection<? extends Artifact> c) {
        return artifactList.addAll(Lists.newArrayList(Iterables.transform(c,
                new Function<Object, org.jfrog.build.api.Artifact>() {
                    @Override
                    public org.jfrog.build.api.Artifact apply(Object input) {
                        return ((Artifact) input).getBuildArtifact();
                    }
                })));
    }

    @Override
    public boolean addAll(int index, Collection<? extends Artifact> c) {
        return artifactList.addAll(index, Lists.newArrayList(Iterables.transform(c,
                new Function<Object, org.jfrog.build.api.Artifact>() {
                    @Override
                    public org.jfrog.build.api.Artifact apply(Object input) {
                        return ((Artifact) input).getBuildArtifact();
                    }
                })));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return artifactList.removeAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((Artifact) input).getBuildArtifact();
            }
        })));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return artifactList.retainAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((Artifact) input).getBuildArtifact();
            }
        })));
    }

    @Override
    public void clear() {
        artifactList.clear();
    }

    @Override
    public Artifact get(int index) {
        return new Artifact(artifactList.get(index));
    }

    @Override
    public Artifact set(int index, Artifact element) {
        org.jfrog.build.api.Artifact artifact = artifactList.set(index, element.getBuildArtifact());
        if (artifact == null) {
            return null;
        }
        return new Artifact(artifact);
    }

    @Override
    public void add(int index, Artifact element) {
        artifactList.add(index, element.getBuildArtifact());
    }

    @Override
    public Artifact remove(int index) {
        org.jfrog.build.api.Artifact removed = artifactList.remove(index);
        if (removed == null) {
            return null;
        }
        return new Artifact(removed);
    }

    @Override
    public int indexOf(Object o) {
        return artifactList.indexOf(((Artifact) o).getBuildArtifact());
    }

    @Override
    public int lastIndexOf(Object o) {
        return artifactList.lastIndexOf(((Artifact) o).getBuildArtifact());
    }

    @Override
    public ListIterator<Artifact> listIterator() {
        return new ArtifactListIterator(artifactList.listIterator());
    }

    @Override
    public ListIterator<Artifact> listIterator(int index) {
        return new ArtifactListIterator(artifactList.listIterator(index));
    }

    @Override
    public List<Artifact> subList(int fromIndex, int toIndex) {
        return Lists.newArrayList(Iterables.transform(artifactList.subList(fromIndex, toIndex),
                new Function<org.jfrog.build.api.Artifact, Artifact>() {
                    @Override
                    public Artifact apply(org.jfrog.build.api.Artifact input) {
                        return new Artifact(input);
                    }
                }));
    }

    private class ArtifactListIterator implements ListIterator<Artifact> {

        private ListIterator<org.jfrog.build.api.Artifact> artifactListIterator;

        public ArtifactListIterator(ListIterator<org.jfrog.build.api.Artifact> artifactListIterator) {
            this.artifactListIterator = artifactListIterator;
        }

        @Override
        public boolean hasNext() {
            return artifactListIterator.hasNext();
        }

        @Override
        public Artifact next() {
            return new Artifact(artifactListIterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return artifactListIterator.hasPrevious();
        }

        @Override
        public Artifact previous() {
            return new Artifact(artifactListIterator.previous());
        }

        @Override
        public int nextIndex() {
            return artifactListIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return artifactListIterator.previousIndex();
        }

        @Override
        public void remove() {
            artifactListIterator.remove();
        }

        @Override
        public void set(Artifact artifact) {
            artifactListIterator.set(artifact.getBuildArtifact());
        }

        @Override
        public void add(Artifact artifact) {
            artifactListIterator.add(artifact.getBuildArtifact());
        }
    }
}
