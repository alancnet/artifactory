package org.artifactory.build;

import org.jfrog.build.api.Build;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Utility class to work with build info common operation.
 *
 * @author Yossi Shaul
 */
public abstract class BuildInfoUtils {

    private static final DateTimeFormatter BUILD_FORMATTER = DateTimeFormat.forPattern(Build.STARTED_FORMAT);

    /**
     * @param time Time in millis to format
     * @return Formatted time using the {@link org.jfrog.build.api.Build#STARTED_FORMAT} time format (ISO time format).
     */
    public static String formatBuildTime(long time) {
        return BUILD_FORMATTER.print(time);
    }

    /**
     * @param buildTimeFormat Build time with {@link org.jfrog.build.api.Build#STARTED_FORMAT} format
     * @return Time in millis represented by the string
     */
    public static long parseBuildTime(String buildTimeFormat) {
        return BUILD_FORMATTER.parseDateTime(buildTimeFormat).getMillis();
    }
}
