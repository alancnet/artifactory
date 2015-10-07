package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.bintray.BintrayPackageInfo;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.modal.links.ModalShowLink;
import org.artifactory.common.wicket.component.modal.panel.BaseModalPanel;
import org.artifactory.common.wicket.component.panel.feedback.UnescapedFeedbackMessage;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.ItemInfo;
import org.artifactory.mime.NamingUtils;
import org.artifactory.webapp.wicket.page.browse.treebrowser.action.BintrayArtifactPanel;
import org.artifactory.webapp.wicket.page.security.login.LoginPage;
import org.artifactory.webapp.wicket.page.security.profile.ProfilePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bintray dynamic info panel (Changes dynamically according to the fileInfo)
 *
 * @author Gidi Shabat
 */
public class BintrayDynamicInfoPanel extends Panel {
    private static final Logger log = LoggerFactory.getLogger(BintrayDynamicInfoPanel.class);
    private static final String DYNAMIC_BINTRAY_INFO_PANEL = "bintrayInfoPanel";
    private static final String DYNAMIC_PUSH_TO_BINTRAY = "pushToBintrayPanel";
    @SpringBean
    protected BintrayService bintrayService;
    @SpringBean
    protected RepositoryService repositoryService;
    @SpringBean
    protected AuthorizationService authorizationService;
    @SpringBean
    private UserGroupService userGroupService;
    private ItemInfo itemInfo;
    private BintrayPackageInfo bintrayPackageInfo;
    private BintrayInfoPanelBehavior currentBintrayInfoPanelBehavior;

    public BintrayDynamicInfoPanel(String id, final ItemInfo itemInfo) {
        super(id);
        this.itemInfo = itemInfo;
        // Show push to bintray button.
        showPushToBintrayButton();
        // Try to get Bintray package info from local cache or from Bintray.
        // If local cache is empty then wait until the asynchronous REST request to bintray finished.
        add(new WebMarkupContainer(DYNAMIC_BINTRAY_INFO_PANEL).setVisible(false));
        currentBintrayInfoPanelBehavior = getBintrayInfoPanelBehavior();
        showInfoPanel(currentBintrayInfoPanelBehavior);
        if (isPackageInProcess()) {
            add(new BintrayItemTimerBehavior(Duration.milliseconds(2000)));
        }
    }

    private void showInfoPanel(BintrayInfoPanelBehavior infoPanelBehavior) {
        switch (infoPanelBehavior) {
            case normal: {
                addBintrayInfoPanel();
                break;
            }
            case anonymous: {
                addAnonymousBintrayInfoPanel();
                break;
            }
            case noBintrayAuthentication: {
                addNoBintrayAuthenticationPanel();
                break;
            }
            case inProgress: {
                addInProgressPanel();
                break;
            }

            case notExistsOnJCenter: {
                addNotExistsInBintrayPanel();
                break;
            }
            case packageRetrievalError: {
                addPackageRetrievalErrorPanel();
                break;
            }
            case hide: {
                addEmptyPanel();
                break;
            }
        }
    }

    private void addBintrayInfoPanel() {
        BintrayInfoPanel panel = new BintrayInfoPanel(DYNAMIC_BINTRAY_INFO_PANEL, bintrayPackageInfo);
        BintrayDynamicInfoPanel.this.replace(panel);
    }

    private void addNoBintrayAuthenticationPanel() {
        CharSequence url = RequestCycle.get().urlFor(ProfilePage.class, null);
        String message = "To view latest version information from Bintray, please configure your Bintray authentication.";
        WebMarkupContainer panel = new BintrayInfoLinkMessagePanel(DYNAMIC_BINTRAY_INFO_PANEL, message, url.toString());
        BintrayDynamicInfoPanel.this.replace(panel);
    }

    private void addAnonymousBintrayInfoPanel() {
        CharSequence url = RequestCycle.get().urlFor(LoginPage.class, null);
        String message = "Please login to view latest version information from Bintray.";
        WebMarkupContainer panel = new BintrayInfoLinkMessagePanel(DYNAMIC_BINTRAY_INFO_PANEL, message, url.toString());
        BintrayDynamicInfoPanel.this.replace(panel);
    }

