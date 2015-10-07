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

package org.artifactory.webapp.wicket.page.home.addon;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonInfo;
import org.artifactory.addon.AddonState;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.AddonsWebManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.webapp.wicket.page.config.license.LicensePage;

import java.util.List;

/**
 * Displays the table of the installed addons
 *
 * @author Noam Y. Tenne
 */
public class AddonsInfoPanel extends TitledPanel {

    @SpringBean
    private AddonsManager addonsManager;

    @SpringBean
    private AddonsWebManager addonsWebManager;

    @SpringBean
    private AuthorizationService authorizationService;

    @SpringBean
    private CentralConfigService centralConfigService;

    /**
     * Main constructor
     *
     * @param id              ID to assign to panel
     * @param installedAddons Name list of installed addons
     * @param noEnabledAddons True if no addons are enabled
     */
    public AddonsInfoPanel(String id, List<AddonInfo> installedAddons, boolean noEnabledAddons) {
        super(id);
        add(new CssClass("addons-table"));

        final boolean currentLicenseValid = addonsManager.isLicenseInstalled();

        MarkupContainer addonTable = new WebMarkupContainer("addonTable");
        boolean noAddons = installedAddons.isEmpty();
        boolean admin = authorizationService.isAdmin();

        addonTable.setVisible(!noAddons);
        addonTable.setOutputMarkupId(true);

        Component listView = new ListView<AddonInfo>("addonItem", installedAddons) {
            @Override
            protected void populateItem(ListItem<AddonInfo> item) {
                AddonInfo addonInfo = item.getModelObject();

                item.add(new ExternalLink("name", getAddonUrl(addonInfo.getAddonName()),
                        addonInfo.getAddonDisplayName()));
                item.add(new Label("image", "").add(new CssClass("addon-" + addonInfo.getAddonName())));

                String stateString = getAddonStatus(addonInfo.getAddonState());
                item.add(new Label("status", stateString));
                if (item.getIndex() % 2 == 0) {
                    item.add(new CssClass("even"));
                }
            }
        };
        addonTable.add(listView);
        add(addonTable);

        add(new Label("addonsDisabled", "No addons available")
                .setVisible(currentLicenseValid && !noAddons && noEnabledAddons));

        add(new Label("noAddons", "No add-ons currently installed.").setVisible(noAddons));

        String licenseRequiredMessage = addonsWebManager.getLicenseRequiredMessage(
                WicketUtils.absoluteMountPathForPage(LicensePage.class));
        Label noLicenseKeyLabel = new Label("noLicenseKey", licenseRequiredMessage);
        noLicenseKeyLabel.setVisible(
                admin && !currentLicenseValid && !noAddons && noEnabledAddons);
        noLicenseKeyLabel.setEscapeModelStrings(false);

        add(noLicenseKeyLabel);
    }

    private String getAddonStatus(AddonState addonState) {
        switch (addonState) {
            case NOT_CONFIGURED:    // Fall-through
            case ACTIVATED:
                return "Available";
            case DISABLED:
                return "Disabled";
            case INACTIVATED:   // Fall-through
            case NOT_LICENSED:  // Fall-through
            default:
                return "Not Available";
        }
    }

    @Override
    public String getTitle() {
        return "Available Add-ons";
    }

    private String getAddonUrl(String addonId) {
        return String.format(ConstantValues.addonsInfoUrl.getString(), addonId);
    }
}