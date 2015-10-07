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

package org.artifactory.security;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.descriptor.security.PasswordSettings;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.crypto.SecretKey;
import java.security.KeyPair;

/**
 * This authentication manager will decrypted any encrypted passwords according to the password and encryption policy
 * and delegate the authentication to a standard authentication provider.
 *
 * @author Yossi Shaul
 */
public class PasswordDecryptingManager implements AuthenticationManager {
    private static final Logger log = LoggerFactory.getLogger(PasswordDecryptingManager.class);

    private AuthenticationManager delegate;

    @Autowired
    private CentralConfigService centralConfigService;

    @Autowired
    private UserGroupService userGroupService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.trace("Received authentication request for {}", authentication);
        String password = authentication.getCredentials().toString();
        UsernamePasswordAuthenticationToken decryptedAuthentication = null;
        try {
            if (needsDecryption(password, (authentication instanceof InternalUsernamePasswordAuthenticationToken))) {
                String username = authentication.getPrincipal().toString();
                log.trace("Decrypting user password for user '{}'", username);
                password = decryptPassword(password, username);
                decryptedAuthentication = new UsernamePasswordAuthenticationToken(username, password);
                decryptedAuthentication.setDetails(authentication.getDetails());
            }
            Authentication result;
            if (decryptedAuthentication != null) {
                result = delegate.authenticate(decryptedAuthentication);
            } else {
                result = delegate.authenticate(authentication);
            }
            return result;
        } catch (AuthenticationException e) {
            //We do not log non-ui successful logins (inflates the log), just failed logins
            AccessLogger.loginDenied(authentication);
            throw e;
        }
    }

    private boolean needsDecryption(String password, boolean internalRequest) {
        CentralConfigDescriptor centralConfigDescriptor = centralConfigService.getDescriptor();
        SecurityDescriptor securityDescriptor = centralConfigDescriptor.getSecurity();
        PasswordSettings passwordSettings = securityDescriptor.getPasswordSettings();
        boolean encryptionEnabled = passwordSettings.isEncryptionEnabled();
        if (!encryptionEnabled) {
            return false;
        }
        boolean isEncrypted = CryptoHelper.isPasswordEncrypted(password);
        log.trace("Detected {} password", isEncrypted ? "encrypted" : "cleartext");
        if (!isEncrypted) {
            if (!internalRequest && passwordSettings.isEncryptionRequired()) {
                log.debug("Cleartext passwords not allowed. Sending unauthorized response");
                throw new PasswordEncryptionException("Artifactory configured to accept only " +
                        "encrypted passwords but received a clear text password.");
            } else {
                return false;
            }
        }
        log.trace("Password needs decryption");
        return true;
    }

    private String decryptPassword(String encryptedPassword, String username) {
        if (!CryptoHelper.isPasswordEncrypted(encryptedPassword)) {
            throw new IllegalArgumentException("Password not encrypted");
        }

        KeyPair keyPair = getKeyPair(username);
        SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(keyPair);
        try {
            return CryptoHelper.decryptSymmetric(encryptedPassword, secretKey, false);
        } catch (Exception e) {
            log.debug("Failed to decrypt user password: " + e.getMessage());
            throw new PasswordEncryptionException("Failed to decrypt password.", e);
        }
    }

    private KeyPair getKeyPair(String username) {
        UserInfo userInfo = userGroupService.findUser(username);
        String privateKey = userInfo.getPrivateKey();
        String publicKey = userInfo.getPublicKey();
        if (privateKey == null || publicKey == null) {
            String message = "User with no key pair tries to authenticate with encrypted password.";
            log.trace(message);
            throw new PasswordEncryptionException(message);
        }

        return CryptoHelper.createKeyPair(privateKey, publicKey, false);
    }

    public void setDelegate(AuthenticationManager delegate) {
        this.delegate = delegate;
    }
}
