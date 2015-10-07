package org.artifactory.storage.fs.lock.aop;

import org.artifactory.sapi.common.Lock;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcher;

import java.lang.reflect.Method;

/**
 * Date: 8/5/11
 * Time: 12:17 PM
 *
 * @author Fred Simon
 */
public class LockingMethodMatcher extends StaticMethodMatcher {

    public LockingMethodMatcher() {
    }

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.isAnnotationPresent(Lock.class)) {
            return true;
        }
        // The method may be on an interface, so let's check on the target class as well.
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        if (specificMethod != method && (specificMethod.isAnnotationPresent(Lock.class))) {
            System.err.println("FOUND ONLY IN SPECIFIC METHOD FOR " + method.toGenericString());
            return true;
        }
        return false;
    }
}
