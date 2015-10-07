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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Tests the ExceptionUtils.
 *
 * @author Yossi Shaul
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class ExceptionUtilsTest {
    @Test
    public void testUnwrapThrowablesOfTypes() {
        IOException ioException = new IOException();
        IllegalArgumentException e = new IllegalArgumentException((new RuntimeException(ioException)));
        Throwable cause = ExceptionUtils.unwrapThrowablesOfTypes(e, IOException.class);
        Assert.assertSame(cause, cause, "Nothing should be wrapped");
        Throwable ioCause = ExceptionUtils.unwrapThrowablesOfTypes(e, RuntimeException.class);
        Assert.assertSame(ioCause, ioException, "Should have unwrapped any runtime exceptions");
    }

    @Test
    public void testGetCauseOfTypes() {
        IOException ioException = new IOException();
        IllegalArgumentException e = new IllegalArgumentException((new RuntimeException(ioException)));
        Throwable ioCause = ExceptionUtils.getCauseOfTypes(e, IOException.class);
        Assert.assertSame(ioCause, ioException, "Should return the same wrapped io exception");
        Throwable notFound = ExceptionUtils.getCauseOfTypes(e, IllegalStateException.class);
        Assert.assertNull(notFound, "Should not have found this type of exception");
    }

    @Test
    public void testGetRootCauseNotNested() {
        IOException ioException = new IOException();
        Throwable rootCause = ExceptionUtils.getRootCause(ioException);
        Assert.assertSame(rootCause, ioException, "Should return the same io exception");
    }

    @Test
    public void testGetRootCauseNested() {
        IOException ioException = new IOException();
        Throwable rootCause = ExceptionUtils.getRootCause(ioException);
        Assert.assertSame(rootCause, ioException, "Should return the io exception");
    }
}
