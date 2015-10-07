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

package org.artifactory.rest.servlet;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.spring.container.SpringComponentProviderFactory;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.webapp.servlet.DelayedInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.Filter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.concurrent.BlockingQueue;

/**
 * We use our own rest servlet for the initialization using ArtifactoryContext.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryRestServlet extends ServletContainer implements DelayedInit {
    private static final Logger log = LoggerFactory.getLogger(ArtifactoryRestServlet.class);

    private ServletConfig config;

    @SuppressWarnings({"unchecked"})
    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        BlockingQueue<Filter> waiters = (BlockingQueue<Filter>) config.getServletContext()
                .getAttribute(APPLICATION_CONTEXT_LOCK_KEY);
        if (waiters != null) {
            waiters.add(this);
        } else {
            //Servlet 2.5 lazy filter initing
            delayedInit();
        }
        // register the bean utils converter to fail if encounters a type mismatch between the destination
        // and the original, see RTFACT-3610
        BeanUtilsBean instance = BeanUtilsBean2.getInstance();
        instance.getConvertUtils().register(true, false, 0);
    }

    @Override
    public void delayedInit() throws ServletException {
        super.init(config);
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        try {
            //Register the OfflineRestFilter
            String filters = (String) rc.getProperties().get(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS);
            rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                    filters + ",org.artifactory.rest.filter.OfflineRestFilter");
            //Register the lICENSERestFilter
            filters = (String) rc.getProperties().get(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS);
            rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                    filters + ",org.artifactory.rest.filter.LicenseRestFilter");
            //Register spring as component provider
            ArtifactoryContext artifactoryContext = (ArtifactoryContext) getServletContext().getAttribute(
                    ArtifactoryContext.APPLICATION_CONTEXT_KEY);
            SpringComponentProviderFactory springComponentProviderFactory =
                    new SpringComponentProviderFactory(rc, (ConfigurableApplicationContext) artifactoryContext);
            wa.initiate(rc, springComponentProviderFactory);
        } catch (RuntimeException e) {
            log.error("Exception in initialization of the Rest servlet");
            throw e;
        }
    }
}
