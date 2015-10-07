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

package org.artifactory.storage.db.spring;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

/**
 * @author Yossi Shaul
 */
@Component("artifactoryTransactionManager")
public class DbTransactionManager extends DataSourceTransactionManager {
    private static final Logger log = LoggerFactory.getLogger(DbTransactionManager.class);

    @Autowired
    public DbTransactionManager(@Qualifier("dataSource") DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        super.prepareSynchronization(status, definition);
        if (status.isNewSynchronization() &&
                definition.getPropagationBehavior() != TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
            log.trace("Registering new session synchronization");
            StorageSessionFactory sessionFactory = ContextHelper.get().beanForType(StorageSessionFactory.class);
            StorageSession session = sessionFactory.create();
            TransactionSynchronizationManager.registerSynchronization(
                    new SessionSynchronization(session, definition.getName()));
        }
    }
}
