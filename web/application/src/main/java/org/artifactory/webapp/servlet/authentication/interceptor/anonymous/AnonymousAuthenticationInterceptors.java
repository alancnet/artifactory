package org.artifactory.webapp.servlet.authentication.interceptor.anonymous;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation that holds and calls all registered {@link AnonymousAuthenticationInterceptor} instances that
 * were added to it.
 *
 * @author Dan Feldman
 */
public class AnonymousAuthenticationInterceptors implements AnonymousAuthenticationInterceptor {

    private final List<AnonymousAuthenticationInterceptor> interceptors = new ArrayList<>();

    public void addInterceptors(Collection<AnonymousAuthenticationInterceptor> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    @Override
    public boolean accept(HttpServletRequest request) {
        for (AnonymousAuthenticationInterceptor interceptor : interceptors) {
            if(interceptor.accept(request)) {
                return true;
            }
        }
        return false;
    }
}
