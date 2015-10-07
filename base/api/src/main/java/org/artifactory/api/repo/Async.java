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

package org.artifactory.api.repo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Schedule asynchronous invocation of a method. Cannot be combined with the @Lock annotation. If the method returns
 * a value it must be of type {@link java.util.concurrent.Future}.
 *
 * @author Yoav Landman
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Async {
    /**
     * Transactional will execute the async in a transaction.
     */
    boolean transactional() default false;

    /**
     * Delay the execution until the current transaction (if any) commits successfully.
     */
    boolean delayUntilAfterCommit() default false;

    /**
     * Share the thread with other async callbacks registered on the current thread - only applicable if
     * 'delayUntilAfterCommit' is true.
     */
    boolean shared() default true;

    /**
     * Don't execute if calling thread is not in a transaction.
     */
    boolean failIfNotScheduledFromTransaction() default false;

    /**
     * Invoke the async method with {@link org.artifactory.api.security.SecurityService#authenticateAsSystem()}
     */
    boolean authenticateAsSystem() default false;
}