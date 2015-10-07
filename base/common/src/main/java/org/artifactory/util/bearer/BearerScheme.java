package org.artifactory.util.bearer;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.RFC2617Scheme;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.artifactory.api.context.ContextHelper;

/**
 * Bearer authentication scheme as defined in RFC 2617
 *
 * @author Shay Yaakov
 */
public class BearerScheme extends RFC2617Scheme {
    private Credentials realCredentials;

    public BearerScheme(Credentials credentials) {
        this.realCredentials = credentials;
    }

    @Override
    public String getSchemeName() {
        return "bearer";
    }

    @Override
    public boolean isConnectionBased() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public Header authenticate(Credentials dummyCredentials, HttpRequest request) throws AuthenticationException {
        return authenticate(dummyCredentials, request, new BasicHttpContext());
    }

    @Override
    public Header authenticate(Credentials dummyCredentials, HttpRequest request, HttpContext context)
            throws AuthenticationException {
        TokenProvider tokenProvider = ContextHelper.get().beanForType(TokenProvider.class);
        String token = tokenProvider.getToken(getParameters(),
                request.getRequestLine().getMethod(),
                request.getRequestLine().getUri(),
                realCredentials);
        final CharArrayBuffer buffer = new CharArrayBuffer(32);
        buffer.append(AUTH.WWW_AUTH_RESP);
        buffer.append(": Bearer ");
        buffer.append(token);
        return new BufferedHeader(buffer);
    }
}
