package org.artifactory.webapp.wicket.page.config.bintray;

import org.apache.wicket.Component;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;
import org.artifactory.webapp.wicket.page.config.SchemaHelpModel;
import org.artifactory.webapp.wicket.page.security.profile.BintrayProfilePanel;

/**
 * Config panel for the Global(default) Bintray configuration
 *
 * @autor Dan Feldman
 */
public class GlobalBintrayConfigPanel<T> extends BintrayProfilePanel {

    public GlobalBintrayConfigPanel(String id, T model) {
        super(id, model, true);

        bintrayUsername.setDefaultModel(new PropertyModel<String>(model, "userName"));
        bintrayUsername.setEnabled(true);
        bintrayUsername.setRequired(false);

        bintrayApiKey.setDefaultModel(new PropertyModel<String>(model, "apiKey"));
        bintrayApiKey.setEnabled(true);
        bintrayApiKey.setRequired(false);

        testButton.setEnabled(true);
    }

    @Override
    public String getTitle() {
        return "Default Bintray Credentials";
    }

    @Override
    protected String getBintrayAuth() {
        return ((BintrayConfigDescriptor) getDefaultModelObject()).getBintrayAuth();
    }

    @Override
    protected String getBintrayUser() {
        return ((BintrayConfigDescriptor) getDefaultModelObject()).getUserName();
    }

    @Override
    protected String getBintrayApiKey() {
        return CryptoHelper.decryptIfNeeded(
                ((BintrayConfigDescriptor) getDefaultModelObject()).getApiKey());
    }

    @Override
    protected IModel getModel(Object model) {
        return new CompoundPropertyModel<>((BintrayConfigDescriptor) model);
    }

    @Override
    protected void addHelpBubbles() {
        add(new SchemaHelpBubble("bintrayUsername.help",
                new SchemaHelpModel((BintrayConfigDescriptor) getDefaultModelObject(), "userName")));
        add(new SchemaHelpBubble("bintrayApiKey.help",
                new SchemaHelpModel((BintrayConfigDescriptor) getDefaultModelObject(), "apiKey")));
    }

    @Override
    protected Component newToolbar(String id) {
        return new HelpBubble(id, new ResourceModel("globalBintray.help"));
    }
}
