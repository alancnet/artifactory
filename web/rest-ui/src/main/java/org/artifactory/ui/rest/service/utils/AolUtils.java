package org.artifactory.ui.rest.service.utils;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.ui.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Lior Azar
 */
public class AolUtils {
    private static final Logger log = LoggerFactory.getLogger(AolUtils.class);

    public static void assertNotAol(String functionName) {
        if (ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class).isAol()) {
            log.warn("{} is not supported when running on the cloud", functionName);
            throw new ForbiddenException("Function is not supported when running on the cloud");
        }
    }
}
