package org.artifactory.descriptor.repo;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.artifactory.descriptor.repo.vcs.VcsGitProvider;
import org.artifactory.descriptor.repo.vcs.VcsUrlBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.MessageFormat;

/**
 * @author Yoav Luft
 */
@XmlType(name = "VcsGitType", propOrder = {"provider", "downloadUrl"})
public class VcsGitConfiguration implements Descriptor {

    @XmlElement(name = "provider")
    private VcsGitProvider provider = VcsGitProvider.GITHUB;

    @XmlElement(name = "downloadUrl")
    private String downloadUrl;

    public VcsGitConfiguration() {
    }

    public VcsGitProvider getProvider() {
        return provider;
    }

    public void setProvider(VcsGitProvider provider) {
        this.provider = provider;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * Constructs ResourceDownloadUrl
     *
     * @param user git user
     * @param repository git repository
     * @param file a file to download
     * @param version content branch/tag
     *
     * @return ResourceDownloadUrl
     */
    public String buildResourceDownloadUrl(String user, String repository, String file, String version) {
        return VcsUrlBuilder.resourceDownloadUrl(provider, user, repository, file, version);
    }

    /**
     * Constructs RepositoryDownloadUrl
     *
     * @param gitOrg git user
     * @param gitRepo git repository
     * @param version content branch/tag
     * @param fileExt file ext
     *
     * @return RepositoryDownloadUrl
     */
    public String buildRepositoryDownloadUrl(String gitOrg, String gitRepo, String version, String fileExt) {
        String url = StringUtils.isNotBlank(downloadUrl) ? downloadUrl : provider.getRepositoryDownloadUrl();
        return VcsUrlBuilder.repositoryDownloadUrl(url, gitOrg, gitRepo, version, fileExt);
    }
}
