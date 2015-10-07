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

package org.artifactory.webapp.wicket.page.base;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.WebApplicationAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.panel.logo.BaseLogoPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.util.HttpUtils;
import org.artifactory.webapp.wicket.application.ArtifactoryApplication;

/**
 * @author Tomer Cohen
 */
public class HeaderLogoPanel extends BaseLogoPanel {
    @SpringBean
    private AddonsManager addons;

    @SpringBean
    private CentralConfigService centralConfig;

    public HeaderLogoPanel(String id) {
        super(id);
    }

    @Override
    protected Class<? extends Page> getLinkPage() {
        WebApplicationAddon applicationAddon = addons.addonByType(WebApplicationAddon.class);
        return applicationAddon.getHomePage();
    }

    @Override
    protected String getLogoUrl() {
        String descriptorLogo = centralConfig.getDescriptor().getLogo();
        if (StringUtils.isNotBlank(descriptorLogo)) {
            return descriptorLogo;
        }

        final ArtifactoryApplication application = ArtifactoryApplication.get();
        if (application.isLogoExists()) {
            return HttpUtils.getWebappContextUrl(
                    WicketUtils.getHttpServletRequest()) + "logo?" + application.getLogoModifyTime();
        }

        return null;
    }

}

