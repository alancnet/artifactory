package org.artifactory.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 8/4/11
 * Time: 9:30 PM
 *
 * @author Fred Simon
 */
public abstract class BasicFactory {
    private static final Logger log = LoggerFactory.getLogger(BasicFactory.class);

    public static <T> T createInstance(Class<T> clazz, String className) {
        T result = null;
        try {
            Class<T> cls =
                    (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(className);
            result = cls.newInstance();
        } catch (Exception e) {
            log.error("Could not create the default factory object due to:" + e.getMessage(), e);
        }
        return result;
    }
}
