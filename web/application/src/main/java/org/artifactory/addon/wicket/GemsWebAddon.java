package org.artifactory.addon.wicket;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.artifactory.addon.Addon;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.repo.RepoPath;

/**
 * @author mamo
 */
public interface GemsWebAddon extends Addon {

    /**
     * build a config section for the Configuration->Repositories->Edit->Packages tab
     */
    WebMarkupContainer buildPackagesConfigSection(String id, RepoDescriptor descriptor, Form form);

    /**
     * build an info section for the Tree Repository Browser for a application/x-rubygems mimetype
     */
    WebMarkupContainer buildInfoSection(String id, RepoPath repoPath);

    /**
     * build a distribution management section for the General Info tab
     *
     * @param id The panel id
     * @param repoPath The repo path of the root repository
     */
    WebMarkupContainer buildDistributionManagementPanel(String id, RepoPath repoPath);

    HttpRequestBase getRemoteRepoTestMethod(String repoUrl, HttpRepoDescriptor repo);

    ITab buildPackagesConfigTab(String id, RepoDescriptor repoDescriptor, Form form);
}
