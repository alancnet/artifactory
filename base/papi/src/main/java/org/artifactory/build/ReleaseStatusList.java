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
import org.jfrog.build.api.release.PromotionStatus;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Noam Y. Tenne
 */
public class ReleaseStatusList implements List<ReleaseStatus> {

    private List<PromotionStatus> promotionStatusList;

    ReleaseStatusList(List<PromotionStatus> promotionStatusList) {
        this.promotionStatusList = promotionStatusList;
    }

    @Override
    public int size() {
        return promotionStatusList.size();
    }

    @Override
    public boolean isEmpty() {
        return promotionStatusList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return promotionStatusList.contains(((ReleaseStatus) o).getPromotionStatus());
    }

    @Override
    public Iterator<ReleaseStatus> iterator() {
        final Iterator<PromotionStatus> iterator = promotionStatusList.iterator();
        return new Iterator<ReleaseStatus>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ReleaseStatus next() {
                return new ReleaseStatus(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[promotionStatusList.size()];
        for (int i = 0; i < promotionStatusList.size(); i++) {
            array[i] = new ReleaseStatus(promotionStatusList.get(i));
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
    public boolean add(ReleaseStatus releaseStatus) {
        return promotionStatusList.add(releaseStatus.getPromotionStatus());
    }

    @Override
    public boolean remove(Object o) {
        return promotionStatusList.remove(((ReleaseStatus) o).getPromotionStatus());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return promotionStatusList.containsAll(
                Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
                    @Override
                    public Object apply(Object input) {
                        return ((ReleaseStatus) input).getPromotionStatus();
                    }
                })));
    }

    @Override
    public boolean addAll(Collection<? extends ReleaseStatus> c) {
        return promotionStatusList.addAll(Lists.newArrayList(Iterables.transform(c,
                new Function<Object, PromotionStatus>() {
                    @Override
                    public PromotionStatus apply(Object input) {
                        return ((ReleaseStatus) input).getPromotionStatus();
                    }
                })));
    }

    @Override
    public boolean addAll(int index, Collection<? extends ReleaseStatus> c) {
        return promotionStatusList.addAll(index, Lists.newArrayList(Iterables.transform(c,
                new Function<Object, PromotionStatus>() {
                    @Override
                    public PromotionStatus apply(Object input) {
                        return ((ReleaseStatus) input).getPromotionStatus();
                    }
                })));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return promotionStatusList.removeAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((ReleaseStatus) input).getPromotionStatus();
            }
        })));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return promotionStatusList.retainAll(Lists.newArrayList(Iterables.transform(c, new Function<Object, Object>() {
            @Override
            public Object apply(Object input) {
                return ((ReleaseStatus) input).getPromotionStatus();
            }
        })));
    }

    @Override
    public void clear() {
        promotionStatusList.clear();
    }

    @Override
    public ReleaseStatus get(int index) {
        return new ReleaseStatus(promotionStatusList.get(index));
    }

    @Override
    public ReleaseStatus set(int index, ReleaseStatus element) {
        PromotionStatus promotionStatus = promotionStatusList.set(index, element.getPromotionStatus());
        if (promotionStatus == null) {
            return null;
        }
        return new ReleaseStatus(promotionStatus);
    }

    @Override
    public void add(int index, ReleaseStatus element) {
        promotionStatusList.add(index, element.getPromotionStatus());
    }

    @Override
    public ReleaseStatus remove(int index) {
        PromotionStatus removed = promotionStatusList.remove(index);
        if (removed == null) {
            return null;
        }
        return new ReleaseStatus(removed);
    }

    @Override
    public int indexOf(Object o) {
        return promotionStatusList.indexOf(((ReleaseStatus) o).getPromotionStatus());
    }

    @Override
    public int lastIndexOf(Object o) {
        return promotionStatusList.lastIndexOf(((ReleaseStatus) o).getPromotionStatus());
    }

    @Override
    public ListIterator<ReleaseStatus> listIterator() {
        return new ReleaseStatusListIterator(promotionStatusList.listIterator());
    }

    @Override
    public ListIterator<ReleaseStatus> listIterator(int index) {
        return new ReleaseStatusListIterator(promotionStatusList.listIterator(index));
    }

    @Override
    public List<ReleaseStatus> subList(int fromIndex, int toIndex) {
        return Lists.newArrayList(Iterables.transform(promotionStatusList.subList(fromIndex, toIndex),
                new Function<PromotionStatus, ReleaseStatus>() {
                    @Override
                    public ReleaseStatus apply(@Nullable PromotionStatus input) {
                        return new ReleaseStatus(input);
                    }
                }));
    }

    private class ReleaseStatusListIterator implements ListIterator<ReleaseStatus> {

        private ListIterator<PromotionStatus> promotionStatusListIterator;

        public ReleaseStatusListIterator(ListIterator<PromotionStatus> promotionStatusListIterator) {
            this.promotionStatusListIterator = promotionStatusListIterator;
        }

        @Override
        public boolean hasNext() {
            return promotionStatusListIterator.hasNext();
        }

        @Override
        public ReleaseStatus next() {
            return new ReleaseStatus(promotionStatusListIterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return promotionStatusListIterator.hasPrevious();
        }

        @Override
        public ReleaseStatus previous() {
            return new ReleaseStatus(promotionStatusListIterator.previous());
        }

        @Override
        public int nextIndex() {
            return promotionStatusListIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return promotionStatusListIterator.previousIndex();
        }

        @Override
        public void remove() {
            promotionStatusListIterator.remove();
        }

        @Override
        public void set(ReleaseStatus releaseStatus) {
            promotionStatusListIterator.set(releaseStatus.getPromotionStatus());
        }

        @Override
        public void add(ReleaseStatus releaseStatus) {
            promotionStatusListIterator.add(releaseStatus.getPromotionStatus());
        }
    }
}