    private void addNotExistsInBintrayPanel() {
        String url = ConstantValues.bintrayUrl.getString() + "/repo/browse/bintray/jcenter";
        String message = "This item does not exist in Bintray's JCenter.";
        WebMarkupContainer panel = new BintrayInfoLinkMessagePanel(DYNAMIC_BINTRAY_INFO_PANEL, message, url.toString());
        BintrayDynamicInfoPanel.this.replace(panel);
    }

    private void addPackageRetrievalErrorPanel() {
        String url = ConstantValues.bintrayUrl.getString() + "/repo/browse/bintray/jcenter";
        String message = "Could not retrieve latest version information from Bintray.";
        WebMarkupContainer panel = new BintrayInfoLinkMessagePanel(DYNAMIC_BINTRAY_INFO_PANEL, message, url.toString());
        BintrayDynamicInfoPanel.this.replace(panel);
    }

    private void addInProgressPanel() {
        String url = ConstantValues.bintrayUrl.getString() + "/repo/browse/bintray/jcenter";
        String message = "Getting the latest version information from Bintray...";
        WebMarkupContainer panel = new BintrayInfoLinkMessagePanel(DYNAMIC_BINTRAY_INFO_PANEL, message, url.toString());
        BintrayDynamicInfoPanel.this.replace(panel);
    }

    private void addEmptyPanel() {
        WebMarkupContainer container = new WebMarkupContainer(DYNAMIC_BINTRAY_INFO_PANEL);
        container.setVisible(false);
        BintrayDynamicInfoPanel.this.replace(container);
    }

    private BintrayInfoPanelBehavior getBintrayInfoPanelBehavior() {
        try {

            boolean anonymousUser = authorizationService.isAnonymous();
            boolean hasSystemAPIKey = bintrayService.hasBintraySystemUser();
            boolean userHasBintrayAuth = bintrayService.isUserHasBintrayAuth();
            boolean hideInfo = ConstantValues.bintrayUIHideInfo.getBoolean();
            boolean userExists = isUserExists();
            boolean validFile = isValidFile();
            if (hideInfo || !validFile || (!hasSystemAPIKey && !userExists)) {
                return BintrayInfoPanelBehavior.hide;
            }
            if (anonymousUser && !hasSystemAPIKey) {
                return BintrayInfoPanelBehavior.anonymous;
            }
            if (!userHasBintrayAuth && !hasSystemAPIKey) {
                return BintrayInfoPanelBehavior.noBintrayAuthentication;
            }
            String sha1 = ((FileInfo) itemInfo).getChecksumsInfo().getSha1();
            Map<String, String> headersMap = WicketUtils.getHeadersMap();
            bintrayPackageInfo = bintrayService.getBintrayPackageInfo(sha1, headersMap);
            if (isPackageExsists()) {
                return BintrayInfoPanelBehavior.normal;
            }
            if (isPackageNotFound()) {
                return BintrayInfoPanelBehavior.notExistsOnJCenter;
            }
            if (isPackageRetrievalError()) {
                return BintrayInfoPanelBehavior.packageRetrievalError;
            }
            if (isPackageInProcess()) {
                return BintrayInfoPanelBehavior.inProgress;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Failed fetching Bintray package info for {}.", itemInfo.getRepoPath(), e);
            } else {
                log.warn("Failed fetching Bintray package info for {}: {}", itemInfo.getRepoPath(), e.getMessage());
            }
            return BintrayInfoPanelBehavior.packageRetrievalError;
        }
        return BintrayInfoPanelBehavior.hide;
    }

    private boolean isValidFile() {
        return NamingUtils.isJarVariant(itemInfo.getName()) || NamingUtils.isPom(itemInfo.getName());
    }

    private boolean isPackageNotFound() {
        return bintrayPackageInfo == BintrayService.PACKAGE_NOT_FOUND;
    }

    private boolean isPackageInProcess() {
        return bintrayPackageInfo == BintrayService.PACKAGE_IN_PROCESS;
    }

    private boolean isPackageRetrievalError() {
        return bintrayPackageInfo == BintrayService.PACKAGE_RETRIEVAL_ERROR;
    }

