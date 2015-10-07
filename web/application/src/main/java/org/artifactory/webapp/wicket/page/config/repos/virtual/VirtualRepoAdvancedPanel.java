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

package org.artifactory.webapp.wicket.page.config.repos.virtual;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.wicket.WebstartWebAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.StatusHolder;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.CreateUpdateAction;
import org.artifactory.common.wicket.component.checkbox.styled.StyledCheckbox;
import org.artifactory.common.wicket.component.help.HelpBubble;
import org.artifactory.common.wicket.component.links.TitledAjaxSubmitLink;
import org.artifactory.common.wicket.util.AjaxUtils;
import org.artifactory.descriptor.repo.PomCleanupPolicy;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.wicket.page.config.SchemaHelpBubble;

import java.util.Arrays;
import java.util.List;

/**
 * @author Noam Y. Tenne
 */
public class VirtualRepoAdvancedPanel extends Panel {

    @SpringBean
    private AddonsManager addons;

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private RepositoryService repositoryService;

    private final CreateUpdateAction action;
    private final VirtualRepoDescriptor repoDescriptor;
    private final Form<VirtualRepoDescriptor> form;

    public VirtualRepoAdvancedPanel(String id, CreateUpdateAction action, VirtualRepoDescriptor repoDescriptor,
            Form<VirtualRepoDescriptor> form) {
        super(id);
        this.action = action;
        this.repoDescriptor = repoDescriptor;
        this.form = form;
        add(new CssClass("virtual-repo-panel-advanced-settings"));

        List<RepoLayout> layouts = centralConfigService.getDescriptor().getRepoLayouts();
        DropDownChoice<RepoLayout> repoLayout = new DropDownChoice<>("repoLayout", layouts,
                new ChoiceRenderer<RepoLayout>("name"));
        repoLayout.setNullValid(true);

        add(repoLayout);
        add(new SchemaHelpBubble("repoLayout.help"));

        // artifactoryRequestsCanRetrieveRemoteArtifacts
        add(new StyledCheckbox("artifactoryRequestsCanRetrieveRemoteArtifacts"));
        add(new SchemaHelpBubble("artifactoryRequestsCanRetrieveRemoteArtifacts.help"));

        // pomRepositoryReferencesCleanupPolicy
        PomCleanupPolicy[] policies = PomCleanupPolicy.values();
        DropDownChoice pomCleanPolicy =
                new DropDownChoice<>("pomRepositoryReferencesCleanupPolicy", Arrays.asList(policies),
                        new ChoiceRenderer<PomCleanupPolicy>("message"));
        add(pomCleanPolicy);
        add(new SchemaHelpBubble("pomRepositoryReferencesCleanupPolicy.help"));

        // keyPair
        WebstartWebAddon webstartAddon = addons.addonByType(WebstartWebAddon.class);
        add(webstartAddon.getKeyPairContainer("keyPairContainer", repoDescriptor.getKey(), isCreate()));
        addCleanCache();
    }

    private void addCleanCache() {
        TitledAjaxSubmitLink cleanCacheButton =
                new TitledAjaxSubmitLink("cleanCache", Model.of("Zap Caches"), form) {
                    @Override
                    protected void onSubmit(AjaxRequestTarget target, Form form) {
                        String repoKey = repoDescriptor.getKey();
                        RepoPath repoPath = InternalRepoPathFactory.repoRootPath(repoKey);
                        StatusHolder statusHolder;
                        statusHolder = repositoryService.undeploy(repoPath, false);
                        if (!statusHolder.isError()) {
                            info("The caches of '" + repoKey + "' have been successfully zapped.");
                        } else {
                            String message = "Could not zap caches for the virtual repository '" + repoKey + "': " +
                                    statusHolder.getStatusMsg() + "";
                            error(message);
                        }
                        AjaxUtils.refreshFeedback(target);
                    }
                };
        cleanCacheButton.setVisible(!isCreate());
        add(cleanCacheButton);
        HelpBubble help = new HelpBubble("cleanCache.help",
                "Clears all caches stored on the virtual repository level\n" +
                        "(transformed POMs, JNLP files, merged indexes, etc.)");
        help.setVisible(!isCreate());
        add(help);
    }

    private boolean isCreate() {
        return CreateUpdateAction.CREATE.equals(action);
    }
}
