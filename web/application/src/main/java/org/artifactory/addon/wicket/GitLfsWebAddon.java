package org.artifactory.addon.wicket;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.wicket.util.ItemCssClass;

/**
 * @author Dan Feldman
 */
public interface GitLfsWebAddon extends Addon {

    /**
     * Assemble the repo Git LFS configuration section and add it to the given form
     *
     * @param form       Repo configuration form
     * @param descriptor Configured repo descriptor
     */
    void createAndAddRepoConfigGitLfsSection(Form<LocalRepoDescriptor> form, RepoDescriptor descriptor);

    ItemCssClass getFileCssClass(RepoPath path);

    WebMarkupContainer buildDistributionManagementPanel(String id, RepoPath repoPath);
}
