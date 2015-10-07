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

package org.artifactory.webapp.spring;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.IBehaviorInstantiationListener;
import org.apache.wicket.IClusterable;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.ISpringContextLocator;
import org.apache.wicket.util.lang.Args;
import org.artifactory.webapp.servlet.RequestUtils;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author yoavl
 */
public class ArtifactorySpringComponentInjector extends Injector
        implements IComponentInstantiationListener, IBehaviorInstantiationListener {

    /**
     * Metadata key used to store application context holder in application's metadata
     */
    private static MetaDataKey<ApplicationContextHolder> CONTEXT_KEY = new MetaDataKey<ApplicationContextHolder>() {
        private static final long serialVersionUID = 1L;
    };

    private final IFieldValueFactory fieldValueFactory;

    public ArtifactorySpringComponentInjector(WebApplication webapp) {
        Args.notNull(webapp, "webapp");

        ApplicationContext ctx = get(webapp);
        Args.notNull(ctx, "ctx");

        // store context in application's metadata ...
        webapp.setMetaData(CONTEXT_KEY, new ApplicationContextHolder(ctx));

        //
        fieldValueFactory = new ArtifactoryContextAnnotFieldValueFactory(new NonCachingContextLocator());
        bind(webapp);
    }

    private static ApplicationContext get(WebApplication webapp) {
        ServletContext sc = webapp.getServletContext();
        ApplicationContext ac = (ApplicationContext) RequestUtils.getArtifactoryContext(sc);
        return ac;
    }


    @Override
    public void inject(final Object object) {
        inject(object, fieldValueFactory);
    }

    @Override
    public void onInstantiation(final Component component) {
        inject(component);
    }

    @Override
    public void onInstantiation(Behavior behavior) {
        inject(behavior);
    }

    /**
     * This is a holder for the application context. The reason we need a holder is that metadata only supports storing
     * serializable objects but application context is not. The holder acts as a serializable wrapper for the context.
     * Notice that although holder implements IClusterable it really is not because it has a reference to non
     * serializable context - but this is ok because metadata objects in application are never serialized.
     *
     * @author ivaynberg
     */
    private static class ApplicationContextHolder implements IClusterable {
        private static final long serialVersionUID = 1L;

        private final ApplicationContext context;

        /**
         * Constructor
         *
         * @param context
         */
        public ApplicationContextHolder(ApplicationContext context) {
            this.context = context;
        }

        /**
         * @return the context
         */
        public ApplicationContext getContext() {
            return context;
        }
    }

    private static class NonCachingContextLocator implements ISpringContextLocator {

        private static final long serialVersionUID = 1L;

        @Override
        public ApplicationContext getSpringContext() {
            return ((ApplicationContextHolder) Application.get().getMetaData(CONTEXT_KEY)).getContext();
        }
    }
}
