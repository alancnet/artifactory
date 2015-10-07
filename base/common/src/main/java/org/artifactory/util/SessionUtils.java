package org.artifactory.util;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Gidi Shabat
 */
public class SessionUtils {
    private static final String LAST_USER_KEY = "artifactory:lastUserId";


    /**
     * create updated session with remember me authentication
     *
     * @param request        - http servlet request
     * @param authentication - remember me authentication
     * @param createSession  - if true create session
     * @return
     */
    public static boolean setAuthentication(HttpServletRequest request, Authentication authentication,
            boolean createSession) {
        HttpSession session = request.getSession(createSession);
        if (session == null) {
            return false;
        }
        session.setAttribute(LAST_USER_KEY, authentication);
        return true;
    }
}
