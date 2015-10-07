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
public class DependencyList implements List<Dependency> {

    private List<org.jfrog.build.api.Dependency> dependencyList;

    DependencyList(List<org.jfrog.build.api.Dependency> dependencyList) {
        this.dependencyList = dependencyList;
    }

    @Override
    public int size() {
        return dependencyList.size();
    }

    @Override
    public boolean isEmpty() {
        return dependencyList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return dependencyList.contains(((Dependency) o).getBuildDependency());
    }

    @Override
    public Iterator<Dependency> iterator() {
        final Iterator<org.jfrog.build.api.Dependency> iterator = dependencyList.iterator();
        return new Iterator<Dependency>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Dependency next() {
                return new Dependency(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[dependencyList.size()];
        for (int i = 0; i < dependencyList.size(); i++) {
            array[i] = new Dependency(dependencyList.get(i));
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
    public boolean add(Dependency dependency) {
        return dependencyList.add(dependency.getBuildDependency());
    }

    @Override
    public boolean remove(Object o) {
        return dependencyList.remove(((Dependency) o).getBuildDependency());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return dependencyList.containsAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((Dependency) input).getBuildDependency();
            }
        })));
    }

    @Override
    public boolean addAll(Collection<? extends Dependency> c) {
        return dependencyList.addAll(Lists.newArrayList(Iterables.transform(c,
                new Function<Object, org.jfrog.build.api.Dependency>() {
                    @Override
                    public org.jfrog.build.api.Dependency apply(Object input) {
                        return ((Dependency) input).getBuildDependency();
                    }
                })));
    }

    @Override
    public boolean addAll(int index, Collection<? extends Dependency> c) {
        return dependencyList.addAll(index, Lists.newArrayList(Iterables.transform(c,
                new Function<Object, org.jfrog.build.api.Dependency>() {
                    @Override
                    public org.jfrog.build.api.Dependency apply(Object input) {
                        return ((Dependency) input).getBuildDependency();
                    }
                })));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return dependencyList.removeAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((Dependency) input).getBuildDependency();
            }
        })));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return dependencyList.retainAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((Dependency) input).getBuildDependency();
            }
        })));
    }

    @Override
    public void clear() {
        dependencyList.clear();
    }

    @Override
    public Dependency get(int index) {
        return new Dependency(dependencyList.get(index));
    }

    @Override
    public Dependency set(int index, Dependency element) {
        org.jfrog.build.api.Dependency dependency = dependencyList.set(index, element.getBuildDependency());
        if (dependency == null) {
            return null;
        }
        return new Dependency(dependency);
    }

    @Override
    public void add(int index, Dependency element) {
        dependencyList.add(index, element.getBuildDependency());
    }

    @Override
    public Dependency remove(int index) {
        org.jfrog.build.api.Dependency removed = dependencyList.remove(index);
        if (removed == null) {
            return null;
        }
        return new Dependency(removed);
    }

    @Override
    public int indexOf(Object o) {
        return dependencyList.indexOf(((Dependency) o).getBuildDependency());
    }

    @Override
    public int lastIndexOf(Object o) {
        return dependencyList.lastIndexOf(((Dependency) o).getBuildDependency());
    }

    @Override
    public ListIterator<Dependency> listIterator() {
        return new DependencyListIterator(dependencyList.listIterator());
    }

    @Override
    public ListIterator<Dependency> listIterator(int index) {
        return new DependencyListIterator(dependencyList.listIterator(index));
    }

    @Override
    public List<Dependency> subList(int fromIndex, int toIndex) {
        return Lists.newArrayList(Iterables.transform(dependencyList.subList(fromIndex, toIndex),
                new Function<org.jfrog.build.api.Dependency, Dependency>() {
                    @Override
                    public Dependency apply(org.jfrog.build.api.Dependency input) {
                        return new Dependency(input);
                    }
                }));
    }

    private class DependencyListIterator implements ListIterator<Dependency> {

        private ListIterator<org.jfrog.build.api.Dependency> dependencyListIterator;

        public DependencyListIterator(ListIterator<org.jfrog.build.api.Dependency> dependencyListIterator) {
            this.dependencyListIterator = dependencyListIterator;
        }

        @Override
        public boolean hasNext() {
            return dependencyListIterator.hasNext();
        }

        @Override
        public Dependency next() {
            return new Dependency(dependencyListIterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return dependencyListIterator.hasPrevious();
        }

        @Override
        public Dependency previous() {
            return new Dependency(dependencyListIterator.previous());
        }

        @Override
        public int nextIndex() {
            return dependencyListIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return dependencyListIterator.previousIndex();
        }

        @Override
        public void remove() {
            dependencyListIterator.remove();
        }

        @Override
        public void set(Dependency dependency) {
            dependencyListIterator.set(dependency.getBuildDependency());
        }

        @Override
        public void add(Dependency dependency) {
            dependencyListIterator.add(dependency.getBuildDependency());
        }
    }
}
