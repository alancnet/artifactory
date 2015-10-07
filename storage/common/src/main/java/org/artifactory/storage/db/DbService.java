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

package org.artifactory.storage.db;

import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.spring.ReloadableBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

/**
 * @author Yossi Shaul
 */
public interface DbService extends ReloadableBean {

    /**
     * A marker for an invalid database id. Should be used to mark non-persisted entities and as the return id an
     * entity is not found in the database
     */
    int NO_DB_ID = -1;

    /**
     * @return Next unique id that can be used to use as row id in any of the tables.
     */
    long nextId();

    /**
     * @return The database type used by Artifactory.
     */
    DbType getDatabaseType();

    /**
     * Compresses the derby database files.
     *
     * @param statusHolder
     */
    void compressDerbyDb(BasicStatusHolder statusHolder);

    /**
     * Enforce a new separate transaction to execute the given callable statement.
     *
     * @param <T>             The return type
     * @param transactionName Symbolic name of the transaction
     * @param execute         The Callable statement execute inside the NEW TX
     * @return Whatever the callable returned
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    <T> T invokeInTransaction(String transactionName, Callable<T> execute);
}
