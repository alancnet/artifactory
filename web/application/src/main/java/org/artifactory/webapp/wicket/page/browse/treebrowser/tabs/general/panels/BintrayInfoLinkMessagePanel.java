package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.component.links.TitledPageLink;

/**
 * User: gidis
 */
public class BintrayInfoLinkMessagePanel extends Panel {
    public BintrayInfoLinkMessagePanel(String id, String message, final String url) {
        super(id);
        addLinkMessage(message, url);
        adddIconToPanel();
    }

    private void addLinkMessage(final String message, final String url) {
        setOutputMarkupId(true);
        TitledPageLink linkMessage = new TitledPageLink("linkMessage", message, null) {
            @Override
            protected CharSequence getURL() {
                return url;
            }
        };
        linkMessage.add(new AttributeModifier("target", "_blank"));
        add(linkMessage);
    }

    private void adddIconToPanel() {
        final String imageUrl = ConstantValues.bintrayApiUrl.getString() + "/packages/bintray/jcenter/unknownPackage/images/avatar";
        Image packageIcon = new Image("packageIcon", "Icon");
        packageIcon.add(new AttributeModifier("src", new AbstractReadOnlyModel() {
            @Override
            public Object getObject() {
                return imageUrl;
            }
        }));
        add(packageIcon);
    }
}
