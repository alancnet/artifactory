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

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.quartz.CronExpression;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Date;

/**
 * A utility class for Cron Expressions.
 *
 * @author Noam Tenne
 */
public abstract class CronUtils {

    private static final Minutes MINIMUM_ALLOWED_MINUTES = Minutes.minutes(5);

    private CronUtils() {
        // utility class
    }

    /**
     * Returns a boolean value representing the validity of a given Cron Expression
     *
     * @param cronExpression A Cron Expression
     * @return boolean - Is given expression valid
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * Returns a String value representing the validity message given invalid Cron Expression
     *
     * @param cronExpression A Cron Expression
     * @return String - the invalid message returned when expression is not valid, or null if valid
     */
    public static
    @Nullable
    String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * Returns the next execution time based on the given Cron Expression
     *
     * @param cronExpression A Cron Expression
     * @return Date - The next time the given Cron Expression should fire
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Checks if the given cron expression interval is less or equals to a certain minimum.
     *
     * @param cronExpression the cron expression to check
     */
    public static boolean isCronIntervalLessThanMinimum(String cronExpression) {
        try {
            // If input is empty or invalid simply return false as default
            if (StringUtils.isBlank(cronExpression) || !isValid(cronExpression)) {
                return false;
            }

            CronExpression cron = new CronExpression(cronExpression);
            final Date firstExecution = cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
            final Date secondExecution = cron.getNextValidTimeAfter(firstExecution);

            Minutes intervalMinutes = Minutes.minutesBetween(new DateTime(firstExecution),
                    new DateTime(secondExecution));
            return !intervalMinutes.isGreaterThan(MINIMUM_ALLOWED_MINUTES);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
