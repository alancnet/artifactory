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

package org.artifactory.common.wicket.util;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Yoav Landman
 */
public class ComponentPersister {

    private static final Logger log = LoggerFactory.getLogger(ComponentPersister.class);

    public static final org.apache.wicket.util.cookies.CookieUtils COOKIE_UTILS = new org.apache.wicket.util.cookies.CookieUtils();

    private static MetaDataKey<Boolean> PERSIST_KEY = new MetaDataKey<Boolean>() {
    };

    public static void loadChildren(MarkupContainer container) {
        container.visitChildren(FormComponent.class, new IVisitor<FormComponent, Void>() {
            @Override
            public void component(FormComponent formComponent, IVisit iVisit) {
                if (isPersistent(formComponent)) {
                    load(formComponent);
                }
            }
        });
    }

    public static void saveChildren(MarkupContainer container) {
        container.visitChildren(FormComponent.class, new IVisitor<FormComponent, Void>() {
            @Override
            public void component(FormComponent formComponent, IVisit iVisit) {
                if (isPersistent(formComponent)) {
                    save(formComponent);
                }
            }
        });
    }

    public static void setPersistent(FormComponent component) {
        component.setMetaData(PERSIST_KEY, true);
    }

    public static void setPersistent(FormComponent component, boolean persistent) {
        component.setMetaData(PERSIST_KEY, persistent);
    }

    public static boolean isPersistent(FormComponent component) {
        return Boolean.TRUE.equals(component.getMetaData(PERSIST_KEY));
    }

    /**
     * Override wicket's cookie utils to enable encoding and decoding of cookie content.
     * See RTFACT-6086
     * @param formComponent
     * @return
     */
    protected static String load(final FormComponent<?> formComponent) {
        String value = COOKIE_UTILS.load(formComponent.getPageRelativePath());
        if (value != null) {
            // Assign the retrieved/persisted value to the component
            String[] values = value.split(FormComponent.VALUE_SEPARATOR);
            for (int i = 0; i < values.length; i++) {
                try {
                    values[i] = URLDecoder.decode(values[i], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.debug("Failed to decode cookie value: {}", e.getMessage());
                }
            }
            formComponent.setModelValue(values);
        }
        return value;
    }

    /**
     * Override wicket's cookie utils to enable encoding and decoding of cookie content.
     * See RTFACT-6086
     * @param formComponent
     * @return
     */
    protected static void save(final FormComponent<?> formComponent) {
        String key = formComponent.getPageRelativePath();
        String value = formComponent.getValue();
        try {
            COOKIE_UTILS.save(key, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.debug("Failed to encode cookie value: {}", e.getMessage());
            COOKIE_UTILS.save(key, value);
        }
    }
}