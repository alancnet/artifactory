package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.generalconfig.GeneralConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UpdateGeneralConfigService implements RestService {

    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        GeneralConfig generalConfig = (GeneralConfig) request.getImodel();
        // update general setting and set config descriptor
        updateDescriptorAndSave(generalConfig);
        response.info("Successfully updated settings");
    }

    /**
     * update config descriptor with general config setting and save
     *
     * @param generalConfig - general setting sent from client
     */
    private void updateDescriptorAndSave(GeneralConfig generalConfig) {
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        mutableDescriptor.setServerName(generalConfig.getServerName());
        mutableDescriptor.setDateFormat(generalConfig.getDateFormat());
        mutableDescriptor.setUrlBase(generalConfig.getCustomUrlBase());
        mutableDescriptor.setFileUploadMaxSizeMb(generalConfig.getFileUploadMaxSize());
        mutableDescriptor.setOfflineMode(generalConfig.isGlobalOfflineMode());
        mutableDescriptor.getAddons().setShowAddonsInfo(generalConfig.isShowAddonSettings());
        mutableDescriptor.setLogo(generalConfig.getLogoUrl());
        mutableDescriptor.setHelpLinksEnabled(generalConfig.isHelpLinksEnabled());
        // update bintray config descriptor
        updateBintrayDescriptor(generalConfig, mutableDescriptor);
        //System message config
        updateSystemMessageConfig(generalConfig, mutableDescriptor);
        //Folder download config
        updateFolderDownloadConfig(generalConfig, mutableDescriptor);
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    private void updateBintrayDescriptor(GeneralConfig generalConfig,
            MutableCentralConfigDescriptor mutableDescriptor) {
        BintrayConfigDescriptor bintrayMutableDescriptor = Optional.ofNullable(mutableDescriptor.getBintrayConfig())
                .orElse(new BintrayConfigDescriptor());
        bintrayMutableDescriptor.setFileUploadLimit(generalConfig.getBintrayFilesUploadLimit());
        mutableDescriptor.setBintrayConfig(bintrayMutableDescriptor);
    }

    //Does not override defaults if UI sent empty model.
    private void updateSystemMessageConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        SystemMessageDescriptor systemMessageDescriptor =
                Optional.ofNullable(descriptor.getSystemMessageConfig()).orElse(new SystemMessageDescriptor());
        systemMessageDescriptor.setEnabled(Optional.ofNullable(
                generalConfig.isSystemMessageEnabled()).orElse(systemMessageDescriptor.isEnabled()));
        systemMessageDescriptor.setTitle(Optional.ofNullable(
                generalConfig.getSystemMessageTitle()).orElse(systemMessageDescriptor.getTitle()));
        systemMessageDescriptor.setTitleColor(Optional.ofNullable(
                generalConfig.getSystemMessageTitleColor()).orElse(systemMessageDescriptor.getTitleColor()));
        systemMessageDescriptor.setMessage(Optional.ofNullable(
                generalConfig.getSystemMessage()).orElse(systemMessageDescriptor.getMessage()));
        systemMessageDescriptor.setShowOnAllPages(Optional.ofNullable(
                generalConfig.isShowSystemMessageOnAllPages()).orElse(systemMessageDescriptor.isShowOnAllPages()));
        descriptor.setSystemMessageConfig(systemMessageDescriptor);
    }

    //Does not override defaults if UI sent empty model.
    private void updateFolderDownloadConfig(GeneralConfig generalConfig, MutableCentralConfigDescriptor descriptor) {
        FolderDownloadConfigDescriptor folderDownloadConfig = descriptor.getFolderDownloadConfig();
        folderDownloadConfig.setEnabled(
                Optional.ofNullable(generalConfig.isFolderDownloadEnabled()).orElse(folderDownloadConfig.isEnabled()));
        folderDownloadConfig.setMaxConcurrentRequests(Optional.ofNullable(
                generalConfig.getFolderDownloadMaxConcurrentRequests())
                .orElse(folderDownloadConfig.getMaxConcurrentRequests()));
        folderDownloadConfig.setMaxDownloadSizeMb(Optional.ofNullable(
                generalConfig.getFolderDownloadMaxSizeMb()).orElse(folderDownloadConfig.getMaxDownloadSizeMb()));
        folderDownloadConfig.setMaxFiles(Optional.ofNullable(
                generalConfig.getMaxFolderDownloadFilesLimit()).orElse(folderDownloadConfig.getMaxFiles()));
        descriptor.setFolderDownloadConfig(folderDownloadConfig);
    }
}
