package org.artifactory.ui.rest.model.admin.configuration.generalconfig;

import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.download.FolderDownloadConfigDescriptor;
import org.artifactory.descriptor.message.SystemMessageDescriptor;
import org.artifactory.rest.common.model.BaseModel;

import java.util.Optional;

/**
 * @author Chen Keinan
 */
public class GeneralConfig extends BaseModel {

    private String serverName;
    private String customUrlBase;
    private Integer fileUploadMaxSize;
    private String dateFormat;
    private Boolean globalOfflineMode;
    private Boolean showAddonSettings;
    private String logoUrl;
    private int bintrayFilesUploadLimit;
    private Boolean helpLinksEnabled;
    //System message
    private Boolean systemMessageEnabled;
    private String systemMessageTitle;
    private String systemMessageTitleColor;
    private String systemMessage;
    private Boolean showSystemMessageOnAllPages;
    //Folder download
    private Boolean folderDownloadEnabled;
    private Integer folderDownloadMaxSizeMb;
    private Long maxFolderDownloadFilesLimit;
    private Integer FolderDownloadMaxConcurrentRequests;


    public GeneralConfig(){}

    public GeneralConfig(MutableCentralConfigDescriptor mutableDescriptor) {
        serverName =  mutableDescriptor.getServerName();
        customUrlBase = mutableDescriptor.getUrlBase();
        fileUploadMaxSize = mutableDescriptor.getFileUploadMaxSizeMb();
        dateFormat = mutableDescriptor.getDateFormat();
        globalOfflineMode = mutableDescriptor.isOfflineMode();
        showAddonSettings = mutableDescriptor.getAddons().isShowAddonsInfo();
        logoUrl = mutableDescriptor.getLogo();
        bintrayFilesUploadLimit = getBintrayFileUploadLimit(mutableDescriptor);
        helpLinksEnabled = mutableDescriptor.isHelpLinksEnabled();
        //System Message
        SystemMessageDescriptor messageDescriptor = Optional.ofNullable(mutableDescriptor.getSystemMessageConfig())
                .orElse(new SystemMessageDescriptor());
        systemMessageEnabled = messageDescriptor.isEnabled();
        systemMessageTitle = messageDescriptor.getTitle();
        systemMessageTitleColor = messageDescriptor.getTitleColor();
        systemMessage = messageDescriptor.getMessage();
        showSystemMessageOnAllPages = messageDescriptor.isShowOnAllPages();
        //Folder Download
        FolderDownloadConfigDescriptor folderDownloadDescriptor = mutableDescriptor.getFolderDownloadConfig();
        folderDownloadEnabled = folderDownloadDescriptor.isEnabled();
        folderDownloadMaxSizeMb = folderDownloadDescriptor.getMaxDownloadSizeMb();
        maxFolderDownloadFilesLimit = folderDownloadDescriptor.getMaxFiles();
        FolderDownloadMaxConcurrentRequests = folderDownloadDescriptor.getMaxConcurrentRequests();
    }

    private int getBintrayFileUploadLimit(MutableCentralConfigDescriptor mutableDescriptor) {
        if(mutableDescriptor.getBintrayConfig() != null){
            return mutableDescriptor.getBintrayConfig().getFileUploadLimit();
        }
        else {
            return 0;
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getCustomUrlBase() {
        return customUrlBase;
    }

    public void setCustomUrlBase(String customUrlBase) {
        this.customUrlBase = customUrlBase;
    }

    public Integer getFileUploadMaxSize() {
        return fileUploadMaxSize;
    }

    public void setFileUploadMaxSize(Integer fileUploadMaxSize) {
        this.fileUploadMaxSize = fileUploadMaxSize;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Boolean isGlobalOfflineMode() {
        return globalOfflineMode;
    }

    public void setGlobalOfflineMode(Boolean globalOfflineMode) {
        this.globalOfflineMode = globalOfflineMode;
    }

    public Boolean isShowAddonSettings() {
        return showAddonSettings;
    }

    public void setShowAddonSettings(Boolean showAddonSettings) {
        this.showAddonSettings = showAddonSettings;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public int getBintrayFilesUploadLimit() {
        return bintrayFilesUploadLimit;
    }

    public void setBintrayFilesUploadLimit(int bintrayFilesUploadLimit) {
        this.bintrayFilesUploadLimit = bintrayFilesUploadLimit;
    }

    public Boolean isHelpLinksEnabled() {
        return helpLinksEnabled;
    }

    public void setHelpLinksEnabled(Boolean helpLinksEnabled) {
        this.helpLinksEnabled = helpLinksEnabled;
    }

    public boolean isSystemMessageEnabled() {
        return systemMessageEnabled;
    }

    public void setSystemMessageEnabled(boolean systemMessageEnabled) {
        this.systemMessageEnabled = systemMessageEnabled;
    }

    public String getSystemMessageTitle() {
        return systemMessageTitle;
    }

    public void setSystemMessageTitle(String systemMessageTitle) {
        this.systemMessageTitle = systemMessageTitle;
    }

    public String getSystemMessageTitleColor() {
        return systemMessageTitleColor;
    }

    public void setSystemMessageTitleColor(String systemMessageTitleColor) {
        this.systemMessageTitleColor = systemMessageTitleColor;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public boolean isShowSystemMessageOnAllPages() {
        return showSystemMessageOnAllPages;
    }

    public void setShowSystemMessageOnAllPages(boolean showSystemMessageOnAllPages) {
        this.showSystemMessageOnAllPages = showSystemMessageOnAllPages;
    }

    public Boolean isFolderDownloadEnabled() {
        return folderDownloadEnabled;
    }

    public void setFolderDownloadEnabled(boolean folderDownloadEnabled) {
        this.folderDownloadEnabled = folderDownloadEnabled;
    }

    public Integer getFolderDownloadMaxSizeMb() {
        return folderDownloadMaxSizeMb;
    }

    public void setFolderDownloadMaxSizeMb(int folderDownloadMaxSizeMb) {
        this.folderDownloadMaxSizeMb = folderDownloadMaxSizeMb;
    }

    public Long getMaxFolderDownloadFilesLimit() {
        return maxFolderDownloadFilesLimit;
    }

    public void setMaxFolderDownloadFilesLimit(long maxFolderDownloadFilesLimit) {
        this.maxFolderDownloadFilesLimit = maxFolderDownloadFilesLimit;
    }

    public Integer getFolderDownloadMaxConcurrentRequests() {
        return FolderDownloadMaxConcurrentRequests;
    }

    public void setFolderDownloadMaxConcurrentRequests(int folderDownloadMaxConcurrentRequests) {
        FolderDownloadMaxConcurrentRequests = folderDownloadMaxConcurrentRequests;
    }
}