    private boolean isPackageExsists() {
        return bintrayPackageInfo != null && bintrayPackageInfo != BintrayService.PACKAGE_IN_PROCESS &&
                bintrayPackageInfo != BintrayService.PACKAGE_NOT_FOUND && bintrayPackageInfo != BintrayService.PACKAGE_RETRIEVAL_ERROR;
    }

    private void showPushToBintrayButton() {
        PushToBintrayBehavior pushToBintrayBehavior = getPushToBintrayButtonBehavior();
        switch (pushToBintrayBehavior) {
            case normal: {
                showPushToButton();
                break;
            }
            case hide: {
                add(new WebMarkupContainer(DYNAMIC_PUSH_TO_BINTRAY));
                break;
            }
        }
    }

    private PushToBintrayBehavior getPushToBintrayButtonBehavior() {
        boolean userExists = isUserExists();
        String repoKey = itemInfo.getRepoKey();
        RepoDescriptor repoDescriptor = repositoryService.repoDescriptorByKey(repoKey);
        boolean localRepository = isLocalNonDockerRepo(repoDescriptor);
        boolean anonymousUser = authorizationService.isAnonymous();
        boolean hideUploads = ConstantValues.bintrayUIHideUploads.getBoolean();
        return !anonymousUser && localRepository && !itemInfo.isFolder() && !hideUploads && userExists ?
                PushToBintrayBehavior.normal :
                PushToBintrayBehavior.hide;
    }

    private boolean isUserExists() {
        AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
        CoreAddons addons = addonsManager.addonByType(CoreAddons.class);
        return !addons.isAolAdmin() && !userGroupService.currentUser().isTransientUser();
    }

    private boolean isLocalNonDockerRepo(RepoDescriptor repoDescriptor) {
        return repoDescriptor.getClass().equals(LocalRepoDescriptor.class)
                && !repoDescriptor.getType().equals(RepoType.Docker);
    }

    private void showPushToButton() {
        ModalShowLink link = new ModalShowLink(DYNAMIC_PUSH_TO_BINTRAY, "") {
            @Override
            protected BaseModalPanel getModelPanel() {
                return new BintrayArtifactPanel(itemInfo);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (!bintrayService.isUserHasBintrayAuth()) {
                    String profilePagePath = WicketUtils.absoluteMountPathForPage(ProfilePage.class);
                    String message = "You do not have Bintray credentials configured, please configure them from your <a href=\"" + profilePagePath + "\">profile page</a>.";
                    getPage().error(new UnescapedFeedbackMessage(message));
                } else {
                    super.onClick(target);
                }
            }
        };
        link.add(new CssClass("bintray_button"));
        add(link);
    }

    private enum BintrayInfoPanelBehavior {
        normal, anonymous, inProgress, notExistsOnJCenter, noBintrayAuthentication, packageRetrievalError, hide
    }

    private enum PushToBintrayBehavior {
        normal, hide
    }

    private class BintrayItemTimerBehavior extends AbstractAjaxTimerBehavior {
        public BintrayItemTimerBehavior(Duration duration) {
            super(duration);
        }

        @Override
        protected void onTimer(AjaxRequestTarget target) {
            // Try to get  BintrayService package info.
            // If still in process (no result from Bintray), wait, else show the corresponded panel and stop waiting.
            BintrayInfoPanelBehavior bintrayInfoPanelBehavior = getBintrayInfoPanelBehavior();
            long updateInterval = getUpdateInterval().getMilliseconds();
            // Max time to wait is 1+2+4+8=15 seconds
            long maxTimeToWait = TimeUnit.SECONDS.toMillis(16);
            if (isPackageInProcess()) {
                if (updateInterval < maxTimeToWait) {
                    // Check less frequently.
                    setUpdateInterval(Duration.milliseconds(updateInterval * 2));
                } else {
                    bintrayInfoPanelBehavior = BintrayInfoPanelBehavior.packageRetrievalError;
                    stop();
                }
            } else {
                stop();
            }
            // Change the panels only if real change occurred
            if (currentBintrayInfoPanelBehavior != bintrayInfoPanelBehavior) {
                currentBintrayInfoPanelBehavior = bintrayInfoPanelBehavior;
                showInfoPanel(bintrayInfoPanelBehavior);
                target.add(BintrayDynamicInfoPanel.this);
            }
        }

        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator() {
            return new NoAjaxIndicatorDecorator();
        }
    }
}