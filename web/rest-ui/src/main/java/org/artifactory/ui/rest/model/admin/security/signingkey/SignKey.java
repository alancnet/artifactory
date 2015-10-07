package org.artifactory.ui.rest.model.admin.security.signingkey;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class SignKey extends BaseModel {

    private String publicKeyLink;
    private String passPhrase;
    boolean publicKeyInstalled;
    boolean privateKeyInstalled;

    public SignKey() {
    }

    public SignKey(String publicKeyDownloadTarget) {
        this.publicKeyLink = publicKeyDownloadTarget;
    }

    public String getPublicKeyLink() {
        return publicKeyLink;
    }

    public void setPublicKeyLink(String publicKeyLink) {
        this.publicKeyLink = publicKeyLink;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    public boolean isPublicKeyInstalled() {
        return publicKeyInstalled;
    }

    public void setPublicKeyInstalled(boolean publicKeyInstalled) {
        this.publicKeyInstalled = publicKeyInstalled;
    }

    public boolean isPrivateKeyInstalled() {
        return privateKeyInstalled;
    }

    public void setPrivateKeyInstalled(boolean privateKeyInstalled) {
        this.privateKeyInstalled = privateKeyInstalled;
    }
}
