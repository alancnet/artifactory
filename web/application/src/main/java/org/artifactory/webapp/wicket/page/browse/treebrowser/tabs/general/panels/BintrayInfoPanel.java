package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.artifactory.api.bintray.BintrayPackageInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.border.fieldset.FieldSetBorder;
import org.artifactory.common.wicket.component.links.TitledPageLink;


/**
 * Bintray  info panel display the item info from Bintray
 *
 * @author Gidi Shabat
 */
public class BintrayInfoPanel extends Panel {
    private final FieldSetBorder infoBorder;

    public BintrayInfoPanel(String id, final BintrayPackageInfo packageInfo) {
        super(id);
        String description = packageInfo.getDesc() != null ? packageInfo.getDesc() : "";
        String rating = packageInfo.getRating() != null ? ", " + packageInfo.getRating() : "";
        final String imageUrl = ConstantValues.bintrayApiUrl.getString() + "/packages/bintray/jcenter/" + packageInfo.getName() + "/images/avatar";
        String information = String.format("%s%s", rating, description);
        infoBorder = new FieldSetBorder("infoBorder") {
            public String getTitle() {
                return "";
            }
        };
        // Add title link to panel
        addTitleLinkToPanel(packageInfo);
        // Add Icon to panel
        addIconToPanel(imageUrl);
        // Add information label
        addInformationLabelToPanel(information);
        // Add latest version link
        addLatestVersionToPanel(packageInfo);
        add(infoBorder);
    }

    private void addLatestVersionToPanel(final BintrayPackageInfo packageInfo) {
        String latestVersion = String.format("%s", packageInfo.getLatest_version());
        TitledPageLink latestVersionLink = new TitledPageLink("latestVersion", latestVersion, null) {
            @Override
            protected CharSequence getURL() {
                StringBuilder urlBuilder = new StringBuilder(
                        ConstantValues.bintrayUrl.getString() + "/version/show/general").
                        append("/").append(packageInfo.getOwner()).
                        append("/").append(packageInfo.getRepo()).
                        append("/").append(packageInfo.getName()).
                        append("/").append(packageInfo.getLatest_version());
                return urlBuilder.toString();
            }
        };
        latestVersionLink.add(new AttributeModifier("target", "_blank"));
        infoBorder.add(latestVersionLink);
    }

    private void addInformationLabelToPanel(String information) {
        infoBorder.add(new Label("information", Model.of(information)));
    }

    private void addIconToPanel(final String imageUrl) {
        Image packageIcon = new Image("packageIcon", "Icon");
        packageIcon.add(new AttributeModifier("src", new AbstractReadOnlyModel() {
            @Override
            public Object getObject() {
                return imageUrl;
            }
        }));
        infoBorder.add(packageIcon);
    }

    private void addTitleLinkToPanel(final BintrayPackageInfo packageInfo) {
        TitledPageLink titleLink = new TitledPageLink("titleLink", packageInfo.getName(), null) {
            @Override
            protected CharSequence getURL() {
                StringBuilder urlBuilder = new StringBuilder(
                        ConstantValues.bintrayUrl.getString() + "/pkg/show/general").
                        append("/").append(packageInfo.getOwner()).
                        append("/").append(packageInfo.getRepo()).
                        append("/").append(packageInfo.getName());
                return urlBuilder.toString();
            }
        };
        titleLink.add(new AttributeModifier("target", "_blank"));
        infoBorder.add(titleLink);
    }
}
