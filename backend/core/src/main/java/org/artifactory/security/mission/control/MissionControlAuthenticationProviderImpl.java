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
 */
package org.artifactory.security.mission.control;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.SimpleUser;
import org.artifactory.security.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * @author Gidi Shabat
 */
public class MissionControlAuthenticationProviderImpl implements MissionControlAuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(MissionControlAuthenticationProviderImpl.class);

    @Override
    public Authentication getFullAuthentication(String userName) {
        log.debug("Starting to generate Mission control authentication");
        UserGroupService userGroupService = ContextHelper.get().beanForType(UserGroupService.class);
        UserInfo userInfo = userGroupService.findOrCreateExternalAuthUser(userName, true);
        MutableUserInfo mutableUserInfo = InfoFactoryHolder.get().copyUser(userInfo);
        mutableUserInfo.setRealm(MissionControlAuthenticationProvider.REALM);
        mutableUserInfo.setAdmin(true);
        userInfo = mutableUserInfo;
        SimpleUser user = new SimpleUser(userInfo);
        // create new authentication response containing the user and it's authorities.
        log.debug("Finished to create Mission Control authentication");
        return new MissionControlAuthenticationToken(user, user.getAuthorities());
    }

    @Override
    public Authentication getAnonymousAuthentication() {
        log.debug("Starting to generate Anonymous authentication");
        UserGroupService userGroupService = ContextHelper.get().beanForType(UserGroupService.class);
        UserInfo userInfo = userGroupService.findOrCreateExternalAuthUser(UserInfo.ANONYMOUS, true);
        MutableUserInfo mutableUserInfo = InfoFactoryHolder.get().copyUser(userInfo);
        mutableUserInfo.setRealm(MissionControlAuthenticationProvider.REALM);
        userInfo = mutableUserInfo;
        SimpleUser user = new SimpleUser(userInfo);
        // create new authentication response containing the user and it's authorities.
        log.debug("Finished to create Anonymous authentication");
        return new MissionControlAuthenticationToken(user, user.getAuthorities());
    }
}
