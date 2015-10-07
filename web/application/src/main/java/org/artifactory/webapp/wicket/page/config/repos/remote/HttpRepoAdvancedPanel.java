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

package org.artifactory.webapp.wicket.page.config.repos.remote;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.behavior.border.TitledBorderBehavior;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.ProxyDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class HttpRepoAdvancedPanel extends Panel {
    @SpringBean
    private CentralConfigService centralConfigService;

    private final HttpRepoDescriptor repoDescriptor;
    private Label incompatibleLayoutLabel;

    public HttpRepoAdvancedPanel(String id, CreateUpdateAction action, HttpRepoDescriptor descriptor,
            MutableCentralConfigDescriptor mutableDescriptor) {
        super(id);
        this.repoDescriptor = descriptor;

        add(new CssClass("advanced-remote-repo"));

        addLayoutFields(action);
        addNetworkFields(action, mutableDescriptor);
        addCacheFields();
        addOtherFields();
        addFlags(action);
    }

    private void addLayoutFields(CreateUpdateAction action) {
        final WebMarkupContainer layoutBorder = new WebMarkupContainer("layoutBorder");
        layoutBorder.setOutputMarkupId(true);
        layoutBorder.add(new TitledBorderBehavior("fieldset-border", "Layout"));
        add(layoutBorder);

        final StyledCheckbox suppressPomConsistencyChecks = new StyledCheckbox("suppressPomConsistencyChecks");
        boolean isMavenRepoLayout = repoDescriptor.isMavenRepoLayout() || (repoDescriptor.getRepoLayout() == null);
        suppressPomConsistencyChecks.setOutputMarkupId(true);
        suppressPomConsistencyChecks.setEnabled(isMavenRepoLayout);

        layoutBorder.add(suppressPomConsistencyChecks);
        layoutBorder.add(new SchemaHelpBubble("suppressPomConsistencyChecks.help"));

        List<RepoLayout> layouts = centralConfigService.getDescriptor().getRepoLayouts();

        DropDownChoice<RepoLayout> repoLayout =
                new DropDownChoice<>("repoLayout", layouts, new ChoiceRenderer<RepoLayout>("name"));
        repoLayout.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                suppressPomConsistencyChecks.setEnabled(repoDescriptor.isMavenRepoLayout());
                suppressPomConsistencyChecks.setDefaultModelObject(!repoDescriptor.isMavenRepoLayout());
                checkLayoutCompatibility();
                target.add(suppressPomConsistencyChecks);

                removeLayoutBorderBehaviors(layoutBorder);
                target.add(layoutBorder);
            }
        });
        if (repoDescriptor.getRepoLayout() == null) {
            repoLayout.setModel(new PropertyModel<RepoLayout>(repoDescriptor, "repoLayout"));
            repoLayout.setDefaultModelObject(RepoLayoutUtils.MAVEN_2_DEFAULT);
        }
        repoLayout.setNullValid(false);
        repoLayout.setRequired(true);
        repoLayout.setEnabled(CreateUpdateAction.CREATE.equals(action));
        layoutBorder.add(repoLayout);
        layoutBorder.add(new SchemaHelpBubble("repoLayout.help"));

        DropDownChoice<RepoLayout> remoteRepoLayout = new DropDownChoice<>("remoteRepoLayout", layouts,
                new ChoiceRenderer<RepoLayout>("name"));
        remoteRepoLayout.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                checkLayoutCompatibility();
                removeLayoutBorderBehaviors(layoutBorder);
                target.add(layoutBorder);
            }
        });
        remoteRepoLayout.setNullValid(true);
        layoutBorder.add(remoteRepoLayout);
        layoutBorder.add(new SchemaHelpBubble("remoteRepoLayout.help"));

        incompatibleLayoutLabel = new Label("incompatibleLayoutLabel",
                "Not all tokens can be mapped between the source and the target " +
                        "layout, which may cause path translation not to work as expected.");
        incompatibleLayoutLabel.setOutputMarkupId(true);
        checkLayoutCompatibility();
        layoutBorder.add(incompatibleLayoutLabel);
    }

    private void checkLayoutCompatibility() {
        RepoLayout cacheLayout = repoDescriptor.getRepoLayout();
        RepoLayout remoteLayout = repoDescriptor.getRemoteRepoLayout();
        if ((cacheLayout == null) || (remoteLayout == null)) {
            incompatibleLayoutLabel.setVisible(false);
            return;
        }

        incompatibleLayoutLabel.setVisible(!RepoLayoutUtils.layoutsAreCompatible(cacheLayout, remoteLayout));
    }

    private void removeLayoutBorderBehaviors(WebMarkupContainer layoutBorder) {
        for (Behavior behavior : layoutBorder.getBehaviors()) {
            if (behavior instanceof TitledBorderBehavior) {
                layoutBorder.remove(behavior);
            }
        }
    }

    private void addNetworkFields(CreateUpdateAction action, MutableCentralConfigDescriptor mutableDescriptor) {
        WebMarkupContainer networkBorder = new WebMarkupContainer("networkBorder");
        networkBorder.add(new TitledBorderBehavior("fieldset-border", "Network"));
        add(networkBorder);

        // proxy
        List<ProxyDescriptor> proxies = mutableDescriptor.getProxies();
        DropDownChoice<ProxyDescriptor> proxiesDropDown = new DropDownChoice<>("proxy", proxies,
                new ChoiceRenderer<ProxyDescriptor>("key", "key"));
        ProxyDescriptor defaultProxyDescriptor = mutableDescriptor.getDefaultProxy();
        if (defaultProxyDescriptor != null && CreateUpdateAction.CREATE.equals(action)) {
            proxiesDropDown.setDefaultModel(new Model<>(defaultProxyDescriptor));
        }
        proxiesDropDown.setNullValid(true);
        networkBorder.add(proxiesDropDown);
        networkBorder.add(new SchemaHelpBubble("proxy.help"));

        // localAddress
        TextField<String> localAddressField = new TextField<>("localAddress");
        localAddressField.add(new LocalAddressValidator());
        networkBorder.add(localAddressField);
        networkBorder.add(new SchemaHelpBubble("localAddress.help"));

        // socketTimeout
        networkBorder.add(new TextField<>("socketTimeoutMillis", Integer.class).setRequired(true));
        networkBorder.add(new SchemaHelpBubble("socketTimeoutMillis.help"));

        // allow passing authentication to redirected hosts
        networkBorder.add(new StyledCheckbox("allowAnyHostAuth"));
        networkBorder.add(new SchemaHelpBubble("allowAnyHostAuth.help"));

        // enable cookies
        networkBorder.add(new StyledCheckbox("enableCookieManagement"));
        networkBorder.add(new SchemaHelpBubble("enableCookieManagement.help"));

        // username
        networkBorder.add(new TextField("username"));
        networkBorder.add(new SchemaHelpBubble("username.help"));

        // password
        PasswordTextField passwordField = new PasswordTextField("password");
        passwordField.setRequired(false);
        passwordField.setResetPassword(false);
        networkBorder.add(passwordField);
        networkBorder.add(new SchemaHelpBubble("password.help"));
    }

    private void addCacheFields() {
        WebMarkupContainer cacheBorder = new WebMarkupContainer("cacheBorder");
        cacheBorder.add(new TitledBorderBehavior("fieldset-border", "Cache"));
        add(cacheBorder);

        // unusedArtifactsCleanupPeriodHours
        final TextField<Integer> unusedCleanupTextField = new TextField<>("unusedArtifactsCleanupPeriodHours",
                Integer.class);
        unusedCleanupTextField.add(new MinimumValidator<>(0)).setRequired(true);
        unusedCleanupTextField.setOutputMarkupId(true);
        cacheBorder.add(unusedCleanupTextField);
        cacheBorder.add(new SchemaHelpBubble("unusedArtifactsCleanupPeriodHours.help"));

        addDurationField(cacheBorder, "retrievalCachePeriodSecs");
        addDurationField(cacheBorder, "assumedOfflinePeriodSecs");
        addDurationField(cacheBorder, "missedRetrievalCachePeriodSecs");
    }

    private void addDurationField(WebMarkupContainer cacheBorder, String fieldName) {
        cacheBorder.add(new TextField<>(fieldName, Long.class).setRequired(true).
                add(new MinimumValidator<>(0L)));
        cacheBorder.add(new SchemaHelpBubble(fieldName + ".help"));
    }

    private void addOtherFields() {
        WebMarkupContainer otherBorder = new WebMarkupContainer("otherBorder");
        otherBorder.add(new TitledBorderBehavior("fieldset-border", "Other"));
        add(otherBorder);

        // query params
        otherBorder.add(new TextField("queryParams"));
        otherBorder.add(new SchemaHelpBubble("queryParams.help"));
    }

    private void addFlags(CreateUpdateAction action) {
        add(new StyledCheckbox("rejectInvalidJars"));
        add(new SchemaHelpBubble("rejectInvalidJars.help"));

        add(new StyledCheckbox("synchronizeProperties"));
        add(new SchemaHelpBubble("synchronizeProperties.help"));

        add(new StyledCheckbox("fetchJarsEagerly"));
        add(new SchemaHelpBubble("fetchJarsEagerly.help"));

        add(new StyledCheckbox("fetchSourcesEagerly"));
        add(new SchemaHelpBubble("fetchSourcesEagerly.help"));

        add(new StyledCheckbox("hardFail"));
        add(new SchemaHelpBubble("hardFail.help"));

        add(new StyledCheckbox("archiveBrowsingEnabled"));
        add(new SchemaHelpBubble("archiveBrowsingEnabled.help"));

        final StyledCheckbox checkbox = new StyledCheckbox("storeArtifactsLocally");
        boolean storeArtifactsLocallyValue = CreateUpdateAction.UPDATE.equals(action)
                && !repoDescriptor.isStoreArtifactsLocally();
        checkbox.setDefaultModel(new Model<>(storeArtifactsLocallyValue));
        checkbox.add(new AjaxFormComponentUpdatingBehavior("onclick") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                repoDescriptor.setStoreArtifactsLocally(!checkbox.isChecked());
            }
        });
        add(checkbox);
        add(new SchemaHelpBubble("storeArtifactsLocally.help"));

        final StyledCheckbox listRemoteFolderItemsCheckbox = new StyledCheckbox("listRemoteFolderItems");
        add(listRemoteFolderItemsCheckbox);
        add(new SchemaHelpBubble("listRemoteFolderItems.help"));

    }

    private static class LocalAddressValidator extends StringValidator {
        @SuppressWarnings({"ResultOfMethodCallIgnored"})
        @Override
        protected void onValidate(IValidatable validatable) {
            String localAddress = (String) validatable.getValue();
            try {
                InetAddress.getByName(localAddress);
            } catch (UnknownHostException e) {
                ValidationError error = new ValidationError();
                error.setMessage("Invalid local address: " + e.getMessage());
                validatable.error(error);
            }
        }
    }
}
