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

package org.artifactory.webapp.wicket.page.browse.treebrowser.tabs.general.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.common.wicket.component.LabeledValue;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.webapp.wicket.behavior.AbstractAjaxRestartableTimerBehavior;

import java.util.concurrent.TimeUnit;

/**
 * Displays online status of remote repository in the general config panel when a cache repo is selected.
 *
 * @author Yossi Shaul
 */
public class OnlineStatusPanel extends Panel {

    @SpringBean
    private RepositoryService repositoryService;

    @SpringBean
    private CentralConfigService centralConfigService;

    @SpringBean
    private AuthorizationService authorizationService;


    private RemoteRepoDescriptor remoteRepository;
    private AbstractAjaxRestartableTimerBehavior behavior;
    private LabeledValue onlineStatusLabel;
    private boolean isOffline;

    OnlineStatusPanel(String id, RemoteRepoDescriptor remoteRepo) {
        super(id);
        setOutputMarkupId(true);
        this.remoteRepository = remoteRepo;
        isOffline = remoteRepo.isOffline() || centralConfigService.getDescriptor().isOfflineMode();
        onlineStatusLabel = new LabeledValue("status", "Online Status: ", "");
        onlineStatusLabel.setValue(getStatusText(remoteRepo, isOffline));
        behavior = new AbstractAjaxRestartableTimerBehavior(Duration.seconds(getSecondsForNextRefresh()),
                "ajaxRefresh") {

            @Override
            protected void onTimer(AjaxRequestTarget target) {
                onlineStatusLabel.setValue(getStatusText(remoteRepository, isOffline));
                this.setUpdateInterval(Duration.seconds(getSecondsForNextRefresh()));
                target.add(OnlineStatusPanel.this);
            }
        };

        // WebMarkupContainer statusLabel=new WebMarkupContainer("statusLabel");
        // add(statusLabel);
        onlineStatusLabel.setOutputMarkupId(true);

        addOnlineInfo();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.renderOnDomReadyJavaScript("var refreshLabel");
        response.renderOnDomReadyJavaScript("clearTimeout(refreshLabel)");
        if (behavior != null && remoteRepository != null && onlineStatusLabel != null) {
            onlineStatusLabel.setValue(getStatusText(remoteRepository, isOffline));

            if (repositoryService.isRemoteAssumedOffline(remoteRepository.getKey())) {

                response.renderOnLoadJavaScript("refreshLabel=GetCount(" + repositoryService.getRemoteNextOnlineCheck(
                        remoteRepository.getKey()) + ", 'statusLabel')");
                if (behavior.isStopped()) {
                    behavior.setUpdateInterval(Duration.seconds(getSecondsForNextRefresh()));
                    behavior.start();
                }

            } else {
                behavior.stop();
                response.renderOnDomReadyJavaScript("clearTimeout(ajaxRefresh)");
                response.renderOnDomReadyJavaScript("clearTimeout(refreshLabel)");
            }

        }
    }

    private void addOnlineInfo() {


        onlineStatusLabel.add(behavior);
        add(onlineStatusLabel);
        WebMarkupContainer resetButton = new TitledAjaxLink("resetButton", "Set Online") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                repositoryService.resetAssumedOffline(remoteRepository.getKey());
                onlineStatusLabel.setValue(getStatusText(remoteRepository, isOffline));
                /* //stops java script refreshing of counter
                behavior.stop();
                target.appendJavaScript("clearTimeout(refreshLabel)");*/
                target.add(OnlineStatusPanel.this);
                target.appendJavaScript("clearTimeout(refreshLabel)");
            }

            @Override
            public boolean isVisible() {

                return (authorizationService.isAdmin() &&
                        repositoryService.isRemoteAssumedOffline(remoteRepository.getKey()));
            }
        };
        add(resetButton);
    }

    private long getSecondsForNextRefresh() {
        long nextCheckTime = repositoryService.getRemoteNextOnlineCheck(remoteRepository.getKey());
        long nextCheckSeconds = Math.max(0,
                TimeUnit.MILLISECONDS.toSeconds(nextCheckTime - System.currentTimeMillis()));
        return nextCheckSeconds + 1;
    }

    private String getStatusText(RemoteRepoDescriptor remoteRepo, boolean offline) {
        String status = "Online";
        if (offline) {
            status = "Offline";
        }
        if (repositoryService.isRemoteAssumedOffline(remoteRepo.getKey())) {
            status = "Assumed offline.";
        }
        return status;
    }


}
