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

package org.artifactory.common;

import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * @author Yoav Landman
 */
public interface MutableStatusHolder extends StatusHolder {

    //TODO: [by YS] this should only be in the importexport status holder
    void setFastFail(boolean failFast);

    void setVerbose(boolean verbose);

    void debug(String message, @Nonnull Logger logger);

    void status(String message, @Nonnull Logger logger);

    void status(String message, int statusCode, @Nonnull Logger logger);

    void warn(String message, @Nonnull Logger logger);

    void warn(String message, Throwable throwable, @Nonnull Logger logger);

    void error(String message, @Nonnull Logger logger);

    void error(String message, int statusCode, @Nonnull Logger logger);

    void error(String message, Throwable throwable, @Nonnull Logger logger);

    void error(String message, int statusCode, Throwable throwable, @Nonnull Logger logger);

    void setActivateLogging(boolean activateLogging);

    void reset();
}
