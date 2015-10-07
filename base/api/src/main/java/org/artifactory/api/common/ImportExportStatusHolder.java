package org.artifactory.api.common;

import org.artifactory.common.StatusEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * NOTE: WHEN CHANGING THE NAME OR THE PACKAGE OF THIS CLASS, MAKE SURE TO UPDATE TEST AND PRODUCTION LOGBACK
 * CONFIGURATION FILES WITH THE CHANGES AND CREATE A CONVERTER IF NEEDED. SOME APPENDERS DEPEND ON THIS.
 * <p/>
 * Specialized status holder for the import and export processes.
 *
 * @author Gidi Shabat
 */
public class ImportExportStatusHolder extends BasicStatusHolder {
    protected static final Logger log = LoggerFactory.getLogger(ImportExportStatusHolder.class);

    @Override
    protected void logEntry(@Nonnull StatusEntry entry, @Nonnull Logger logger) {
        if (isVerbose()) {
            doLogEntry(entry, log);
        }
        doLogEntry(entry, logger);
    }
}
