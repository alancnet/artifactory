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

package org.artifactory.common.wicket.event;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yoav Aharoni
 */
public class EventBus implements Serializable {
    private transient MultiValueMap<Class, Listener> listenerMap;

    public <T> void addListener(Class<T> eventClass, Listener<T> listener) {
        if (listenerMap == null) {
            listenerMap = new LinkedMultiValueMap<>();
        }
        listenerMap.add(eventClass, listener);
    }

    public <T> void removeListener(Class<T> eventClass, Listener<T> listener) {
        if (listenerMap != null) {
            List<Listener> listeners = listenerMap.get(eventClass);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public void fire(Object event) {
        if (listenerMap != null) {
            Class eventClass = event.getClass();
            List<Listener> listeners = listenerMap.get(eventClass);
            if (listeners != null) {
                for (Listener listener : listeners) {
                    listener.onEvent(event);
                }
            }
        }
    }
}
