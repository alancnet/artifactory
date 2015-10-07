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

package org.artifactory.webapp.wicket.page.browse.treebrowser.action;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.Repo;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.defaultbutton.DefaultButtonBehavior;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.common.wicket.component.combobox.ComboBox;
import org.artifactory.common.wicket.component.form.SecureForm;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.component.modal.ModalHandler;
import org.artifactory.common.wicket.component.modal.links.ModalCloseLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.common.wicket.util.WicketUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Shay Yaakov
 */
public abstract class BintrayBasePanel extends BaseModalPanel {

    @SpringBean
    protected RepositoryService repoService;

    @SpringBean
    protected BintrayService bintrayService;

    protected BintrayParams bintrayModel;

    private ComboBox packagesComboBox;
    private ComboBox versionsComboBox;
    private Map<String, String> headersMap = WicketUtils.getHeadersMap();

    @Override
    public String getTitle() {
        return "Push To Bintray";
    }

    protected abstract void initBintrayModel();

    protected abstract boolean isFieldRequired();

    protected void initComponents() {
        MarkupContainer border = new TitledBorder("border");
        add(border);

        Label descriptionLabel = new Label("descriptionLabel", getBintrayDescriptionLabel());
        descriptionLabel.setEscapeModelStrings(false);
        border.add(descriptionLabel);

        final Form form = new SecureForm("form");
        form.setOutputMarkupId(true);
        border.add(form);

        final ComboBox reposDropDown = new ComboBox("repo",
                new PropertyModel<String>(bintrayModel, "repo"), getBintrayRepos());
        reposDropDown.setRequired(isFieldRequired());
        reposDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String selectedRepo = reposDropDown.getModelObject();
                List<String> repoPackages = getRepoPackages(selectedRepo);
                packagesComboBox.setChoices(repoPackages);
                if (!repoPackages.isEmpty()) {
                    String selectedPackage = repoPackages.get(0);
                    packagesComboBox.setDefaultModelObject(selectedPackage);
                    List<String> packageVersions = getPackageVersions(selectedRepo, selectedPackage);
                    versionsComboBox.setChoices(packageVersions);
                    if (!packageVersions.isEmpty()) {
                        versionsComboBox.setDefaultModelObject(packageVersions.get(0));
                    }
                }
                target.add(form);
                AjaxUtils.refreshFeedback(target);
            }
        });
        form.add(reposDropDown);
        form.add(new HelpBubble("repo.help", new ResourceModel("repo.help")));

        List<String> packagesChoices;
        if (reposDropDown.getModelObject() != null) {
            packagesChoices = getRepoPackages(reposDropDown.getModelObject());
        } else {
            packagesChoices = Lists.newArrayList();
        }

        packagesComboBox = new ComboBox("packageId", new PropertyModel<String>(bintrayModel, "packageId"),
                packagesChoices);
        packagesComboBox.setRequired(isFieldRequired());
        packagesComboBox.setOutputMarkupId(true);
        packagesComboBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                List<String> packageVersions = getPackageVersions(reposDropDown.getModelObject(),
                        packagesComboBox.getModelObject());
                versionsComboBox.setChoices(packageVersions);
                if (!packageVersions.isEmpty()) {
                    versionsComboBox.setDefaultModelObject(packageVersions.get(0));
                }
                target.add(versionsComboBox.getParent());
                AjaxUtils.refreshFeedback(target);
            }
        });
        form.add(packagesComboBox);
        form.add(new HelpBubble("packageId.help", new ResourceModel("packageId.help")));

        List<String> versionsChoices;
        if (reposDropDown.getModelObject() != null && packagesComboBox.getModelObject() != null) {
            versionsChoices = getPackageVersions(reposDropDown.getModelObject(), packagesComboBox.getModelObject());
        } else {
            versionsChoices = Lists.newArrayList();
        }

        versionsComboBox = new ComboBox("version", new PropertyModel<String>(bintrayModel, "version"), versionsChoices);
        versionsComboBox.setRequired(isFieldRequired());
        versionsComboBox.setOutputMarkupId(true);
        form.add(versionsComboBox);
        form.add(new HelpBubble("version.help", new ResourceModel("version.help")));

        addExtraComponentsToForm(form);

        TitledAjaxSubmitLink pushButton = createPushButton(form, "push");
        form.add(new DefaultButtonBehavior(pushButton));
        add(pushButton);
        addExtraButton(form);
        add(new ModalCloseLink("cancel"));
    }

    protected abstract void addExtraComponentsToForm(Form form);

    protected abstract String getBintrayDescriptionLabel();

    protected String getBintrayLink() {
        return "<a href=\"https://bintray.com\" target=\"blank\">Bintray</a>";
    }

    protected void addExtraButton(Form form) {
    }

    private List<String> getBintrayRepos() {
        List<String> repos = Lists.newArrayList();
        try {
            List<Repo> reposToDeploy = bintrayService.getReposToDeploy(headersMap);
            repos = Lists.newArrayList(Iterables.transform(reposToDeploy, new Function<Repo, String>() {
                @Override
                public String apply(Repo repo) {
                    return repo.getOwner() + "/" + repo.getName();
                }
            }));
        } catch (IOException e) {
            if (getFeedbackMessages().isEmpty()) {
                error("Connection failed with exception: " + e.getMessage());
            }
        } catch (BintrayException e) {
            if (getFeedbackMessages().isEmpty()) {
                error("Could not retrieve repositories list from Bintray.");
            }
        }

        return repos;
    }

    private List<String> getRepoPackages(String repoKey) {
        List<String> repoPackages = Lists.newArrayList();
        try {
            repoPackages = bintrayService.getPackagesToDeploy(repoKey, headersMap);
        } catch (IOException e) {
            if (getFeedbackMessages().isEmpty()) {
                error("Connection failed with exception: " + e.getMessage());
            }
        } catch (BintrayException e) {
            if (getFeedbackMessages().isEmpty()) {
                error("Could not retrieve packages list for '" + repoKey + "'");
            }
        }

        return repoPackages;
    }

    private List<String> getPackageVersions(String repoKey, String packageId) {
        List<String> packageVersions = Lists.newArrayList();
        try {
            packageVersions = bintrayService.getVersions(repoKey, packageId, headersMap);
        } catch (IOException e) {
            if (getFeedbackMessages().isEmpty()) {
                error("Connection failed with exception: " + e.getMessage());
            }
        } catch (BintrayException e) {
            if (getFeedbackMessages().isEmpty()) {
                error("Could not retrieve versions list for Repository '" + repoKey + "' and Package '" + packageId + "'");
            }
        }

        return packageVersions;
    }

    private TitledAjaxSubmitLink createPushButton(Form form, String wicketId) {
        return new TitledAjaxSubmitLink(wicketId, "Push", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                onPushClicked();
                AjaxUtils.refreshFeedback(target);
                ModalHandler.closeCurrent(target);
            }
        };
    }

    protected abstract void onPushClicked();
}
