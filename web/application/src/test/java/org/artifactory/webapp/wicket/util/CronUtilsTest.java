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

package org.artifactory.webapp.wicket.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Tests the different methods in the CronUtils class
 *
 * @author Noam Tenne
 */

public class CronUtilsTest {
    /**
     * An example of a legal Cron Expression
     */
    private static final String LEGAL_EXPRESSION = "0 0 /1 * * ?";

    /**
     * Tests the validity of a legal cron expression
     */
    @Test
    public void validExpression() {
        Assert.assertTrue(CronUtils.isValid(LEGAL_EXPRESSION));
    }

    /**
     * Tests the validity of a null cron expression
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullExpression() {
        Assert.assertFalse(CronUtils.isValid(null));
    }

    /**
     * Tests the validity of an empty cron expression
     */
    @Test
    public void emptyExpression() {
        Assert.assertFalse(CronUtils.isValid(""));
    }

    /**
     * Tests the validity of an illegal cron expression
     */
    @Test
    public void illegalExpression() {
        Assert.assertFalse(CronUtils.isValid("* /* 8"));
    }

    /**
     * Tests the validity of the next run
     */
    @Test
    public void nextRun() {
        Date date = CronUtils.getNextExecution(LEGAL_EXPRESSION);
        Assert.assertFalse(date == null);
        if (date != null) {
            Assert.assertTrue(date.after(new Date(System.currentTimeMillis())));
        }
    }
}
