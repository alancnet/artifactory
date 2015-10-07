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

package org.artifactory.webapp.wicket.page.security.profile;

import org.apache.wicket.Component;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.panel.titled.TitledPanel;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.security.MutableUserInfo;
import org.artifactory.security.UserInfo;
import org.artifactory.security.crypto.CryptoHelper;

import javax.crypto.SecretKey;

import static org.artifactory.common.wicket.component.label.highlighter.Syntax.xml;

/**
 * @author Shay Yaakov
 */
public class MavenSettingsPanel extends TitledPanel {

    public MavenSettingsPanel(String id, ProfileModel profile) {
        super(id);
        setOutputMarkupId(true);
        add(new CssClass("profile-panel"));
        setDefaultModel(new CompoundPropertyModel<>(profile));

        WebMarkupContainer settingsSnippet = new WebMarkupContainer("settingsSnippet");
        settingsSnippet.setVisible(false);
        add(settingsSnippet);
    }

    @Override
    public void onEvent(IEvent<?> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof ProfileEvent)) {
            return;
        }

        MutableUserInfo mutableUser = ((ProfileEvent) payload).getMutableUser();
        displayEncryptedPassword(mutableUser);
    }

    public void displayEncryptedPassword(MutableUserInfo userInfo) {
        WebMarkupContainer settingsSnippet = new WebMarkupContainer("settingsSnippet");
        String currentPassword = getUserProfile().getCurrentPassword();
        SecretKey secretKey = CryptoHelper.generatePbeKeyFromKeyPair(userInfo.getPrivateKey(), userInfo.getPublicKey(),
                false);
        String encryptedPassword = CryptoHelper.encryptSymmetric(currentPassword, secretKey, false);
        settingsSnippet.add(createSettingXml(userInfo, encryptedPassword));

        // With Base58 new encryption this not needed anymore
        Component nonMavenPasswordLabel = new WebMarkupContainer("nonMavenPassword");
        settingsSnippet.add(nonMavenPasswordLabel);
        if (ConstantValues.securityUseBase64.getBoolean()) {
            nonMavenPasswordLabel.replaceWith(new Label("nonMavenPassword",
                    "Non-maven clients should use a non-escaped password: " + encryptedPassword));
        }
        replace(settingsSnippet);
    }

    private ProfileModel getUserProfile() {
        return (ProfileModel) getDefaultModelObject();
    }

    private Component createSettingXml(UserInfo userInfo, String encryptedPassword) {
        encryptedPassword = CryptoHelper.needsEscaping(encryptedPassword);
        StringBuilder sb = new StringBuilder();
        sb.append("<server>\n");
        sb.append("    <id>${server-id}</id>\n");
        sb.append("    <username>").append(userInfo.getUsername()).append("</username>\n");
        sb.append("    <password>").append(encryptedPassword).append("</password>\n");
        sb.append("</server>");

        FieldSetBorder border = new FieldSetBorder("settingsBorder");
        add(border);

        border.add(WicketUtils.getSyntaxHighlighter("settingsDeclaration", sb.toString(), xml));
        return border;
    }

    @Override
    public String getTitle() {
        return "Maven Settings";
    }
}
