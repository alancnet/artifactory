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

package org.artifactory.logging.converter;

import ch.qos.logback.classic.pattern.LineOfCallerConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Special converter to get the line number of the caller class. Since we sometimes pass the logger into another class
 * and print the message from there, the default implementation of getting the first element out of the stacktrace no
 * longer works and we have to dig deeper in the stacktrace to get the correct element.
 *
 * @author Tomer Cohen
 * @see ch.qos.logback.classic.pattern.LineOfCallerConverter
 */
public class BackTraceLineOfCallerConverter extends LineOfCallerConverter {
    /**
     * An override of the original logback implementation to get the line number of the calling class.
     *
     * @param event The logging event.
     * @return The calling classe's line number
     */
    @Override
    public String convert(ILoggingEvent event) {
        // we assume the logger name is the FQN of the class which it belongs to
        String originalLoggerCallerClassName = event.getLoggerName();
        StackTraceElement[] callerData = event.getCallerData();
        if (callerData != null && callerData.length > 0) {
            // dig into the stacktrace to find the correct class name from which to get the line number from.
            for (StackTraceElement element : callerData) {
                String stackClass = element.getClassName();
                if (stackClass != null && stackClass.startsWith(originalLoggerCallerClassName)) {
                    return Integer.toString(element.getLineNumber());
                }
            }
        }
        // In case we didn't find the right line number from the caller, fall back to logback's solution.
        return super.convert(event);
    }
}
