/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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

package org.artifactory.storage.db.itest.spring;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.DbType;
import org.artifactory.version.CompoundVersionDetails;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Callable;

/**
 * Date: 8/5/13 10:56 AM
 *
 * @author freds
 */
@Configuration
public class DbUpgradeTestConfigFactory implements BeanFactoryAware {

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    }

    @Bean
    public DbService createDummyDbService() {
        return new DummyDbService();
    }
}

class DummyDbService implements DbService {
    @Override
    public long nextId() {
        return 0;
    }

    @Override
    public DbType getDatabaseType() {
        return DbType.DERBY;
    }

    @Override
    public void compressDerbyDb(BasicStatusHolder statusHolder) {
        // Nothing
    }

    @Override
    public <T> T invokeInTransaction(String transactionName, Callable<T> execute) {
        return null;
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
        // Nothing
    }

    @Override
    public void init() {
        // Nothing
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
        // Nothing
    }

    @Override
    public void destroy() {
        // Nothing
    }
}
