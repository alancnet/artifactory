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
package org.artifactory.storage.db.util;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A dynamic proxy to wrap {@link java.sql.ResultSet}. Its purpose is to close associated connection and statement
 * whenever it is closed (this is a valid behaviour assuming pooling is performed).
 * <p/>
 * A simple delegator will also do the work but it's a bit too long.
 */
public final class ResultSetWrapper implements InvocationHandler {

    private final DataSource ds;
    private final Connection con;
    private final Statement stmt;
    private final ResultSet rs;

    /**
     * Creates a new {@link org.artifactory.storage.db.util.ResultSetWrapper} with the associated connection and
     * statement.
     *
     * @param con  The associated {@link java.sql.Connection}
     * @param stmt The associated {@link java.sql.Statement}
     * @param rs   The {@link java.sql.ResultSet} to wrap
     * @param ds   The data source of the connection (transactional aware)
     * @return Proxy to the result set
     */
    public static ResultSet newInstance(Connection con, Statement stmt, ResultSet rs, DataSource ds) {
        ResultSetWrapper proxy = new ResultSetWrapper(con, stmt, rs, ds);
        return (ResultSet) Proxy.newProxyInstance(rs.getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class}, proxy);
    }

    private ResultSetWrapper(Connection con, Statement stmt, ResultSet rs, DataSource ds) {
        this.con = con;
        this.stmt = stmt;
        this.rs = rs;
        this.ds = ds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("close".equals(method.getName())) {
            DbUtils.close(con, stmt, rs, ds);
            return null;
        } else {
            return method.invoke(rs, args);
        }
    }
}
