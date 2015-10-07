/*
 * Copyright 2012 JFrog Ltd. All rights reserved.
 * JFROG PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.artifactory.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public abstract class RealmAwareUserPassAuthenticationToken extends UsernamePasswordAuthenticationToken
        implements RealmAwareAuthentication {

    public RealmAwareUserPassAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public RealmAwareUserPassAuthenticationToken(Object principal, Object credentials,
            Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
