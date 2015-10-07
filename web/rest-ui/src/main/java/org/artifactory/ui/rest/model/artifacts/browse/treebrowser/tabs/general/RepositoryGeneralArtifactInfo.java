package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general;

import com.google.common.base.Joiner;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.debian.DebianAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RealRepoDescriptor;
import org.artifactory.descriptor.repo.RepoBaseDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.util.JsonUtil;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.BaseInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.info.RepositoryInfo;
import org.artifactory.util.HttpUtils;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Chen Keinan
 */
@JsonTypeName("repository")
public class RepositoryGeneralArtifactInfo extends GeneralArtifactInfo implements RestGeneralTab {

    private String blackedOutMessage;

    RepositoryGeneralArtifactInfo(){}
    public RepositoryGeneralArtifactInfo(String name) {
        super(name);
    }


    @Override
    public void populateGeneralData(ArtifactoryRestRequest artifactoryRestRequest, AuthorizationService authService) {
        RepoPath repoPath = retrieveRepoPath();
        CentralConfigService centralConfigService = retrieveCentralConfigService();
        RepositoryService repoService = retrieveRepoService();
        LocalRepoDescriptor repoDescriptor = localOrCachedRepoDescriptor(repoPath);
        String baseUrl = HttpUtils.getServletContextUrl(artifactoryRestRequest.getServletRequest());
        //populate repository info
        BaseInfo baseInfo = populateRepositoryInfo(repoService, repoDescriptor, repoPath, centralConfigService,
                authService, baseUrl);
        super.setInfo(baseInfo);
        // populate virtual repositories
        populateVirtualRepositories(HttpUtils.getServletContextUrl(artifactoryRestRequest.getServletRequest()));
        setRepositoryBlackedOut(repoDescriptor);
    }

    /**
     * populate Repository info data
     * @param repoService  -repository service
     * @param repoDescriptor - repo descriptor
     * @param repoPath - repo path
     * @param centralConfigService - central config service
     * @return
     */
    private BaseInfo populateRepositoryInfo(RepositoryService repoService ,
            LocalRepoDescriptor repoDescriptor, RepoPath repoPath, CentralConfigService centralConfigService,
            AuthorizationService authService, String baseUrl) {
        RepositoryInfo repoInfo = new RepositoryInfo();
        repoInfo.populateRepositoryInfo(repoService, repoDescriptor, repoPath, centralConfigService,
                authService.currentUsername());
        setGpgKeyLink(repoDescriptor, repoInfo, baseUrl);
        return repoInfo;
    }

    private void setRepositoryBlackedOut(RepoBaseDescriptor repoDescriptor) {
        if (repoDescriptor != null && repoDescriptor instanceof RealRepoDescriptor) {
            if (((RealRepoDescriptor) repoDescriptor).isBlackedOut()) {
                this.setBlackedOutMessage("This repository is blacked out, " +
                        "items can only be viewed but cannot be resolved or deployed.");
            }
        }
    }

    //Add gpg public key under general info in Debian repository only
    private void setGpgKeyLink(RepoBaseDescriptor repoDescriptor, RepositoryInfo repoInfo, String baseUrl) {
        if(repoDescriptor.getType().equals(RepoType.Debian)){
            AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
            DebianAddon debianAddon = addonsManager.addonByType(DebianAddon.class);
            if (debianAddon.hasPublicKey()) {
                String gpgLink = Joiner.on('/').join(baseUrl, "api", "gpg", "key/public");
                repoInfo.setSigningKeyLink(gpgLink);
            }
        }
    }

    public String getBlackedOutMessage() {
        return blackedOutMessage;
    }

    public void setBlackedOutMessage(String blackedOutMessage) {
        this.blackedOutMessage = blackedOutMessage;
    }

    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
