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

package org.artifactory.webapp.wicket.page.home.settings.modal.editable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.WicketProperty;
import org.artifactory.common.wicket.component.border.titled.TitledBorder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;

import java.util.List;

/**
 * Base implementation of build-tool settings provisioning border
 *
 * @author Noam Y. Tenne
 */
public abstract class BaseSettingsProvisioningBorder extends TitledBorder {

    @WicketProperty
    protected LocalRepoDescriptor selectedRepo;

    @WicketProperty
    protected String deploymentPath;

    protected BaseSettingsProvisioningBorder(String id, Form form, TextArea<String> content, String saveToFileName) {
        super(id);
        deploymentPath = saveToFileName;

        add(new DropDownChoice<>("repoSelect",
                new PropertyModel<LocalRepoDescriptor>(this, "selectedRepo"),
                getDeployableRepoDescriptors(), new RepoKeyChoiceRenderer()));

        add(new TextField<>("deploymentPath", new PropertyModel<String>(this, "deploymentPath")));

        add(getDeploymentLink("deploy", "Deploy", form, content));
    }

    protected abstract List<? extends LocalRepoDescriptor> getDeployableRepoDescriptors();

    protected abstract Component getDeploymentLink(String id, String title, Form form,
            TextArea<String> editableContent);

    private static class RepoKeyChoiceRenderer implements IChoiceRenderer<LocalRepoDescriptor> {

        @Override
        public Object getDisplayValue(LocalRepoDescriptor object) {
            return object.getKey();
        }

        @Override
        public String getIdValue(LocalRepoDescriptor object, int index) {
            return object.getKey();
        }
    }

    @Override
    public String getTitle() {
        return "Settings Provisioning";
    }
}
