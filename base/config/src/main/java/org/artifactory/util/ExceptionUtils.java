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

package org.artifactory.util;

/**
 * @author Yoav Landman
 */
public abstract class ExceptionUtils {
    private ExceptionUtils() {
        // utility class
    }

    /**
     * Unwrap an exception
     *
     * @param throwable     the throwable to unwrap
     * @param typesToUnwrap the types to keep unwrapping
     * @return The wrapped cause
     */
    public static Throwable unwrapThrowablesOfTypes(Throwable throwable,
            Class<? extends Throwable>... typesToUnwrap) {
        if (!isTypeOf(throwable, typesToUnwrap)) {
            return throwable;
        }
        Throwable cause = throwable.getCause();
        if (cause != null) {
            cause = unwrapThrowablesOfTypes(cause, typesToUnwrap);
        } else {
            cause = throwable;
        }
        return cause;
    }

    /**
     * Unwrap an exception
     *
     * @param throwable  the throwable to examine
     * @param causeTypes the desired cause types to find
     * @return The wrapped cause or null if not found
     */
    public static Throwable getCauseOfTypes(Throwable throwable, Class<? extends Throwable>... causeTypes) {
        if (throwable != null) {
            if (isTypeOf(throwable, causeTypes)) {
                return throwable;
            } else {
                return getCauseOfTypes(throwable.getCause(), causeTypes);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the root cause of the exception. If the input throwable has no cause will return the input.
     *
     * @param throwable the throwable to examine
     * @return The root cause or itself if has no cause
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    public static CharSequence getStackTrace(Thread thread) {
        StringBuilder b = new StringBuilder();
        StackTraceElement[] elements = thread.getStackTrace();
        for (StackTraceElement element : elements) {
            b.append(element.toString()).append('\n');
        }
        return b;
    }

    private static boolean isTypeOf(Throwable source, Class<? extends Throwable>... targetTypes) {
        Class<? extends Throwable> sourceType = source.getClass();
        for (Class<? extends Throwable> targetType : targetTypes) {
            if (targetType.isAssignableFrom(sourceType)) {
                return true;
            }
        }
        return false;
    }
}
